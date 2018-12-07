package com.example.g.myapplication;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
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
import android.util.Property;
import android.view.*;
import android.graphics.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.*;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import com.google.maps.*;
import com.example.g.myapplication.GTFS.*;

import android.content.res.Resources;

import de.siegmar.fastcsv.reader.*;

import java.time.LocalTime;
import java.util.zip.*;
import java.util.*;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnInfoWindowCloseListener, OnMarkerClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Riga) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(56.9496, 24.1052);//Riga coordinates
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
    private Marker stopMarker;
    private Marker currentLocationMarker;
    Timer timer;
    TimerTask timerTask;
    Context mContext = this;
    ObjectAnimator animator;
    List<LatLng> shape = new ArrayList<>();
    Calendar cal=Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gtfs = new GTFS(this);
        List<Route> tramRoutes=new ArrayList<>(gtfs.getTramRoutes());
        Collections.sort(tramRoutes);

        for(Route tramRoute:tramRoutes){
//            Log.d("tram r",tramRoute.getShortName()+" "+tramRoute.getLongName());
            for(Trip trip:tramRoute.getTrips()){
//                Log.d("tram r ","\t Direction:"+trip.getHeadsign()+" "+trip.getTripId());
                if(trip.operatesOnDay(cal.get(Calendar.DAY_OF_WEEK))){
                    Log.d("today","\t Direction:"+trip.getHeadsign()+" "+trip.getTripId());
                    LocalTime testTime=LocalTime.of(12,0,0);
                    if(trip.operatesAtTime(testTime)){
                        Log.d("tram r","operates at time");
                    }else{
                        Log.d("tram r","does not operate at time");
                    }

                }
            }

        }

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
     * Manipulates the map when it's available. This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Prompt the user for permission or determine that permission has been previously granted
        getLocationPermission();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
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
        });

        animation();

    }

    public void animation() {
        LatLng maja = new LatLng(56.971977, 24.190068);
        Marker marker = mMap.addMarker(new MarkerOptions().position(maja).title("markeris"));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        final List<LatLng> locList = new ArrayList<>();
        locList.add(new LatLng(56.971977, 24.190068));
        locList.add(new LatLng(56.968288, 24.193106));
        locList.add(new LatLng(56.968288, 24.184742));
        locList.add(new LatLng(56.968288, 24.174742));
        locList.add(new LatLng(56.968288, 24.164742));
        mMap.addPolyline(new PolylineOptions().addAll(locList).color(0x70FF0000).width(6));

        class Listener implements Animator.AnimatorListener {
            Marker marker;
            Iterator<LatLng> locIterator;

            Listener(Marker marker, Iterator<LatLng> locIterator) {
                this.marker = marker;
                this.locIterator = locIterator;
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (locIterator.hasNext()) {
                    animateMarker(marker, locIterator.next());
                    Log.d("TAG", "here");
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }
        Iterator<LatLng> locIterator = locList.iterator();
        Animator.AnimatorListener listener = new Listener(marker, locIterator);
        animateMarker(marker, locIterator.next());
        animator.addListener(listener);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(stopMarker)) {
            Log.d("sometag", "stopMark");
            if (!stopMarker.isInfoWindowShown()) {
                updateMarkerInfo();
            }
//            InfoWindow infoWindow=new
//            marker.set
        } else if (marker.equals(currentLocationMarker)) {
            Log.d("sometag", "currLocMark");
        }
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {//when pressing marker with open infowindow, close event appears to always happen before reopen, so handling the start/stop of timer should not be a problem
        if (marker.equals(stopMarker)) {
            Log.d("sometag", "closeEvent");
//            runMarkerUpdateTimer = false;
            TimerTask prevTimerTask = timerTask;
            timerTask = null;//doing this in the timertask itself is does not happen early enough, so the same task is scheduled again and we get an exception
            prevTimerTask.cancel();
        }
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

        if (currentLocationMarker == null) {//first creation of marker
            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current loc"));
        } else {//reuse same marker
            currentLocationMarker.setPosition(currentLoc);
        }

        if (stopMarker == null) {
            stopMarker = mMap.addMarker(new MarkerOptions().position(closestStop).title("Closest stop"));
        } else {
            stopMarker.setPosition(closestStop);//TODO set relevant position instead of hardcoded
        }
        //move to midpoint between currentLoc, and closest stop
        LatLng midpoint = SphericalUtil.interpolate(currentLoc, closestStop, 0.5);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(midpoint));
        animator.end();
    }

    public void updateMarkerInfo() {
//        runMarkerUpdateTimer = true;
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {

            timerTask = new TimerTask() {//fix to have only one such thread running at a time
                @Override
                public void run() {
                    Calendar cal = Calendar.getInstance();
                    final int hour = cal.get(Calendar.HOUR_OF_DAY);
                    final int minute = cal.get(Calendar.MINUTE);
                    final int second = cal.get(Calendar.SECOND);
//                    if (!runMarkerUpdateTimer) {
//                        TimerTask localTimerTaskForCancellation=timerTask;
//                        timerTask=null;
//                        localTimerTaskForCancellation.cancel();//TODO fix cancelling prev timertask
//                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (stopMarker.isInfoWindowShown()) {

                                stopMarker.setTitle("Pieturas nosaukums");
                                stopMarker.setSnippet(hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second + "\n" +
                                        hour + ":" + minute + ":" + second);
                                stopMarker.showInfoWindow();

                            }
                            Log.d("freq", "test");
                        }
                    });
                }

            };
        }
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {//TODO make appropriate name
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
                        } else {//getLastLocation task unsuccesful
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

    public static LatLng interpolate(float fraction, LatLng a, LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lng = (b.longitude - a.longitude) * fraction + a.longitude;
        return new LatLng(lat, lng);
    }

    void animateMarker(Marker marker, LatLng finalPosition) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        if (animator == null) {
            animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
            animator.setInterpolator(new LinearInterpolator());
        } else {
            animator.setObjectValues(finalPosition);
        }
        animator.setDuration(2000);
        animator.start();
    }

}
