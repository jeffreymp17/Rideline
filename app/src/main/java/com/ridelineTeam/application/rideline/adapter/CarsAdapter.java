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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Car;
import com.ridelineTeam.application.rideline.model.User;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CarsAdapter {

    static class CarsViewHolder extends RecyclerView.ViewHolder {
        private TextView model;
        private TextView plate;
        private CardView card;


        private CarsViewHolder(View view) {
            super(view);
            model = view.findViewById(R.id.carModel);
            plate = view.findViewById(R.id.carPlate);
            card=view.findViewById(R.id.cardViewCar);
        }
    }


    public static class CarsAdapterRecycler extends RecyclerView.Adapter<CarsViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ValueEventListener valueEventListener;
        private List<Car> cars;
        private Activity activity;


        public CarsAdapterRecycler(final Context context, DatabaseReference reference,
                                      Activity activity) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.cars = new ArrayList<>();

            this.valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    if (user!=null) {
                        cars = user.getCars();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addValueEventListener(this.valueEventListener);
        }

        @NonNull
        @Override
        public CarsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.cardview_car, parent, false);
            return new CarsAdapter.CarsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CarsViewHolder holder, int position) {
            final Car car = cars.get(position);
            Toasty.info(context,car.getModel(),Toast.LENGTH_SHORT).show();
            holder.model.setText(car.getModel());
            holder.plate.setText(car.getPlate());

        }

        @Override
        public int getItemCount() {
            return cars.size();
        }

        private void cleanupListener() {
            if (valueEventListener != null) {
                databaseReference.removeEventListener(valueEventListener);
            }
        }


    }
}

