package marwa.hameed.sportmate;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    Button getstarted,signin;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        getstarted = findViewById(R.id.getstarted_btn);
        signin = findViewById(R.id.signin_btn);

        getstarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
            }
        });
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
