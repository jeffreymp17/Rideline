package com.ridelineTeam.application.rideline.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.util.helpers.DateTimeAndStringHelper;
import com.ridelineTeam.application.rideline.view.RideDetailActivity;
import com.ridelineTeam.application.rideline.model.Ride;
import com.ridelineTeam.application.rideline.util.enums.Status;
import com.ridelineTeam.application.rideline.model.enums.Type;

import java.util.ArrayList;
import java.util.List;



public class ProfileAdapter {

    static class RideViewHolder extends RecyclerView.ViewHolder {
        private TextView status;
        private TextView typeCard;
        private ImageView rideImage;
        private CardView card;


        private RideViewHolder(View view) {
            super(view);
            status = view.findViewById(R.id.status);
            rideImage = view.findViewById(R.id.ride_image_profile);
            card=view.findViewById(R.id.card_profile);
            typeCard=view.findViewById(R.id.ride_type);
        }
    }


    public static class ProfileAdapterRecycler extends RecyclerView.Adapter<RideViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ChildEventListener childEventListener;
        private List<Ride> Rides;
        private List<String> RidesIds;
        private Activity activity;


        public ProfileAdapterRecycler(final Context context, DatabaseReference reference,
                                   Activity activity, Query query) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.Rides = new ArrayList<>();
            this.RidesIds = new ArrayList<>();

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("ProfileAdapter", "onChildAdded " + dataSnapshot.getKey());

                    Ride ride = dataSnapshot.getValue(Ride.class);

                    RidesIds.add(dataSnapshot.getKey());

                    Rides.add(ride);

                    notifyItemInserted(Rides.size() - 1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Ride Ride = dataSnapshot.getValue(Ride.class);
                    String RideKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    int RideIndex = RidesIds.indexOf(RideKey);
                    if (RideIndex > -1) {
                        // Replace with the new data
                        Rides.set(RideIndex, Ride);
                        // Update the RecyclerView
                        notifyItemChanged(RideIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + RideKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("RideAdapter", "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String RideKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int RideIndex = RidesIds.indexOf(RideKey);
                    if (RideIndex > -1) {
                        // Remove data from the list
                        RidesIds.remove(RideIndex);
                        Rides.remove(RideIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(RideIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + RideKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("RideAdapter", "postRide:onCancelled", databaseError.toException());
                    // Toast.makeText(context, "Failed to load comments.",
                    //       Toast.LENGTH_SHORT).show();
                }
            };

            query.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }

        @NonNull
        @Override
        public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.card_view_profile, parent, false);
            return new RideViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(@NonNull final RideViewHolder holder, int position) {
            final Ride ride = Rides.get(position);
            holder.status.setText(
                    DateTimeAndStringHelper.capitalize(getStatusText(ride.getStatus())));
            holder.typeCard.setText(typeMessage(ride.getType()));
            if (ride.getType().equals(Type.OFFERED)) {
                holder.rideImage.setBackground(activity.getResources().getDrawable(R.drawable.taxi));
            } else {

                holder.rideImage.setBackground(activity.getResources().getDrawable(R.drawable.como));
            }
            holder.card.setOnLongClickListener(view -> {
                Intent intent = new Intent(activity, RideDetailActivity.class);
                intent.putExtra("ride", ride);
                activity.startActivity(intent);
                return false;

            });
        }

        @Override
        public int getItemCount() {
            return Rides.size();
        }

        private void cleanupListener() {
            if (childEventListener != null) {
                Log.d("CLEAN", "LIMPIANDO LISTENER");
                databaseReference.removeEventListener(childEventListener);
            }
        }
        private String typeMessage(Type type){
            if(type==Type.REQUESTED){
                return  activity.getResources().getString(R.string.radioTypeRequest);
            }
            else{
                return  activity.getResources().getString(R.string.radioTypeOffer);
            }
        }

        private String getStatusText(Status status){
            String statusText;
            if(status.equals(Status.ACTIVE)){
                statusText= activity.getResources().getString(R.string.active);

            }
            else if(status.equals(Status.CANCELED)){
                statusText= activity.getResources().getString(R.string.canceled);

            }
            else{
                statusText= activity.getResources().getString(R.string.finished);
            }
            return statusText;
        }

    }
}

