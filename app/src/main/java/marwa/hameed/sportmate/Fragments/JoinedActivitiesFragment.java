package marwa.hameed.sportmate.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import marwa.hameed.sportmate.Model.ActivityModel;
import marwa.hameed.sportmate.Model.JoinModel;
import marwa.hameed.sportmate.R;
import marwa.hameed.sportmate.StartActivity;

public class JoinedActivitiesFragment extends Fragment
{
    View view;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<ActivityModel, ActivityViewHolder> firebaseRecyclerAdapter;

    RotateLoading rotateLoading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.joined_activities_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerview);
        rotateLoading = view.findViewById(R.id.rotateloading);

        rotateLoading.start();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        allActivities(getUID());
    }

    private void allActivities(String key)
    {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("MineActivities")
                .child(key)
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

                holder.BindPlaces(model);
            }

            @NonNull
            @Override
            public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
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

        ActivityViewHolder(View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name_txt);
            type = itemView.findViewById(R.id.type_txt);
            date = itemView.findViewById(R.id.date_txt);
            time = itemView.findViewById(R.id.time_txt);
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
