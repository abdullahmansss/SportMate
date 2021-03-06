package marwa.hameed.sportmate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import de.hdodenhof.circleimageview.CircleImageView;
import marwa.hameed.sportmate.Fragments.JoinedActivitiesFragment;
import marwa.hameed.sportmate.Fragments.MyActivitiesFragment;
import marwa.hameed.sportmate.Model.ActivityModel;
import marwa.hameed.sportmate.Model.JoinModel;

public class CreateActivity extends AppCompatActivity
{
    String KEY;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<JoinModel, ActivityViewHolder> firebaseRecyclerAdapter;

    RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        KEY = getIntent().getStringExtra(MyActivitiesFragment.EXTRA);

        recyclerView = findViewById(R.id.recyclerview);
        rotateLoading = findViewById(R.id.rotateloading);

        rotateLoading.start();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        allActivities(KEY);
    }

    private void allActivities(String key)
    {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("JoinedActivities")
                .child(key)
                .limitToLast(50);

        FirebaseRecyclerOptions<JoinModel> options =
                new FirebaseRecyclerOptions.Builder<JoinModel>()
                        .setQuery(query, JoinModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<JoinModel, ActivityViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ActivityViewHolder holder, int position, @NonNull final JoinModel model)
            {
                rotateLoading.stop();

                final String key = getRef(position).getKey();

                holder.BindPlaces(model);
            }

            @NonNull
            @Override
            public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item2, parent, false);
                return new ActivityViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        rotateLoading.stop();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView imageView;
        TextView name;

        ActivityViewHolder(View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name_txt);
        }

        void BindPlaces(final JoinModel activityModel)
        {
            name.setText(activityModel.getName());

            Picasso.get()
                    .load(activityModel.getImageurl())
                    .placeholder(R.drawable.addphoto)
                    .error(R.drawable.addphoto)
                    .into(imageView);
        }
    }

    private String getUID()
    {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
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
}
