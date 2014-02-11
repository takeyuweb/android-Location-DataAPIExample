package com.takeyuweb.dataapiexample;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by uzuki05 on 14/01/25.
 */
public class MTEntry {
    public int id;
    public String title;
    public double lat;
    public double lng;
    public String excerpt;
    public String permalink;

    public LatLng getLatLng() {
        LatLng latLng = new LatLng(lat, lng);
        return latLng;
    }
}
