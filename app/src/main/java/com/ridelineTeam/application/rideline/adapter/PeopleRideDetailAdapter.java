package com.ridelineTeam.application.rideline.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Ride;
import com.ridelineTeam.application.rideline.model.User;
import com.ridelineTeam.application.rideline.model.enums.Type;
import com.ridelineTeam.application.rideline.view.PeopleRideDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleRideDetailAdapter {
    static class PeopleRideDetailViewHolder extends RecyclerView.ViewHolder {

        private CardView cardViewPeopleRide;
        private TextView userCard;
        private TextView driverCard;
        private ImageButton btnRemovePassenger;
        private CircleImageView userPicture;

        private PeopleRideDetailViewHolder(View view) {
            super(view);
            cardViewPeopleRide = view.findViewById(R.id.cardViewPeopleRide);
            userCard = view.findViewById(R.id.userCard);
            driverCard = view.findViewById(R.id.driverCard);
            btnRemovePassenger = view.findViewById(R.id.btnRemovePassenger);
            userPicture = view.findViewById(R.id.userPicture);
        }
    }

    public static class PeopleRideDetailAdapterRecycler
            extends RecyclerView.Adapter<PeopleRideDetailViewHolder> {
        private Context context;
        private List<User> users;
        private String driver;
        private String rideId;
        private Activity activity;
        private String currentUser;
        private Type rideType;

        public PeopleRideDetailAdapterRecycler(final Context context,
                                              Activity activity, ArrayList<User> users,
                                               String driver,String currentUser, Ride ride) {
            this.context = context;
            this.activity = activity;
            this.users = users;
            this.driver = driver;
            this.currentUser = currentUser;
            this.rideType = ride.getType();
            this.rideId= ride.getId();
        }

        @NonNull
        @Override
        public PeopleRideDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.cardview_people_ride_detail, parent, false);
            return new PeopleRideDetailAdapter.PeopleRideDetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PeopleRideDetailViewHolder holder, int position) {
            User user = this.users.get(position);
            if (rideType==Type.REQUESTED){
                holder.btnRemovePassenger.setVisibility(View.GONE);
                if(user.getId().equals(driver))
                    holder.driverCard.setText(activity.getString(R.string.passengerText));
                else
                    holder.driverCard.setText(activity.getString(R.string.driver));
            }
            else{
                if (!currentUser.equals(driver))
                    holder.btnRemovePassenger.setVisibility(View.GONE);

                if(user.getId().equals(driver)) {
                    holder.driverCard.setText(activity.getString(R.string.driver));
                    holder.btnRemovePassenger.setVisibility(View.GONE);
                }
                else
                    holder.driverCard.setText(activity.getString(R.string.passengerText));

                holder.btnRemovePassenger.setOnClickListener(view
                        -> removePassengerDialog(user,rideId,activity));
            }

            if(user.getId().equals(currentUser))
                holder.userCard.setText(activity.getString(R.string.you));
            else{
                String fullName = user.getName()+" "+user.getLastName();
                holder.userCard.setText(fullName);
            }

            if (!user.getPictureUrl().isEmpty())
                Picasso.with(activity).load(user.getPictureUrl()).fit().into(holder.userPicture);
            else
                Picasso.with(activity).load(R.drawable.avatar).fit().into(holder.userPicture);

        }

        @Override
        public int getItemCount() {
            return this.users.size();
        }

        private void removePassengerDialog(User passenger, String rideId, Activity activity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            // Set the alert dialog title
            builder.setTitle("Remover del ride");
            // Display a message on alert dialog
            builder.setMessage("¿Desea remover a este pasajero de su ride?");

            DialogInterface.OnClickListener positive = (dialogInterface, i)
                    -> PeopleRideDetailActivity.Companion.removePassenger(passenger,rideId,
                    activity);

            DialogInterface.OnClickListener negative = (dialogInterface, i) -> {

            };

            builder.setPositiveButton(activity.getResources().getString(R.string.yes),positive);

            builder.setNegativeButton(activity.getResources().getString(R.string.no),negative);

            AlertDialog dialog  = builder.create();

            dialog.show();

        }

    }
}
