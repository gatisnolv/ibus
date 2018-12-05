package com.example.g.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.*;
//import org.apache.commons.csv.*;
import org.onebusaway.gtfs.impl.*;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.*;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import android.content.res.Resources;

import de.siegmar.fastcsv.reader.*;

import java.util.zip.*;
import java.util.*;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Riga) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(56.9496, 24.1052);
    private static final int DEFAULT_ZOOM = 10;
    private static final int PERMISSIONS_GRANTED_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    //-----------------
    private GTFS gtfs;
    private boolean locationPermissionGrantedPreviously = false;
    Marker stopMarker;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
////        final TextView helloTextView = findViewById(R.id.text_view_id);
////        helloTextView.setText(R.string.stops);
//        System.out.println("something");
//        gtfs = new GTFS(this);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int busRouteIdOffset = 100;
        int trolleyRouteIdOffset = 200;
        int tramRouteIdOffset = 300;
        getMenuInflater().inflate(R.menu.user_options_menu, menu);
        for (int i = 0; i < 20; i++) {
            MenuItem item = menu.getItem(0).getSubMenu().getItem(0).getSubMenu().add(Menu.NONE, busRouteIdOffset + i, i, "this is a long string");
//            Log.d("sometag","a"+Integer.toString(item.getItemId()));
        }
//        Log.d("sometag",getString(menu.getItem(0).getItemId()));
//        Log.d("sometag",menu.getItem(0).getTitle().toString()+menu.getItem(0).getSubMenu().getItem(0).hasSubMenu());

        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_name:
//                item.ge
                Log.d("sometag", "this happened");
                break;
            case R.id.bus_menu:
                Log.d("sometag", "this2 happened");
                break;
            case R.id.trolley_menu:
                Log.d("sometag", "this3 happened");
                break;
            case R.id.tram_menu:
                Log.d("sometag", "this4 happened");
                break;
            default:
                Log.d("sometag", "id: " + itemId);
                if (itemId >= 100 && itemId < 400) {
                    Log.d("sometag", "this");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
                }

        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        try {
            // Customise the styling of the base map, hiding transit stops, which we cannot bind info windows to
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//
//            @Override
//            // Return null here, so that getInfoContents() is called next.
//            public View getInfoWindow(Marker arg0) {
//                return null;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//                // Inflate the layouts for the info window, title and snippet.
//                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
//                        (FrameLayout) findViewById(R.id.map), false);
//
//                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
//                title.setText(marker.getTitle());
//
//                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
//                snippet.setText(marker.getSnippet());
//
//                return infoWindow;
//            }
//        });

        // Prompt the user for permission.
        getLocationPermission();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

//        final Calendar calendarInstance=Calendar.getInstance();
//        BroadcastReceiver minutesReceiver=new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                stopMarker.setTitle(Integer.toString(calendarInstance.get(Calendar.HOUR))+":"
//                        +Integer.toString(calendarInstance.get(Calendar.MINUTE)));
//                stopMarker.showInfoWindow();
//            }
//        };
//
//        registerReceiver(minutesReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));


    }

    public void findClosestStop(View view) {
        List<LatLng> stopList = new ArrayList<>();
        stopList.add(new LatLng(56.968985, 24.188312));
        stopList.add(new LatLng(56.969662, 24.184618));
        stopList.add(new LatLng(56.977891, 24.182042));

        LatLng currentLoc = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        LatLng closestStop = currentLoc; //iffy initialization
        float closestDistance = Integer.MAX_VALUE;

        for (LatLng stop : stopList) {
            float distance = (float) SphericalUtil.computeDistanceBetween(stop, currentLoc);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestStop = stop;
            }
        }

        //move to midpoint between currentLoc, and closest stop
        LatLng midpoint = SphericalUtil.interpolate(currentLoc, closestStop, 0.5);

        mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current loc"));
        stopMarker = mMap.addMarker(new MarkerOptions().position(closestStop).title("Closest stop"));
//        stopMarker
        mMap.animateCamera(CameraUpdateFactory.newLatLng(midpoint));
        updateMarkerInfo();


    }

    public void updateHere(int h, int m, int s) {
        stopMarker.setTitle(h + ":" + m + ":" + s);
        if (stopMarker.isInfoWindowShown()) {
            stopMarker.showInfoWindow();

        }
    }

    public void updateMarkerInfo() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {//fix to have only one such thread running at a time
            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
//                int mYear = c.get(Calendar.YEAR);
//                int mMonth = c.get(Calendar.MONTH);
//                int mDay = c.get(Calendar.DAY_OF_MONTH);
                final int mHour = c.get(Calendar.HOUR_OF_DAY);
                final int mMinute = c.get(Calendar.MINUTE);
                final int mSecond = c.get(Calendar.SECOND);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateHere(mHour, mMinute, mSecond);
                        Log.d("freq","test");
                    }
                });

//                stopMarker.setTitle(Integer.toString(mHour) + ":" + Integer.toString(mMinute) + ":" + Integer.toString(mSecond));
//                stopMarker.showInfoWindow();
            }

        }, 0, 1000);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (locationPermissionGrantedPreviously) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), PERMISSIONS_GRANTED_ZOOM));
                            } else {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), PERMISSIONS_GRANTED_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } else {//if user doesn't grant permission, move camera to center on Riga
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            locationPermissionGrantedPreviously = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
        getDeviceLocation();//it makes sense for this to be here, because otherwise the camera location will not be repositioned
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
//                getLocationPermission();//removing this should enable preventing infinite loop
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


}
