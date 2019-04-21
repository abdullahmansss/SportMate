package marwa.hameed.sportmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import de.hdodenhof.circleimageview.CircleImageView;
import marwa.hameed.sportmate.Model.ActivityModel;
import marwa.hameed.sportmate.Model.JoinModel;
import marwa.hameed.sportmate.Model.UserModel;

public class StartActivity extends AppCompatActivity
{
    CircleImageView circleImageView;
    TextView welcome_txt;
    FloatingActionButton create_activity_fab;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<ActivityModel, ActivityViewHolder> firebaseRecyclerAdapter;

    RotateLoading rotateLoading;

    String name,imageurl;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }

        circleImageView = findViewById(R.id.profile_image);
        welcome_txt = findViewById(R.id.welcome_txt);
        create_activity_fab = findViewById(R.id.create_activity_fab);

        recyclerView = findViewById(R.id.recyclerview);
        rotateLoading = findViewById(R.id.rotateloading);

        rotateLoading.start();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        returndata(getUID());

        create_activity_fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(StartActivity.this, circleImageView);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.pop_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.near:
                                Intent intent = new Intent(getApplicationContext(), NearestActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.my_activity:
                                Intent intent2 = new Intent(getApplicationContext(), MyActivity.class);
                                startActivity(intent2);
                                return true;
                            case R.id.sign_out:
                                FirebaseAuth.getInstance().signOut();

                                Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent3);
                                return true;
                            default:
                                return true;
                        }
                    }});
                popup.show(); //showing popup menu
            }
        });

        allActivities();
    }

    public void returndata(String key)
    {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("Users").child(key).addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Get user value
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);

                        name = userModel.getName();
                        imageurl = userModel.getImageurl();

                        Picasso.get()
                                .load(imageurl)
                                .placeholder(R.drawable.addphoto)
                                .error(R.drawable.addphoto)
                                .into(circleImageView);

                        welcome_txt.setText("Let's go, " + userModel.getName() + " !!");
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

    private void allActivities()
    {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Activites")
                .limitToLast(50);

        FirebaseRecyclerOptions<ActivityModel> options =
                new FirebaseRecyclerOptions.Builder<ActivityModel>()
                        .setQuery(query, ActivityModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ActivityModel, ActivityViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ActivityViewHolder holder, int position, @NonNull final ActivityModel model)
            {
                rotateLoading.stop();

                final String key = getRef(position).getKey();

                holder.join.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        JoinModel joinModel = new JoinModel(name,imageurl);

                        databaseReference.child("JoinedActivities").child(key).child(getUID()).setValue(joinModel);
                        databaseReference.child("MineActivities").child(getUID()).child(key).setValue(model);
                        Toast.makeText(getApplicationContext(), "Joined ..", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.BindPlaces(model);
            }

            @NonNull
            @Override
            public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_item, parent, false);
                return new ActivityViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        rotateLoading.stop();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView imageView;
        TextView name,type,date,time;
        MaterialRippleLayout details;
        Button join;

        ActivityViewHolder(View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name_txt);
            type = itemView.findViewById(R.id.type_txt);
            date = itemView.findViewById(R.id.date_txt);
            time = itemView.findViewById(R.id.time_txt);
            details = itemView.findViewById(R.id.details);
            join = itemView.findViewById(R.id.join_btn);
        }

        void BindPlaces(final ActivityModel activityModel)
        {
            name.setText(activityModel.getName());
            type.setText(activityModel.getType());
            date.setText(activityModel.getDate());
            time.setText("at " + activityModel.getTime());

            Picasso.get()
                    .load(activityModel.getImageurl())
                    .placeholder(R.drawable.addphoto)
                    .error(R.drawable.addphoto)
                    .into(imageView);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (firebaseRecyclerAdapter != null)
        {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (firebaseRecyclerAdapter != null)
        {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    private long exitTime = 0;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000)
        {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finishAffinity();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed()
    {
        doExitApp();
    }
}
