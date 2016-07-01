package com.dfirago.maps.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.dfirago.maps.dao.MarkerEntityDAO;
import com.dfirago.maps.dao.MarkerEntityDAOImpl;
import com.dfirago.maps.enums.PhotoTaskType;
import com.dfirago.maps.R;
import com.dfirago.maps.model.MarkerEntity;
import com.dfirago.maps.util.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_SELECT_FILE = 1;

    public static final String LABEL_CHOOSE_FROM_GALLERY = "Choose from Gallery";
    public static final String LABEL_CANCEL = "Cancel";
    public static final String LABEL_DELETE = "Delete";
    public static final String LABEL_OPEN = "Open";
    public static final String LABEL_TAKE_PHOTO = "Take Photo";

    public static final int MAX_IMAGE_SIZE = 120;

    public static final String TMP_FILE_PATH = "/sdcard/tmp";

    private GoogleMap mMap;

    private PhotoTaskType userChosenTask;

    private LatLng position;

    // markers and appropriate image URIs will be stored here
    private final Map<Marker, Uri> images = new HashMap<>();
    // marker and its db entity
    private final Map<Marker, MarkerEntity> entities = new HashMap<>();

    private MarkerEntityDAO markerEntityDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        markerEntityDAO = new MarkerEntityDAOImpl(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        initializeMarkers();
    }

    private void initializeMarkers() {
        List<MarkerEntity> markers = markerEntityDAO.list();
        for (MarkerEntity marker : markers) {
            restoreMarker(marker);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        position = latLng;
        selectImage();
    }

    private void selectImage() {
        final CharSequence[] items = {LABEL_TAKE_PHOTO, LABEL_CHOOSE_FROM_GALLERY,
                LABEL_CANCEL};
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(LABEL_TAKE_PHOTO)) {
                    userChosenTask = PhotoTaskType.CAMERA;
                    if (Utils.checkPermission(MapsActivity.this, Manifest.permission.CAMERA))
                        cameraIntent();
                } else if (items[item].equals(LABEL_CHOOSE_FROM_GALLERY)) {
                    userChosenTask = PhotoTaskType.GALLERY;
                    if (Utils.checkPermission(MapsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
                        galleryIntent();
                } else if (items[item].equals(LABEL_CANCEL)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(TMP_FILE_PATH)));
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select file"), REQUEST_SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChosenTask.equals(LABEL_TAKE_PHOTO))
                        cameraIntent();
                    else if (userChosenTask.equals(LABEL_CHOOSE_FROM_GALLERY))
                        galleryIntent();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult();
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            addMarker(position, data.getData());
        }
    }

    private void onCaptureImageResult() {
        File file = new File(TMP_FILE_PATH);
        try {
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), null, null));
            addMarker(position, uri);
            if (!file.delete()) {
                Log.i("logMarker", "Failed to delete " + file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addMarker(LatLng latLng, Uri uri) {
        try {
            Bitmap scaled = Utils.getScaledBitmap(getApplicationContext(), uri, MAX_IMAGE_SIZE);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(scaled)));
            // add marker and its uri to the map
            images.put(marker, uri);
            MarkerEntity entity = createEntityFromMarker(marker);
            // after this line entity will have an id
            markerEntityDAO.save(entity);
            entities.put(marker, entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreMarker(MarkerEntity entity) {
        LatLng latLng = new LatLng(entity.getLat(), entity.getLng());
        Uri uri = Uri.parse(entity.getUri());
        try {
            Bitmap scaled = Utils.getScaledBitmap(getApplicationContext(), uri, MAX_IMAGE_SIZE);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(scaled)));
            // add marker and its uri to the map
            images.put(marker, uri);
            entities.put(marker, entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private MarkerEntity createEntityFromMarker(Marker marker) {
        MarkerEntity entity = new MarkerEntity();
        LatLng position = marker.getPosition();
        if (position != null) {
            entity.setLat(position.latitude);
            entity.setLng(position.longitude);
        }
        Uri uri = images.get(marker);
        entity.setUri(uri.toString());
        return entity;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final CharSequence[] items = {LABEL_OPEN, LABEL_DELETE, LABEL_CANCEL};
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Select option");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(LABEL_OPEN)) {
                    openMarkerImage(marker);
                } else if (items[item].equals(LABEL_DELETE)) {
                    dropMarker(marker);
                } else if (items[item].equals(LABEL_CANCEL)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
        return false;
    }

    private void dropMarker(final Marker marker) {
        MarkerEntity entity = entities.get(marker);
        markerEntityDAO.delete(entity);
        images.remove(entity);
        marker.remove();
    }

    private void openMarkerImage(Marker marker) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = images.get(marker);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
}
