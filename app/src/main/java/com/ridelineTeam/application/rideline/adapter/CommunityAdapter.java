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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Community;
import com.ridelineTeam.application.rideline.util.helpers.DateTimeAndStringHelper;
import com.ridelineTeam.application.rideline.view.CommunityRidesActivity;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class CommunityAdapter {

    static class CommunityViewHolder extends RecyclerView.ViewHolder {
        private CardView cardViewCommunity;
        private TextView communityNameCard;
        private TextView communityDescriptionCard;
        private Button btnJoin;

        private CommunityViewHolder(View view) {
            super(view);
            communityNameCard = view.findViewById(R.id.communityNameCard);
            communityDescriptionCard = view.findViewById(R.id.communityDescriptionCard);
            btnJoin = view.findViewById(R.id.btn_join);
            cardViewCommunity = view.findViewById(R.id.cardViewCommunity);
        }
    }


    public static class CommunityAdapterRecycler extends RecyclerView.Adapter<CommunityViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ChildEventListener childEventListener;
        private List<Community> communities;
        private List<String> communitiesIds;
        private Activity activity;
        private com.ridelineTeam.application.rideline.dataAccessLayer.Community communityDal;
        final private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        public CommunityAdapterRecycler(final Context context, DatabaseReference reference,
                                        Activity activity) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.communities = new ArrayList<>();
            this.communitiesIds = new ArrayList<>();
            this.communityDal = new com.ridelineTeam.application.rideline.dataAccessLayer.Community(this.activity);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    try {
                        Community community = dataSnapshot.getValue(Community.class);
                        assert community != null;
                        for (String userId:community.getUsers()) {
                            if (userId.equals(currentUser.getUid())){
                                communitiesIds.add(dataSnapshot.getKey());
                                communities.add(community);
                                notifyItemInserted(communities.size() - 1);
                            }
                        }

                    } catch (Exception e) {
                        Log.e("RideAdapter", e.getMessage());
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Community community = dataSnapshot.getValue(Community.class);

                    String communityKey = dataSnapshot.getKey();
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        communities.set(communityIndex, community);

                        notifyItemChanged(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String communityKey = dataSnapshot.getKey();

                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        communitiesIds.remove(communityIndex);
                        communities.remove(communityIndex);

                        notifyItemRemoved(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("RideAdapter", "postRide:onCancelled", databaseError.toException());
                }
            };
            this.databaseReference.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }

        public CommunityAdapterRecycler(final Context context, DatabaseReference reference,
                                        Activity activity, Query query) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.communities = new ArrayList<>();
            this.communitiesIds = new ArrayList<>();
            this.communityDal = new com.ridelineTeam.application.rideline.dataAccessLayer.Community(this.activity);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                    try {
                        Community community = dataSnapshot.getValue(Community.class);
                        communitiesIds.add(dataSnapshot.getKey());

                        communities.add(community);

                        notifyItemInserted(communities.size() - 1);
                    } catch (Exception e) {
                        Log.e("RideAdapter", e.getMessage());
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Community community = dataSnapshot.getValue(Community.class);

                    String communityKey = dataSnapshot.getKey();

                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        communities.set(communityIndex, community);

                        notifyItemChanged(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    String communityKey = dataSnapshot.getKey();

                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {

                        communitiesIds.remove(communityIndex);
                        communities.remove(communityIndex);

                        notifyItemRemoved(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("RideAdapter", "postRide:onCancelled", databaseError.toException());
                    //Toast.makeText(context, "Failed to load comments.",Toast.LENGTH_SHORT).show();
                }
            };


            query.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }
        @NonNull
        @Override
        public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.community_cardview, parent, false);
            return new CommunityViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(@NonNull final CommunityViewHolder holder, int position) {
            final Community community = communities.get(position);
            holder.communityNameCard.setText(DateTimeAndStringHelper.capitalize(community.getName()));
            holder.communityDescriptionCard.setText(community.getDescription());

            holder.cardViewCommunity.setOnClickListener(view -> {
                Boolean inCommunity = community.isUserInCommunity(currentUser.getUid());
                if (inCommunity){
                    Intent intent = new Intent(activity,CommunityRidesActivity.class);
                    intent.putExtra("community", community);
                    activity.startActivity(intent);
                }
                else {
                    Toasty.info(activity.getApplicationContext(),
                            activity.getResources().getString(R.string.cant_access_community),
                            Toast.LENGTH_LONG).show();
                }
            });

            holder.btnJoin.setOnClickListener(view -> {
                String communityId = communitiesIds.get(holder.getAdapterPosition());
                communityDal.join(communityId);
            });

            for (String id : community.getUsers()) {
                if (id.equalsIgnoreCase(currentUser.getUid())) {
                    holder.btnJoin.setEnabled(false);
                    holder.btnJoin.setBackground(activity.getResources()
                            .getDrawable(R.drawable.rounded_button_inactive));
                }
            }
        }

        @Override
        public int getItemCount() {
            return communities.size();
        }

        public void cleanupListener() {
            if (childEventListener != null) {
                databaseReference.removeEventListener(childEventListener);
            }
        }
    }
}
