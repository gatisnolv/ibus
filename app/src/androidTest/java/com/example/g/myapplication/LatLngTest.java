package com.example.g.myapplication;

import android.support.test.runner.AndroidJUnit4;
import com.google.android.gms.maps.model.LatLng;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)

public class LatLngTest {
    @Test
    public void LatLng() {

        double lat1=56.971977;
        double lon1=24.190068;
        double lat2=56.968288;
        double lon2=24.193106;
        LatLng loc1=new LatLng(lat1,lon1);
        LatLng loc2=new LatLng(lat2,lon2);


        float friction = 0.5f;

        double latRes = (lat2 - lat1) * friction + lat1;
        double lngRes = (lon2 - lon1) * friction + lon1;


        LatLng latlng = new LatLng( latRes , lngRes);


        assertEquals(latlng, MainActivity.interpolate(friction,loc1,loc2));
    }
}