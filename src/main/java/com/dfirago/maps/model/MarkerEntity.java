package com.dfirago.maps.model;

/**
 * Created by dmfi on 28/06/2016.
 */
public class MarkerEntity {

    public static final String KEY_TABLE_NAME = "markers";
    public static final String KEY_ID = "id";
    public static final String KEY_POSITION_LAT = "position_lat";
    public static final String KEY_POSITION_LNG = "position_lng";
    public static final String KEY_IMAGE_URI = "image_uri";

    private long id;
    private double lat;
    private double lng;
    private String uri;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
