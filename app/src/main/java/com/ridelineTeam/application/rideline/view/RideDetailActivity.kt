package com.ridelineTeam.application.rideline.view

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.*
import com.google.maps.model.TravelMode
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.MapDrawHelper
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.enums.Status
import com.ridelineTeam.application.rideline.util.helpers.ConnectivityHelper
import com.ridelineTeam.application.rideline.util.helpers.ImageHelper
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.Disposable


class RideDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var txtUser: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtHour: TextView
    private lateinit var txtPrice: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtUsername: TextView
    private lateinit var manager: LocationManager
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var number: String
    private lateinit var avatar:String
    private lateinit var btnNavigation: FloatingActionButton
    private lateinit var btnCall: Button
    private lateinit var connectivityDisposable: Disposable
    private lateinit var btnBottomDetails:FloatingActionButton
    private lateinit var userImage: CircleImageView
    private var ride = Ride()
    private lateinit var user:User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        reference = database.reference.child(USERS)
        setContentView(R.layout.activity_ride_detail)
        btnNavigation = findViewById(R.id.btn_navigation)
        btnBottomDetails=findViewById(R.id.btnBottomDetails)
        ride = intent.getSerializableExtra("ride") as Ride
        Log.d("RIDE", "ES:" + ride.toString())
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getUser()
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            FragmentHelper.startGPS(this@RideDetailActivity)

        }
        btnNavigation.setOnClickListener {
           initNavigation(ride, this)

        }
        btnBottomDetails.setOnClickListener {
            dialogCustomDetail()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        drawMap(ride.origin, ride.destination)

    }

    private fun drawMap(origin: String, destination: String) {
        try {
            val results = MapDrawHelper.getDirectionsDetails(origin, destination,
                    TravelMode.DRIVING, getString(R.string.apiKey))
            mMap.clear()
            MapDrawHelper.setupGoogleMapScreenSettings(mMap)
            MapDrawHelper.addPolyline(results!!, mMap)
            MapDrawHelper.positionCamera(results.routes[MapDrawHelper.overview], mMap)
            MapDrawHelper.addMarkersToMap(results, mMap,this@RideDetailActivity)
            mMap.setMinZoomPreference(10.0f)
            mMap.setMaxZoomPreference(15.0f)
        } catch (e: Exception) {

        }
    }

    override fun onStart() {
        super.onStart()
        if (ride.status == Status.FINISHED) {
            btnNavigation.visibility = View.INVISIBLE
        }
        drawMap(ride.origin, ride.destination)
    }


    private fun getUser() {
        reference.child(ride.user).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(applicationContext, databaseError.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(data: DataSnapshot) {
                 user= data.getValue(User::class.java)!!
            }

        })
    }

    private fun makeCall() {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:${user.telephone}")
        startActivity(callIntent)
    }

    private fun initNavigation(ride: Ride, activity: Activity) {
        val uri = Uri.parse("google.navigation:q=" + ride.destination)
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity")
        activity.startActivity(mapIntent)
    }
    override fun onResume() {
        super.onResume()
        connectivityDisposable=ConnectivityHelper.networkConnectionState(applicationContext,this@RideDetailActivity)

    }

    override fun onPause() {
        super.onPause()
        ConnectivityHelper.safelyDispose(connectivityDisposable)
    }
    private fun dialogCustomDetail() {
        val dialog: DialogPlus? = DialogPlus.newDialog(this@RideDetailActivity)
                .setContentHolder(ViewHolder(R.layout.ride_details_dialog))
                .setExpanded(true)
                .setCancelable(true)
                .create()
        dialog!!.footerView
        dialog!!.show()
        rideDetails(dialog)



    }

    private fun rideDetails(dialog: DialogPlus?) {
        val customDialog = dialog!!.holderView
        btnCall = customDialog.findViewById(R.id.btn_call)
        txtDate = customDialog.findViewById(R.id.detailDate)
        txtStatus = customDialog.findViewById(R.id.detailStatus)
        userImage = customDialog.findViewById(R.id.userImage)
        txtPrice=customDialog.findViewById(R.id.price)
        txtUsername=customDialog.findViewById(R.id.txtUserName)
        txtHour=customDialog.findViewById(R.id.hour)
        ride.apply {
            if(status == Status.ACTIVE)
            txtDate.text =  "${resources.getString(R.string.date)}: $date"
            txtStatus.text ="${resources.getString(R.string.hint_status)}: ${resources.getString(R.string.statusActive)}"
            txtHour.text = """${getString(R.string.hour)}:${ride.time}"""
            if(cost!!.price>0) {
                txtPrice.text = "${Math.round(ride.cost!!.price)
                }"
            }else{
                txtPrice.text=getString(R.string.free_ride)
            }

        }

        ImageHelper.setImageViewPicture(userImage,this@RideDetailActivity,user.pictureUrl)
        txtUsername.text= "${user.name} ${user.lastName}"
        btnCall.setOnClickListener {
            makeCall()
        }
    }
}
