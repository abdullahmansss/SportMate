package marwa.hameed.sportmate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.balysv.materialripple.MaterialRippleLayout;

public class CreateActivity extends AppCompatActivity
{
    MaterialRippleLayout select_location_mrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        select_location_mrl = findViewById(R.id.select_location_mrl);

        select_location_mrl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
