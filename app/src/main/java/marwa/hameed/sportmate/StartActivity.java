package marwa.hameed.sportmate;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import marwa.hameed.sportmate.Model.UserModel;

public class StartActivity extends AppCompatActivity
{
    CircleImageView circleImageView;
    TextView welcome_txt;
    FloatingActionButton create_activity_fab;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        circleImageView = findViewById(R.id.profile_image);
        welcome_txt = findViewById(R.id.welcome_txt);
        create_activity_fab = findViewById(R.id.create_activity_fab);

        returndata(getUID());

        create_activity_fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(intent);
            }
        });
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

                        Picasso.get()
                                .load(userModel.getImageurl())
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
