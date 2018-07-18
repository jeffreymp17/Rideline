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
import com.google.firebase.database.ValueEventListener;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Community;
import com.ridelineTeam.application.rideline.model.User;
import com.ridelineTeam.application.rideline.util.helpers.DateTimeAndStringHelper;
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper;
import com.ridelineTeam.application.rideline.view.fragment.ChatCommunityActivity;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.COMMUNITIES;
import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.USERS;


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

        public CommunityAdapterRecycler(final Context context, DatabaseReference reference,
                                        Activity activity) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.communities = new ArrayList<>();
            this.communitiesIds = new ArrayList<>();
            String currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    try {
                        Community community = dataSnapshot.getValue(Community.class);
                        for (String userId:community.getUsers()) {
                            if (userId.equals(currentUserId)){
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
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("CommunityAdapter", "onChildChanged:" + dataSnapshot.getKey());
                    Log.d("Changed", dataSnapshot.toString());
                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Community community = dataSnapshot.getValue(Community.class);

                    String communityKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        // Replace with the new data
                        communities.set(communityIndex, community);
                        // Update the RecyclerView
                        notifyItemChanged(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d("RideAdapter", "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String communityKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        // Remove data from the list
                        communitiesIds.remove(communityIndex);
                        communities.remove(communityIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("RideAdapter", "postRide:onCancelled", databaseError.toException());
                    //Toast.makeText(context, "Failed to load comments.",Toast.LENGTH_SHORT).show();
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
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("CommunityAdapter", "onChildAdded " + dataSnapshot.getKey());
                    Log.d("ADDED", dataSnapshot.toString());

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
                    Log.d("CommunityAdapter", "onChildChanged:" + dataSnapshot.getKey());
                    Log.d("Changed", dataSnapshot.toString());
                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Community community = dataSnapshot.getValue(Community.class);

                    String communityKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        // Replace with the new data
                        communities.set(communityIndex, community);
                        // Update the RecyclerView
                        notifyItemChanged(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("RideAdapter", "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String communityKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        // Remove data from the list
                        communitiesIds.remove(communityIndex);
                        communities.remove(communityIndex);

                        // Update the RecyclerView
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
            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            holder.communityNameCard.setText(
                    DateTimeAndStringHelper.capitalize(community.getName()));
            holder.communityDescriptionCard.setText(community.getDescription());

            holder.cardViewCommunity.setOnClickListener(view -> {
                Boolean inCommunity = false;
                for (String userId:community.getUsers()) {
                    if (currentUser.getUid().equalsIgnoreCase(userId))
                        inCommunity=true;
                }
                if (inCommunity){
                    Intent intent = new Intent(activity, ChatCommunityActivity.class);
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
                String key = communitiesIds.get(holder.getAdapterPosition());
                joinToGroup(key);
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
                Log.d("CLEAN", "LIMPIANDO LISTENER");
                databaseReference.removeEventListener(childEventListener);
            }
        }

        private void joinToGroup(final String key) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference db = databaseReference.getDatabase().getReference().child(COMMUNITIES);
            db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Community community = dataSnapshot.getValue(Community.class);
                    community.getUsers().add(user.getUid());
                    db.child(key).child("users").setValue(community.getUsers());

                    final DatabaseReference reference = databaseReference.getDatabase().getReference().child(USERS);
                    reference.child(community.getCreatedBy()).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user1 = dataSnapshot.getValue(User.class);
                                    if (user1 != null) {
                                        NotificationHelper.message(com.ridelineTeam.application.rideline.MainActivity.Companion.getFmc(),
                                                user1.getToken(), activity.getResources().getString(R.string.join_title),
                                                user.getDisplayName() + " "+
                                                activity.getResources().getString(R.string.join_body));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            }
                    );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            final DatabaseReference db1 = databaseReference.getDatabase().getReference().child(USERS);
            if (user != null) {
                db1.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user1 = dataSnapshot.getValue(User.class);
                        if (user1 != null) {
                            user1.getCommunities().add(key);
                            db1.child(user.getUid()).setValue(user1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
