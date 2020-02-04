package com.example.owen_.assignment3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,GoogleMap.OnCameraMoveListener {

    private GoogleMap mMap;
    MarkerOptions markerOptions;
    Boolean GpsStatus;
    private LocationRequest lr;
    private GoogleApiClient gac;
    private Location mLastLocation;
    private static int update_interval = 5000;
    private static int fastest_interval = 3000;
    private static int Displacement = 10;
    Boolean getstatus;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    LocationManager locationManager;
    private static final int MY_PER_REQ = 7000;
    CameraPosition cmPosition;
    SupportMapFragment mapFragment;
    ImageView im;
    Button done;
    ImageView mf;
    private LocationCallback mLocationCallback;
    Runnable runnable;
    Handler handler;
    double latitude,longitude;
    StringBuilder strBuilder;
    FileOutputStream out;
    Calendar calander;
    SimpleDateFormat simpledateformat;
    String Date,currenttime;
    int speed=0;
    float distance;
    Location locationcurrent,locationb;
    long diff,diffmins,diffsec;
    Date d1,d2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        getstatus = GPSStatus();

        /*
         * Set up image views and button*/
        im = (ImageView) findViewById(R.id.imguser);
        mf = (ImageView) findViewById(R.id.mapfocus);
        done = (Button) findViewById(R.id.Done);
        done.setText("Start Tracking");
        mf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getstatus.equals(true)) {
                    displayLocationSettingsRequest(MapsActivity.this);
                }
                displaylocation();
            }
        });
        if (!getstatus.equals(true)) {
            displayLocationSettingsRequest(MapsActivity.this);
        }

        /*
         * check if user has given permissions required*/
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestpermissions();
        }


        if (checkPlayServices()) {
            buildGoogleapiclient();
            createLocationRequest();
        }

        /*
         * Update user with location data*/
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                }
                displaylocation();
            }
        };
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /*Request location data and set position update speed*/
    private void createLocationRequest() {
        lr = new LocationRequest();
        lr.setInterval(update_interval);
        lr.setFastestInterval(fastest_interval);
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setSmallestDisplacement(Displacement);
    }

    /*
    * Request data from google api*/
    protected synchronized void buildGoogleapiclient() {
        gac = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        gac.connect();
    }

    /*Check google play services to see if device is supported*/
    private boolean checkPlayServices() {
        int resultcode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultcode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode)) {
                GooglePlayServicesUtil.getErrorDialog(resultcode, this, 1).show();
            } else {
                Toast.makeText(this, "This Device is not supported. Please update Google Services", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /*check for access to permissions needed to get location*/
    private void requestpermissions() {
        ActivityCompat.requestPermissions(this , new String[]
                {
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },MY_PER_REQ);
        displaylocation();

    }

    /*Check if gps in enabled*/
    private Boolean GPSStatus() {
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return GpsStatus;
        }

     /*to check if all location settings are correct to find users position*/
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            /*
            * Check if settings are satisfied
            * the settings havent been satisfied
            * request cannot be executed
            * location settings are invalid*/
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //Log.i("", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                       // Log.i("", "Location settings are not satisfied.");

                        try {
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        }
                        catch (IntentSender.SendIntentException e) {
                         //   Log.i("", "Pending intent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                      //  Log.i("", "Location settings are inadequate");
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        displaylocation();
                        // All required changes were successfully made
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }

    }

    private void displaylocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestpermissions();
        }
        else {
            if (mLastLocation != null) {

                latitude = mLastLocation.getLatitude();
                cmPosition=mMap.getCameraPosition();
                longitude = mLastLocation.getLongitude();
                final LatLng yourlocation = new LatLng(latitude,longitude);
                im.setVisibility(View.VISIBLE);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourlocation,15.5f));
                mMap.setOnMarkerClickListener(
                        new GoogleMap.OnMarkerClickListener() {
                            boolean doNotMoveCameraToCenterMarker = true;
                            public boolean onMarkerClick(Marker marker) {
                                //Do whatever you need to do here ....
                                marker.showInfoWindow();
                                return doNotMoveCameraToCenterMarker;
                            }
                        });

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        cmPosition=mMap.getCameraPosition();
                        }
                });

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if( ActivityCompat.checkSelfPermission(MapsActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                &&  ActivityCompat.checkSelfPermission(MapsActivity.this , Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MapsActivity.this , new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            },MY_PER_REQ);
                        }

                        if(done.getText().toString().equals("Start Tracking")) {
                            locationcurrent=new Location("Point A");
                            locationcurrent=mLastLocation;
                            calander = Calendar.getInstance();
                            simpledateformat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
                            Date = simpledateformat.format(calander.getTime());
                            done.setText("Stop Tracking");
                            handler = new Handler();
                            strBuilder = new StringBuilder();
                            handler.postDelayed(runnable=new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 5 seconds
                                    startLocationUpdates();
                                    final LatLng yourlocation = new LatLng((mLastLocation.getLatitude()),(mLastLocation.getLongitude()));

                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourlocation,15.0f));
                                    strBuilder.append(mLastLocation.getLatitude()+Date + mLastLocation.getLongitude());
                                    strBuilder.append("\n");

                                    String line;

                                    /*
                                    * Create a directory to save gps data*/
                                    try {
                                        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GpsTracker/";
                                        File root = new File(rootPath);
                                        if (!root.exists()) {
                                            root.mkdirs();
                                        }
                                        File f = new File(rootPath +""+".txt");
                                        if(!f.exists()) {
                                            f.createNewFile();
                                        }
                                        out = new FileOutputStream(f);
                                        out.write(strBuilder.toString().getBytes());
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    handler.postDelayed(runnable, 5000);

                                }
                            }, 0);
                        }
                        else {
                            try {
                                out.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            calander = Calendar.getInstance();
                            simpledateformat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
                            currenttime = simpledateformat.format(calander.getTime());

                            try {
                                d2=simpledateformat.parse(currenttime);
                                d1=simpledateformat.parse(Date);

                                //time in ms
                                diff = d2.getTime() - d1.getTime();
                                diffmins = diff / (60 * 1000) % 24;
                                diffsec = diff / (1000) % 24;
                                }

                            catch (Exception e) {
                                // TODO: handle exception
                            }


                            /*
                            * to get speed*/
                            locationb=new Location("PointB");
                            locationb=mLastLocation;
                            distance = locationcurrent.distanceTo(locationb);
                            speed = (int) (distance/diffsec);

                            long totaltime=locationcurrent.getTime()-locationb.getTime();
                            done.setText("Start Tracking");
                            handler.removeCallbacks(runnable);
                            Intent i=new Intent(MapsActivity.this,Results.class);
                            i.putExtra("distance",String.valueOf(distance));
                            i.putExtra("speed",String.valueOf(speed));
                            i.putExtra("time",String.valueOf(diffmins + " minute " + diffsec));
                            //i.putExtra(time2, )
                            i.putExtra("Altitude",String.valueOf(mLastLocation.getAltitude()));
                            startActivity(i);
                        }
                    }
                });
            }
            else {
                Log.d("Error" , "Couldn't get location");
            }
        }
    }

    /*ability to move the camera*/
    @Override
    public void onCameraMove() {
        cmPosition = mMap.getCameraPosition();
        }

    /*add a marker to the map*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    /*
    * to update user location*/
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(lr, mLocationCallback,  Looper.myLooper());
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(gac!=null) {
            gac.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displaylocation();
    }

    /*These are listeners needed by the imports*/
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displaylocation();
        startLocationUpdates();
    }

   @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
