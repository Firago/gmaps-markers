package com.dfirago.maps.dao;

import com.dfirago.maps.model.MarkerEntity;

import java.util.List;

/**
 * Created by dmfi on 28/06/2016.
 */
public interface MarkerEntityDAO {

    List<MarkerEntity> list();

    MarkerEntity save(MarkerEntity marker);

    void delete(MarkerEntity marker);

}
