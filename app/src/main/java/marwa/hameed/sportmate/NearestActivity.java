package marwa.hameed.sportmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marwa.hameed.sportmate.Model.ActivityModel;

public class NearestActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    FloatingActionButton near_fab;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RotateLoading rotateLoading;

    GeoFire geoFire;
    GeoQuery geoQuery;
    int raduis = 1;
    Boolean driverfound = false;
    String driverfoundID;
    int i = 0;

    CircleImageView imageView;
    TextView name,type,date,time;
    MaterialRippleLayout details;
    CardView linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest);

        buildGoogleAPIClient();

        near_fab = findViewById(R.id.near_fab);
        rotateLoading = findViewById(R.id.rotateloading);

        imageView = findViewById(R.id.image);
        name = findViewById(R.id.name_txt);
        type = findViewById(R.id.type_txt);
        date = findViewById(R.id.date_txt);
        time = findViewById(R.id.time_txt);
        details = findViewById(R.id.details);
        linearLayout = findViewById(R.id.lin);

        linearLayout.setVisibility(View.GONE);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        near_fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getLocation();
            }
        });
    }

    private void getClosestDriver(final Location location)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("ActivitiesLocations");

        geoFire = new GeoFire(databaseReference);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), raduis);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener()
        {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if (!driverfound || i ==0)
                {
                    driverfound = true;
                    driverfoundID = key;
                    Toast.makeText(getApplicationContext(), "Found ...", Toast.LENGTH_SHORT).show();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                    reference.child("Activites").child(driverfoundID).addListenerForSingleValueEvent(
                            new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    // Get user value
                                    ActivityModel activityModel = dataSnapshot.getValue(ActivityModel.class);

                                    linearLayout.setVisibility(View.VISIBLE);

                                    name.setText(activityModel.getName());
                                    type.setText(activityModel.getType());
                                    date.setText(activityModel.getDate());
                                    time.setText("at " + activityModel.getTime());

                                    Picasso.get()
                                            .load(activityModel.getImageurl())
                                            .placeholder(R.drawable.addphoto)
                                            .error(R.drawable.addphoto)
                                            .into(imageView);

                                    rotateLoading.stop();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {
                                    Toast.makeText(getApplicationContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                                    rotateLoading.stop();
                                }
                            });

                    rotateLoading.stop();
                    i =0;
                }
            }

            @Override
            public void onKeyExited(String key)
            {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location)
            {

            }

            @Override
            public void onGeoQueryReady()
            {
                if (!driverfound)
                {
                    if (raduis >= 8)
                    {
                        Toast.makeText(getApplicationContext(), "No Activities Nearby You ...", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(intent);
                    } else
                    {
                        raduis = raduis + 1;
                        getClosestDriver(location);
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

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

        rotateLoading.start();

        getClosestDriver(lastlocation);
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
