package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.maps.model.TravelMode
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.PlaceAutocompleteAdapter
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.files.*
import com.ridelineTeam.application.rideline.util.helpers.MapDrawHelper
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper
import es.dmoral.toasty.Toasty


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private lateinit var txtOrigin: AutoCompleteTextView
    private lateinit var txtDestination: AutoCompleteTextView
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var btnShowRoute: Button
    private lateinit var loadingBar: View
    private lateinit var btnCreate: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var listOfTokens = ArrayList<String>()
    private var usersIds = ArrayList<String>()
    private var communityDescription = ""

    private val overview = 0
    private lateinit var ride: Ride

    private lateinit var placeAutocompleteAdapter: PlaceAutocompleteAdapter
    private lateinit var mGoogleApiClient: GoogleApiClient

    private var LAT_LONG_BOUNDS = LatLngBounds(
            LatLng((-40).toDouble(), (-168).toDouble()),
            LatLng((71).toDouble(), (136).toDouble())
    )

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toasty.error(this,p0.errorMessage.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()
        val filter = AutocompleteFilter.Builder()
                .setCountry("CR")
        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LONG_BOUNDS,filter.build())
        txtOrigin = findViewById(R.id.txtOrigin)
        txtDestination = findViewById(R.id.txtDestination)
        btnShowRoute = findViewById(R.id.btn_ShowRoute)
        btnCreate = findViewById(R.id.btn_createRide)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        loadingBar = findViewById(R.id.loading)
        ride = intent.getSerializableExtra("rideObject") as Ride
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(RIDES).push()
        Log.d("gettingObject", ride.toString())
        Log.d("MY COMMUNITY::", "" + getCommunityForNotification(ride.community))


    }

    override fun onStart() {
        super.onStart()
        mapFragment.getMapAsync(this)
        txtOrigin.setAdapter(placeAutocompleteAdapter)
        txtDestination.setAdapter(placeAutocompleteAdapter)
        btnShowRoute.setOnClickListener { _ ->
            if (validateFields()) {
                loadingBar.visibility = View.VISIBLE
                val origin = txtOrigin.text.toString()
                val destination = txtDestination.text.toString()
                drawMap(origin, destination)
            } else {
                Toast.makeText(this, getString(R.string.completeFields), Toast.LENGTH_SHORT).show()
            }
        }
        btnCreate.setOnClickListener({ _ ->
            if (validateFields()) {
                ride.origin = txtOrigin.text.toString()
                ride.destination = txtDestination.text.toString()
                ride.id = databaseReference.key!!
                databaseReference.setValue(ride).addOnSuccessListener {
                    getCommunityUsers()
                    databaseReference.database.reference
                            .child(USERS)
                            .child(ride.user)
                            .child("activeRide")
                            .setValue(ride).addOnCompleteListener {
                                if(it.isComplete){
                                    databaseReference.database.reference.child(USERS)
                                            .child(ride.user)
                                            .child("taked").setValue(1)
                                    startActivity(Intent(baseContext, MainActivity::class.java))

                                }
                            }.addOnFailureListener{
                                Toasty.error(applicationContext,"Error when save active ride",Toast.LENGTH_SHORT,true).show();
                            }
                }
            }
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        loadingBar.visibility = View.VISIBLE
        mMap = googleMap
        MapDrawHelper.setupGoogleMapScreenSettings(mMap)
        val location = MapDrawHelper.getCurrentLocation(this)
        mMap.addMarker(MarkerOptions().position(location).title("You are here!")
                .icon(BitmapDescriptorFactory.defaultMarker(45.0f))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        loadingBar.visibility = View.GONE

    }

    private fun drawMap(origin: String, destination: String) {
        try {
            val results = MapDrawHelper.getDirectionsDetails(origin, destination,
                    TravelMode.DRIVING, getString(R.string.apiKey))
            mMap.clear()
            MapDrawHelper.setupGoogleMapScreenSettings(mMap)
            MapDrawHelper.addPolyline(results!!, mMap)
            MapDrawHelper.positionCamera(results.routes[overview], mMap)
            MapDrawHelper.addMarkersToMap(results, mMap)
            loadingBar.visibility = View.GONE
        } catch (e: Exception) {
            txtOrigin.text = null
            txtDestination.text = null
            Toasty.error(this, getString(R.string.errorMessage), Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateFields(): Boolean {
        return when {
            txtOrigin.text.toString().trim().isEmpty() -> {
                txtOrigin.error = "Required field"
                false
            }
            txtDestination.text.toString().trim().isEmpty() -> {
                txtDestination.error = "Required field"
                false
            }
            else -> true
        }
    }

    private fun getCommunityUsers() {
        val ref: DatabaseReference = database.reference
        val query: Query = ref.child(COMMUNITIES).child(ride.community).child(COMMUNITY_USERS)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                for (users in data.children) {
                    usersIds.add(users.value.toString())
                }
                var users=usersIds.filterNot { it==MainActivity.userId }
                getTokens(users as ArrayList<String>)
                Log.d("USERS", "LIST--->$usersIds")
            }


        })
    }

    private fun getTokens(list: ArrayList<String>) {
        getCommunityForNotification(ride.community)
        val ref: DatabaseReference = database.reference
        val query: Query = ref.child(USERS)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(userToken: DataSnapshot) {
                for (items in list) {
                    listOfTokens.add(userToken.child(items).child(TOKEN).value.toString())
                }
                if (ride.type.toString() == Type.OFFERED.toString()) {
                    NotificationHelper.messageToCommunity(MainActivity.fmc, listOfTokens,
                            resources.getString(R.string.new_ride_offered)
                            , resources.getString(R.string.new_ride_offered_body)+" "+communityDescription
                            , ride)
                } else {
                    NotificationHelper.messageToCommunity(MainActivity.fmc, listOfTokens,
                            resources.getString(R.string.new_ride_requested)
                            , resources.getString(R.string.new_ride_requested_body)+" "+communityDescription
                            , ride)

                }


            }


        })
    }

    private fun getCommunityForNotification(key: String) {
        val ref: DatabaseReference = database.reference
        ref.child(COMMUNITIES).child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                communityDescription = data.child(NAME).value.toString()
                Log.d("COMMUNITY", "-DATA:::$communityDescription")
            }

        })
    }


}
