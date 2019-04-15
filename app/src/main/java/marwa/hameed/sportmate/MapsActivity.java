package marwa.hameed.sportmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private GoogleMap mMap;

    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    MaterialRippleLayout get_my_location_mrl,estimate_distance_mrl;

    LatLng latLng_from,latLng_to;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        get_my_location_mrl  =findViewById(R.id.get_my_location_mrl);
        estimate_distance_mrl = findViewById(R.id.estimate_distance_mrl);

        get_my_location_mrl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getLocation();
            }
        });

        buildGoogleAPIClient();

        estimate_distance_mrl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (latLng_from == null || latLng_to == null)
                {
                    Toast.makeText(getApplicationContext(), "please pick locations firstly", Toast.LENGTH_SHORT).show();
                    return;
                }

                float[] results = new float[1];
                Location.distanceBetween(latLng_from.latitude, latLng_from.longitude,
                        latLng_to.latitude, latLng_to.longitude, results);

                float s = results[0] * 1.8f;
                String distance = String.valueOf(s);

                Toast.makeText(getApplicationContext(), distance, Toast.LENGTH_SHORT).show();

                List<LatLng> listLocsToDraw = new ArrayList<>();

                listLocsToDraw.add(latLng_from);
                listLocsToDraw.add(latLng_to);

                PolylineOptions options = new PolylineOptions();

                options.color( Color.parseColor( "#CC0000FF" ) );
                options.width( 5 );
                options.visible( true );

                for (LatLng locRecorded : listLocsToDraw )
                {
                    options.add( new LatLng(locRecorded.latitude, locRecorded.longitude));
                }

                mMap.addPolyline(options);
            }
        });
    }

    public void getLocation ()
    {
        if (lastlocation == null)
        {
            Toast.makeText(getApplicationContext(), "please refresh your GPS and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = lastlocation.getLatitude();
        double longtude = lastlocation.getLongitude();

        LatLng myposition = new LatLng(latitude, longtude);
        //mMap.addMarker(new MarkerOptions().position(myposition).title("Me"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myposition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                if (i == 0)
                {
                    mMap.clear();
                    latLng_to = null;

                    latLng_from = latLng;
                    LatLng myposition = new LatLng(latLng_from.latitude, latLng_from.longitude);

                    String latitude = String.valueOf(latLng_from.latitude);
                    String longitude = String.valueOf(latLng_from.longitude);

                    mMap.addMarker(new MarkerOptions().position(myposition).title("To\n" + latitude + "\n" + longitude));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myposition));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    i = 1;
                } else if (i == 1)
                {
                    latLng_to = latLng;
                    LatLng myposition = new LatLng(latLng_to.latitude, latLng_to.longitude);

                    String latitude = String.valueOf(latLng_to.latitude);
                    String longitude = String.valueOf(latLng_to.longitude);

                    mMap.addMarker(new MarkerOptions().position(myposition).title("To\n" + latitude + "\n" + longitude));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myposition));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    i = 0;
                }
            }
        });
        // Add a marker in Sydney and move the camera
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastlocation = location;
    }

    protected synchronized void buildGoogleAPIClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
}
