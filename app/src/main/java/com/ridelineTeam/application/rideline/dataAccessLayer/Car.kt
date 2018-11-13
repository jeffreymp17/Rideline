package com.ridelineTeam.application.rideline.dataAccessLayer

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ridelineTeam.application.rideline.adapter.CarsAdapter
import com.ridelineTeam.application.rideline.model.Car
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.files.USERS

class Car(private val activity: Activity){
    val database:FirebaseDatabase = FirebaseDatabase.getInstance()
    private var adapter:CarsAdapter.CarsAdapterRecycler? = null
    private var currentUser = FirebaseAuth.getInstance().currentUser!!

    fun all(recycler: RecyclerView){
        val linearLayoutManager = LinearLayoutManager(activity.baseContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        adapter = CarsAdapter.CarsAdapterRecycler(activity, database.reference.child(USERS)
                .child(currentUser.uid),activity)
        recycler.adapter = adapter
    }

    fun register(car: Car){
        val db = database.reference.child(USERS).child(currentUser.uid)
        car.id = db.push().key!!
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    user.cars.add(car)
                    db.setValue(user)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun remove(car: Car){
        val db = database.reference.child(USERS).child(currentUser.uid)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    val cars = user.cars.filter {!it.id.contentEquals(car.id)}
                    user.cars = ArrayList(cars)
                    db.setValue(user)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

}