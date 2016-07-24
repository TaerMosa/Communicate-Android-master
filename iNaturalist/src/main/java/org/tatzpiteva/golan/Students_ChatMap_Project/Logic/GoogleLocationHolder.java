package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

/**
 * Created by ta2er Mosa.
 */
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;


import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import org.inaturalist.android.ActivityHelper;
import org.inaturalist.android.R;


public class GoogleLocationHolder implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
        GpsStatus.Listener  {

    public static GoogleLocationHolder instance = null;
    private Context ctx = null;
    static LocationRequest locationRequest;
    // changed to google api client from google play servce 7+
    private GoogleApiClient locationClient;
    private LatLng currentLocation = null;
    private List<GoogleLocationListener> listeners = new ArrayList<GoogleLocationListener>();
    private LocationManager locationManager = null;
    private ActivityHelper mHelper;

    public static GoogleLocationHolder getInstance() {
        if (instance == null) {
            instance = new GoogleLocationHolder();

        }
        return instance;
    }

    public void init(Context context) {

        this.ctx = context;


        try {
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            locationManager.addGpsStatusListener(this);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                alertGpsDisabled();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        locationClient =  new GoogleApiClient.Builder(ctx)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        locationRequest = new LocationRequest();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 1 seconds
        locationRequest.setInterval(1000);
        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(1000);
        locationClient.connect();



    }

    public static void releaseInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    public void subscribeForLocation(GoogleLocationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribeForLocationn(GoogleLocationListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        setGoogleLocation(new LatLng(loc.getLatitude(), loc.getLongitude()));

        for (GoogleLocationListener o : listeners) {
            try {
                o.GoogleLocationChange(currentLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(ctx,"Done3",Toast.LENGTH_SHORT);
    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient,locationRequest,this);
        //  locationClient.requestLocationUpdates(locationRequest, this);

    }


    public LatLng getGoogleLocation() {
        return currentLocation;
    }

    public void setGoogleLocation(LatLng googleLocation) {
        this.currentLocation = googleLocation;
    }

    public List<GoogleLocationListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<GoogleLocationListener> listeners) {
        this.listeners = listeners;
    }

    private void alertGpsDisabled() {
        /*
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Your GPS is disabled, Do you want to enable it?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        ctx.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();

            }
        });
        final AlertDialog alert = builder.create();
        alert.show();*/
        mHelper = new ActivityHelper(ctx);
        mHelper.confirm(R.string.Locationisdisabled,R.string.enableGPS,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,final int id) {
                        ctx.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                }
        );

    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                // GPS PRIVDER ENABLED"
                // no need to to any thing
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                // "GPS PROVIDER DISABLED"
                alertGpsDisabled();
                break;
        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(ctx,"Done1",Toast.LENGTH_SHORT);

    }

   /* @Override
    public void onDisconnected() {
        Toast.makeText(ctx,"Done2",Toast.LENGTH_SHORT);
        // TODO Auto-generated method stub

    }*/

}
