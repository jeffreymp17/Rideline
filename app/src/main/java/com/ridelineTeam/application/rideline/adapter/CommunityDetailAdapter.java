package com.ridelineTeam.application.rideline.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommunityDetailAdapter {
    static class CommunityDetailViewHolder extends RecyclerView.ViewHolder {

        private CardView userCommunityCardView;
        private TextView usernameCard;
        private TextView ownerCard;
        private TextView statusCard;
        private CircleImageView userImage;

        private CommunityDetailViewHolder(View view) {
            super(view);
            userCommunityCardView = view.findViewById(R.id.userCommunityCardView);
            usernameCard = view.findViewById(R.id.usernameCard);
            ownerCard = view.findViewById(R.id.ownerCard);
            statusCard = view.findViewById(R.id.statusCard);
            userImage = view.findViewById(R.id.image_view_user);
        }
    }

    public static class CommunityDetailAdapterRecycler
            extends RecyclerView.Adapter<CommunityDetailViewHolder> {
        private Context context;
        private List<User> users;
        private List<String> usersIds;
        private String admin;
        private Activity activity;

        public CommunityDetailAdapterRecycler(final Context context,
                                              Activity activity, ArrayList<User> users,
                                              ArrayList<String> usersIds, String admin) {
            this.context = context;
            this.activity = activity;
            this.users = users;
            this.usersIds = usersIds;
            this.admin = admin;
        }

        @NonNull
        @Override
        public CommunityDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.users_community_cardview, parent, false);
            return new CommunityDetailAdapter.CommunityDetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommunityDetailViewHolder holder, int position) {
            User user = this.users.get(position);
            String key = this.usersIds.get(position);
            //muestra tu en el nombre del usuario actual
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.getId()))
                holder.usernameCard.setText(this.activity.getString(R.string.you));
            else
                holder.usernameCard.setText(user.getName() + " " + user.getLastName());
            //Muestra quien es el administrador de la comunidad
            if (this.admin.equals(user.getId()))
                holder.ownerCard.setText("admin");
            else
                holder.ownerCard.setText("");
            //Pone la foto por defecto si no existe una elegida por usuario.
            if (!user.getPictureUrl().isEmpty())
                Picasso.with(activity).load(user.getPictureUrl()).fit().into(holder.userImage);
            else
                Picasso.with(activity).load(R.drawable.avatar).fit().into(holder.userImage);
            holder.statusCard.setText(user.getStatus());

        }

        @Override
        public int getItemCount() {
            return this.users.size();
        }
    }
}
