package com.ridelineTeam.application.rideline.view

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.*
import com.google.maps.model.TravelMode
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.MapDrawHelper
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.util.enums.Status
import com.ridelineTeam.application.rideline.util.helpers.ConnectivityHelper
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.Disposable


class RideDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var txtOrigin: TextView
    private lateinit var txtDestination: TextView
    private lateinit var txtUser: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtStatus: TextView
    private lateinit var showDetailText: TextView
    private lateinit var manager: LocationManager
    private lateinit var linearDetail: LinearLayout
    private lateinit var relativeDetail: RelativeLayout
    private lateinit var cardviewDetails: CardView
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var number: String
    private lateinit var btnNavigation: FloatingActionButton
    private lateinit var btnCall: FloatingActionButton
    private lateinit var connectivityDisposable: Disposable
    private var ride = Ride()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        reference = database.reference.child(USERS)
        setContentView(R.layout.activity_ride_detail)
        txtOrigin = findViewById(R.id.detailOrigin)
        btnCall = findViewById(R.id.btn_call)
        txtDestination = findViewById(R.id.detailDestination)
        txtDate = findViewById(R.id.detailDate)
        txtStatus = findViewById(R.id.detailStatus)
        txtUser = findViewById(R.id.detailCommunity)
        btnNavigation = findViewById(R.id.btn_navigation)
        linearDetail = findViewById(R.id.linear_detail_information)
        relativeDetail = findViewById(R.id.relative_detail_information)
        cardviewDetails = findViewById(R.id.card_details)
        showDetailText = findViewById(R.id.show_detail_text)
        ride = intent.getSerializableExtra("ride") as Ride
        Log.d("RIDE", "ES:" + ride.toString())
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        showRideInformation()
        cardviewDetails.setOnClickListener {
            if (linearDetail.visibility == View.GONE) {
                showDetailText.visibility = View.GONE
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels / 3
                expand(it, 1000, height)
                linearDetail.visibility = View.VISIBLE
                relativeDetail.visibility = View.VISIBLE

            } else {
                val initialHeight = resources.getDimension(R.dimen.height_card_ride_detail).toInt()
                showDetailText.visibility = View.VISIBLE
                expand(it, 1000, initialHeight)
                linearDetail.visibility = View.GONE
                relativeDetail.visibility = View.GONE
            }


        }
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            FragmentHelper.startGPS(this@RideDetailActivity)

        }
        btnCall.setOnClickListener {
            makeCall(number)
        }
        btnNavigation.setOnClickListener {
           initNavigation(ride, this)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
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

    private fun showRideInformation() {
        getUser()
        ride.apply {
            val originText = resources.getString(R.string.originText) + " " + origin
            val destinationText = resources.getString(R.string.destinationText) + " " + destination
            val dateText = resources.getString(R.string.date) + ": " + date + " " + time
            val statusText = resources.getString(R.string.hint_status) + ": " + status
            txtOrigin.text = originText
            txtDestination.text = destinationText
            txtDate.text = dateText
            txtStatus.text = statusText

        }
    }

    private fun getUser() {
        reference.child(ride.user).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(applicationContext, databaseError.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(data: DataSnapshot) {
                val userText ="User:" + data.child("name").value.toString() + " " +
                        "" + data.child("lastName").value.toString()
                txtUser.text = userText
                number = data.child("telephone").value.toString()
            }

        })
    }

    private fun makeCall(telephoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$telephoneNumber")
        startActivity(callIntent)
    }

    private fun initNavigation(ride: Ride, activity: Activity) {
        val uri = Uri.parse("google.navigation:q=" + ride.destination)
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity")
        activity.startActivity(mapIntent)
    }

    private fun expand(v: View, duration: Int, targetHeight: Int) {

        val prevHeight = v.height

        v.visibility = View.VISIBLE
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
        valueAnimator.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

    override fun onResume() {
        super.onResume()
        connectivityDisposable=ConnectivityHelper.networkConnectionState(applicationContext,this@RideDetailActivity)

    }

    override fun onPause() {
        super.onPause()
        ConnectivityHelper.safelyDispose(connectivityDisposable)
    }


}
