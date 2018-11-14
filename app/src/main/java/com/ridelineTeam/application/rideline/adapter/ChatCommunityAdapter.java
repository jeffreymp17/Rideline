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

public class ChatCommunityAdapter {

    static class CommunityViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView timeZone;
        private TextView message;
        private LinearLayout bubble;


        private CommunityViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name_other_user);
            message = view.findViewById(R.id.message);
            timeZone=view.findViewById(R.id.time_message);
            bubble=view.findViewById(R.id.layout_bubble);
        }
    }


    public static class ChatCommunityAdapterRecycler extends RecyclerView.Adapter<ChatCommunityAdapter.CommunityViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ChildEventListener childEventListener;
        private List<Messages> messages;
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
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    try {
                        Messages message= dataSnapshot.getValue(Messages.class);
                        communitiesIds.add(dataSnapshot.getKey());
                        messages.add(message);

                        notifyItemInserted(messages.size() - 1);
                    } catch (Exception e) {
                        Log.e("RideAdapter", e.getMessage());
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Messages message = dataSnapshot.getValue(Messages.class);

                    String communityKey = dataSnapshot.getKey();
                    int communityIndex = communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        messages.set(communityIndex,message);
                        notifyItemChanged(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String communityKey = dataSnapshot.getKey();

                    int communityIndex =  communitiesIds.indexOf(communityKey);
                    if (communityIndex > -1) {
                        communitiesIds.remove(communityIndex);
                        messages.remove(communityIndex);

                        notifyItemRemoved(communityIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + communityKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("Chat", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("Chat", "postRide:onCancelled", databaseError.toException());
                }
            };
            this.databaseReference.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }

        @NonNull
        @Override
        public ChatCommunityAdapter.CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.conversation_bubbles, parent, false);
            return new ChatCommunityAdapter.CommunityViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(@NonNull final ChatCommunityAdapter.CommunityViewHolder holder, int position) {
            final Messages message = messages.get(position);
            DatabaseReference db=FirebaseDatabase.getInstance().getReference();
            db.child(USERS).child(message.getUserName()).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   User user=dataSnapshot.getValue(User.class);
                   if(user!=null) {
                       holder.name.setText(user.getName());
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


        void cleanupListener() {
            if (childEventListener != null) {
                databaseReference.removeEventListener(childEventListener);
            }
        }

    }
}
