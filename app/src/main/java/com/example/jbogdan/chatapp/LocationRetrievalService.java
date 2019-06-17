package com.example.jbogdan.chatapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationRetrievalService extends Service {

    private static final String TAG = "LOCATIONSERVICETAG";
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private LocationManager locationManager = null;
    private LocationListener networkLocationListener;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;
    private DatabaseReference mDatabase;

    public LocationRetrievalService() {
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            // get Database reference
            mDatabase = FirebaseDatabase.getInstance().getReference();

            // Acquire a reference to the system Location Manager
            if (locationManager == null)
                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            System.out.println("isGPSEnabled:" + isGPSEnabled);
            System.out.println("isNetworkEnabled:" + isNetworkEnabled);

            // Define a listener that responds to location updates
            networkLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    Log.e(TAG, "onLocationChanged: " + location);
                    String message = String.format("New Location \n Longitude: %1$s \n Latitude: %2$s", location.getLongitude(), location.getLatitude());
                    Toast.makeText(LocationRetrievalService.this, message, Toast.LENGTH_LONG).show();

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    if (mAuth != null) {
                        Log.e("mAuthNOTNULL", "mAuthNOTNULL");
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null) {
                            Log.e("SENDTODATABASE", "SENDTODATABASE");
                            writeData(currentUser, location.getLongitude(), location.getLatitude());
                        }
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.e(TAG, "onStatusChanged " + provider);
                    Toast.makeText(LocationRetrievalService.this, "Provider status changed", Toast.LENGTH_LONG).show();
                }

                public void onProviderEnabled(String provider) {
                    Log.e(TAG, "onProviderEnabled: " + provider);
                }

                public void onProviderDisabled(String provider) {
                    Log.e(TAG, "onProviderDisabled: " + provider);
                }
            };

            // Register the listener with the Location Manager to receive location updates
            if (isNetworkEnabled) {
                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, networkLocationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (location != null) {
                        String message = String.format(
                                "Current Location \n Longitude: %1$s \n Latitude: %2$s",
                                location.getLongitude(), location.getLatitude()
                        );
                        Toast.makeText(LocationRetrievalService.this, message,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (SecurityException e) {
                    Log.i(TAG, "request location update", e);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "GPS_PROVIDER missing", e);
                }
            } else if (isGPSEnabled) { // just in case network provider doesn't work
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, networkLocationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (location != null) {
                        String message = String.format(
                                "Current Location \n Longitude: %1$s \n Latitude: %2$s",
                                location.getLongitude(), location.getLatitude()
                        );
                        Toast.makeText(LocationRetrievalService.this, message,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (SecurityException e) {
                    Log.i(TAG, "request location update", e);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "GPS_PROVIDER missing", e);
                }
            }
        }
    }

    private void writeData(FirebaseUser currentUser, double longitude, double latitude) {
        User user = new User(currentUser.getDisplayName(), longitude, latitude, System.currentTimeMillis() / 1000);

        mDatabase.child("users").child(currentUser.getUid()).setValue(user);
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_LONG).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_LONG).show();
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(networkLocationListener);
        }
    }
}
