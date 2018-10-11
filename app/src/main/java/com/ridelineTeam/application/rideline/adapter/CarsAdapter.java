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
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Car;

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
            model = view.findViewById(R.id.status);
            plate = view.findViewById(R.id.ride_image_profile);
            card=view.findViewById(R.id.card_profile);
        }
    }


    public static class CarsAdapterRecycler extends RecyclerView.Adapter<CarsViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ChildEventListener childEventListener;
        private List<Car> cars;
        private List<String> carsIds;
        private Activity activity;


        public CarsAdapterRecycler(final Context context, DatabaseReference reference,
                                      Activity activity) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.cars = new ArrayList<>();
            this.carsIds = new ArrayList<>();

            this.childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                    Car car = dataSnapshot.getValue(Car.class);

                    carsIds.add(dataSnapshot.getKey());

                    cars.add(car);

                    notifyItemInserted(cars.size() - 1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                    Car car = dataSnapshot.getValue(Car.class);
                    String carKey = dataSnapshot.getKey();

                    int cardIndex = carsIds.indexOf(carKey);
                    if (cardIndex > -1) {
                        cars.set(cardIndex, car);
                        notifyItemChanged(cardIndex);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String carKey = dataSnapshot.getKey();

                    int carIndex = carsIds.indexOf(carKey);
                    if (carIndex > -1) {

                        carsIds.remove(carIndex);
                        cars.remove(carIndex);

                        notifyItemRemoved(carIndex);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                     Toasty.error(context, "Failed to load comments.", Toast.LENGTH_SHORT).show();
                }
            };
        }

        @NonNull
        @Override
        public CarsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.card_view_profile, parent, false);
            return new CarsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CarsViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return cars.size();
        }

        private void cleanupListener() {
            if (childEventListener != null) {
                databaseReference.removeEventListener(childEventListener);
            }
        }


    }
}

