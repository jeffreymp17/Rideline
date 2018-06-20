package com.ridelineTeam.application.rideline.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Messages;
import com.ridelineTeam.application.rideline.model.User;

import java.util.ArrayList;
import java.util.List;

import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.USERS;

/**
 * Created by jeffry on 10/06/18.
 */

public class ChatCommunityAdapter {

    static class CommunityViewHolder extends RecyclerView.ViewHolder {
        private TextView nombre;
        private TextView timeZone;
        private TextView message;
        private LinearLayout bubble;


        private CommunityViewHolder(View view) {
            super(view);
            nombre = view.findViewById(R.id.name_other_user);
            message = view.findViewById(R.id.message);
            timeZone=view.findViewById(R.id.time_message);
            bubble=view.findViewById(R.id.layout_bubble);
        }
    }


    public static class ChatCommunityAdapterRecycler extends RecyclerView.Adapter<ChatCommunityAdapter.CommunityViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ChildEventListener childEventListener;
        public List<Messages> messages;
        private List<String> communitiesIds;
        private Activity activity;
        private String userId;

        public ChatCommunityAdapterRecycler(final Context context, DatabaseReference reference,
                                        Activity activity,String userId) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.messages=new ArrayList<>();
            this.communitiesIds = new ArrayList<>();
            this.userId = userId;

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("ChatCommunityAdapter", "onChildAdded " + dataSnapshot.getKey());
                    Log.d("ADDED", dataSnapshot.toString());

                    try {
                        Messages message= dataSnapshot.getValue(Messages.class);
                        communitiesIds.add(dataSnapshot.getKey());
                        Log.d("Chats","Conversations"+message);
                        messages.add(message);

                        notifyItemInserted(messages.size() - 1);
                    } catch (Exception e) {
                        Log.e("RideAdapter", e.getMessage());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("ChatCommunityAdapter", "onChildChanged:" + dataSnapshot.getKey());
                    Log.d("Changed", dataSnapshot.toString());
                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                   Messages message = dataSnapshot.getValue(Messages.class);

                    String communityKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        // Replace with the new data
                        messages.set(communityIndex,message);
                        // Update the RecyclerView
                        notifyItemChanged(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d("Chat", "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String communityKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int communityIndex =  communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        // Remove data from the list
                        communitiesIds.remove(communityIndex);
                        messages.remove(communityIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.d("Chat", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("Chat", "postRide:onCancelled", databaseError.toException());
                    //Toast.makeText(context, "Failed to load comments.",Toast.LENGTH_SHORT).show();
                }
            };
            this.databaseReference.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }

      /*  public ChatCommunityAdapterRecycler(final Context context, DatabaseReference reference,
                                        Activity activity, Query query) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.communities = new ArrayList<>();
            this.communitiesIds = new ArrayList<>();
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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
                    int communityIndex =  communitiesIds.indexOf(communityKey);
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


            query.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }
*/
        @Override
        public ChatCommunityAdapter.CommunityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.conversation_bubbles, parent, false);
            return new ChatCommunityAdapter.CommunityViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(final ChatCommunityAdapter.CommunityViewHolder holder, int position) {
            final Messages message = messages.get(position);
           Log.d("data","--->"+message);
            DatabaseReference db=FirebaseDatabase.getInstance().getReference();
            db.child(USERS).child(message.getUserName()).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   User user=dataSnapshot.getValue(User.class);
                   if(user!=null) {
                       holder.nombre.setText(user.getName());
                       Log.d("Data","--------"+userId);
                   }
                   if(user.getId().equals(userId)){
                      holder.bubble.setBackgroundDrawable(activity.getDrawable(R.drawable.rounded_corner_two));
                   }else {
                       holder.bubble.setBackgroundResource(R.drawable.rounded_corner);

                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
            holder.message.setText(message.getMessage());
            holder.timeZone.setText(message.getTime());

        }

        @Override
        public int getItemCount() {
            return messages.size();
        }


        public void cleanupListener() {
            if (childEventListener != null) {
                Log.d("CLEAN", "LIMPIANDO LISTENER");
                databaseReference.removeEventListener(childEventListener);
            }
        }

    }
}
