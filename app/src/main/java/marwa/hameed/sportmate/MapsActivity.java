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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import marwa.hameed.sportmate.Model.ActivityModel;
import marwa.hameed.sportmate.Model.UserModel;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private GoogleMap mMap;

    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    Button get_my_location_mrl,estimate_distance_mrl,create_activity;
    Spinner type_spinner;
    EditText time_field,date_field;
    String type,time,date,name,imageurl;


    LatLng latLng_from,latLng_to;
    int i = 0;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    BigDecimal result;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        returndata();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        get_my_location_mrl  =findViewById(R.id.get_my_location_mrl);
        estimate_distance_mrl = findViewById(R.id.estimate_distance_mrl);
        create_activity = findViewById(R.id.create_activity_btn);
        type_spinner = findViewById(R.id.type_spinner);
        time_field = findViewById(R.id.time_field);
        date_field = findViewById(R.id.date_field);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.activities, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        type_spinner.setAdapter(adapter1);

        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                type = String.valueOf(parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

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

                float s = (results[0] * 1.8f) / 1000;
                String distance = String.valueOf(s);

                //Toast.makeText(getApplicationContext(), distance, Toast.LENGTH_SHORT).show();

                result = round(s,2);

                estimate_distance_mrl.setText(result + " K.M");

                Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(
                                new LatLng(latLng_from.latitude, latLng_from.longitude),
                                new LatLng(latLng_to.latitude, latLng_to.longitude)));

                /*List<LatLng> listLocsToDraw = new ArrayList<>();

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

                mMap.addPolyline(options);*/
            }
        });

        create_activity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                time = time_field.getText().toString();
                date = date_field.getText().toString();

                if (latLng_to == null)
                {
                    Toast.makeText(getApplicationContext(), "please choose activity location", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (result == null)
                {
                    Toast.makeText(getApplicationContext(), "please estimate activity distance", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (type.equals("Select Activity Type"))
                {
                    Toast.makeText(getApplicationContext(), "please choose activity type", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(time))
                {
                    Toast.makeText(getApplicationContext(), "please choose activity time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(date))
                {
                    Toast.makeText(getApplicationContext(), "please choose activity date", Toast.LENGTH_SHORT).show();
                    return;
                }
                //private String distance,name,imageurl,type,time,date;
                //private double latitude_from,longitude_from,latitude_to,longitude_to;
                createActivity(result + "",name,imageurl,type,time,date,latLng_from.latitude,latLng_from.longitude,latLng_to.latitude,latLng_to.longitude);
            }
        });
    }

    private void createActivity(String s, String name, String imageurl, String type, String time, String date, double latitude, double longitude, double latitude1, double longitude1)
    {
        ActivityModel activityModel = new ActivityModel(s,name,imageurl,type,time,date,latitude,longitude,latitude1,longitude1);

        String key = databaseReference.child("Activites").push().getKey();
        databaseReference.child("Activites").child(key).setValue(activityModel);
        databaseReference.child("UsersActivities").child(getUID()).child(key).setValue(activityModel);

        if (lastlocation != null && key != null)
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("ActivitiesLocations");

            GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(key, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener()
            {
                @Override
                public void onComplete(String key, DatabaseError error)
                {

                }
            });
        }

        Toast.makeText(getApplicationContext(), "Activity Created ..", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
    }

    /*private void createActivity(String s, String name, String imageurl, String type, String time, String date, double longitude, double latitude, double latitude1, double longitude1)
    {
        ActivityModel activityModel = new ActivityModel(s,name,imageurl,type,time,date,longitude,latitude,latitude1,longitude1);

        String key = databaseReference.child("Activites").push().getKey();
        databaseReference.child("Activites").child(key).setValue(activityModel);
        databaseReference.child("UsersActivities").child(getUID()).child(key).setValue(activityModel);

        if (lastlocation != null && key != null)
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("ActivitiesLocations");

            GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(key, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()), new GeoFire.CompletionListener()
            {
                @Override
                public void onComplete(String key, DatabaseError error)
                {

                }
            });
        }

        Toast.makeText(getApplicationContext(), "Activity Created ..", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
    }*/

    public static BigDecimal round(float d, int decimalPlace)
    {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                Toast.makeText(MapsActivity.this, "" + marker.getPosition() , Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                if (i == 0)
                {
                    mMap.clear();
                    latLng_to = null;
                    estimate_distance_mrl.setText("estimate");

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

    public void returndata()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        final String userId = user.getUid();

        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Get user value
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);

                        name = userModel.getName();
                        imageurl = userModel.getImageurl();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getApplicationContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getUID()
    {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}
