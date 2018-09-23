package com.ridelineTeam.application.rideline.view

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.PeopleRideDetailAdapter
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.enums.Status
import com.ridelineTeam.application.rideline.util.files.RIDES
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.helpers.ConnectivityHelper
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.Disposable

class PeopleRideDetailActivity : AppCompatActivity() {

    private lateinit var ride:Ride
    private lateinit var peopleRecycler: MultiSnapRecyclerView
    private lateinit var toolbar: android.support.v7.widget.Toolbar
    private lateinit var adapter: PeopleRideDetailAdapter.PeopleRideDetailAdapterRecycler
    private lateinit var connectivityDisposable: Disposable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_ride_detail)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.ride_passengers)
        FragmentHelper.backButtonToFragment(toolbar, PeopleRideDetailActivity@ this)

        ride = intent.getSerializableExtra("rideObject") as Ride
        peopleRecycler = findViewById(R.id.peopleRecycler)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        peopleRecycler.layoutManager = linearLayoutManager
    }
    override fun onStart() {
        super.onStart()
        getDriver()
    }
    private fun getDriver(){
        val passengers = ArrayList<User>()
        val db = FirebaseDatabase.getInstance().reference.child(USERS).child(ride.user)
        db.runTransaction(object: Transaction.Handler {
            override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, p2: DataSnapshot?) {

                if (boolean){
                    getRidePassengers(passengers)
                }

            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val user = mutableData.getValue(User::class.java)
                if(user!=null)
                    passengers.add(user)
                return Transaction.success(mutableData)
            }
        })
    }
    private fun getRidePassengers(passengers:ArrayList<User>){
        val db = FirebaseDatabase.getInstance().reference.child(RIDES).child(ride.id)
        db.runTransaction(object: Transaction.Handler {
            override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, p2: DataSnapshot?) {

                if (boolean){
                    adapter = PeopleRideDetailAdapter.PeopleRideDetailAdapterRecycler(
                            this@PeopleRideDetailActivity,
                            this@PeopleRideDetailActivity,passengers,ride.user,
                            FirebaseAuth.getInstance().currentUser!!.uid,ride)
                    peopleRecycler.adapter=adapter
                }

            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val ride = mutableData.getValue(Ride::class.java)
                for (passenger in ride!!.passengers.values){
                    passengers.add(passenger)
                }
                return Transaction.success(mutableData)
            }
        })
    }

    companion object {

         fun removePassenger(user: User, rideId: String,activity: Activity){
             val passengers: HashMap<String,User> = HashMap()
             val db = FirebaseDatabase.getInstance().reference.child(RIDES).child(rideId)
             db.runTransaction(object: Transaction.Handler {
                override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, p2: DataSnapshot?) {

                    if (boolean){
                        Toasty.success(activity.applicationContext,
                                activity.getString(R.string.passenger_removed),
                                Toast.LENGTH_LONG).show()

                    }

                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val ride = mutableData.getValue(Ride::class.java)
                    for (passenger in ride!!.passengers.values){
                        if(passenger.id!=user.id)
                            passengers[passenger.id] = passenger
                        else{
                            val currentUser = FirebaseAuth.getInstance().currentUser!!
                            val userDb =FirebaseDatabase.getInstance().reference.child(USERS)
                                    .child(user.id)
                            userDb.child("activeRide").removeValue()
                            userDb.child("taked").setValue(0)
                            NotificationHelper.message(MainActivity.fmc,user.token,
                                    activity.resources.getString(R.string.rejected_request),
                                    currentUser.displayName+" "+ activity.resources.getString(R.string.rejected_request_message))

                        }
                    }
                    if (ride.status==Status.FINISHED){
                        ride.status=Status.ACTIVE
                    }
                    ride.passengers = passengers
                    mutableData.value=ride
                    return Transaction.success(mutableData)
                }
             })
        }
    }
    override fun onResume() {
        super.onResume()
        connectivityDisposable= ConnectivityHelper.networkConnectionState(applicationContext,this@PeopleRideDetailActivity)
    }

    override fun onPause() {
        super.onPause()
        ConnectivityHelper.safelyDispose(connectivityDisposable)


    }
}
