package com.ridelineTeam.application.rideline.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.maps.model.TravelMode
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.MainActivity.Companion.PERMISSION_REQUEST_ACCESS_FINE_LOCATION
import com.ridelineTeam.application.rideline.MainActivity.Companion.PLACE_PICKER_REQUEST
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.PlaceAutocompleteAdapter
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.CommunityCallback
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.TokenCallback
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.files.*
import com.ridelineTeam.application.rideline.util.helpers.*
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.Disposable
import kotlin.collections.ArrayList
import com.ridelineTeam.application.rideline.dataAccessLayer.Community as CommunityDal


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private lateinit var txtOrigin: AutoCompleteTextView
    private lateinit var txtDestination: AutoCompleteTextView
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var btnShowRoute: Button
    private lateinit var loadingBar: View
    private lateinit var btnCreate: Button
    private lateinit var manager: LocationManager
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var communityDescription = ""
    private val overview = 0
    private lateinit var ride: Ride
    private lateinit var materialDialog: MaterialDialog
    private lateinit var locationCallback: LocationCallback
    private lateinit var placeAutocompleteAdapter: PlaceAutocompleteAdapter
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var lastLocation: Location
    private var latLongBounds = LatLngBounds(
            LatLng((-40).toDouble(), (-168).toDouble()),
            LatLng((71).toDouble(), (136).toDouble())
    )
    private lateinit var connectivityDisposable: Disposable
    private lateinit var communityDal: CommunityDal

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toasty.error(this, p0.errorMessage.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        communityDal = CommunityDal(this@MapsActivity)
        ride = intent.getSerializableExtra("rideObject") as Ride
        val country = intent.getStringExtra("country")
        init(country)
        Log.d("gettingObject", ride.toString())
        Log.d("MY COMMUNITY::", "" + getCommunityForNotification(ride.community))
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        getPermissionLocation()

    }

    private fun init(country: String?) {
        materialDialog = MaterialDialog.Builder(this)
                .title(getString(R.string.loading))
                .content(getString(R.string.please_wait))
                .progress(true, 0).build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()
        val filter = AutocompleteFilter.Builder()
                .setCountry(country)
        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this, mGoogleApiClient, latLongBounds, filter.build())
        txtOrigin = findViewById(R.id.txtOrigin)
        txtDestination = findViewById(R.id.txtDestination)
        btnShowRoute = findViewById(R.id.btn_ShowRoute)
        btnCreate = findViewById(R.id.btn_createRide)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient = getFusedLocationProviderClient(this)
        loadingBar = findViewById(R.id.loading)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(RIDES).push()
    }


    override fun onDestroy() {
        super.onDestroy()
        //  stopLocationUpdates()
        Log.d("STATE", "onDestroy")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()

    }

    override fun onStart() {
        super.onStart()
        mapFragment.getMapAsync(this)
        /// startLocationUpdates()
        txtOrigin.setAdapter(placeAutocompleteAdapter)
        txtDestination.setAdapter(placeAutocompleteAdapter)
        btnShowRoute.setOnClickListener { _ ->
            if (validateFields()) {
                showProgressBar()
                val origin = txtOrigin.text.toString()
                val destination = txtDestination.text.toString()
                drawMap(origin, destination)
                hideProgressBar()
            } else {
                Toast.makeText(this, getString(R.string.completeFields), Toast.LENGTH_SHORT).show()
            }
        }
        btnCreate.setOnClickListener({ _ ->
            if (validateFields()) {
                showProgressBar()
                ride.origin = txtOrigin.text.toString()
                ride.destination = txtDestination.text.toString()
                ride.id = databaseReference.key!!
                databaseReference.setValue(ride).addOnSuccessListener {
                    sendNotification()
                    databaseReference.database.reference
                            .child(USERS)
                            .child(ride.user)
                            .child("activeRide")
                            .setValue(ride).addOnCompleteListener {
                                if (it.isComplete) {
                                    databaseReference.database.reference.child(USERS)
                                            .child(ride.user)
                                            .child("taked").setValue(1)
                                    startActivity(Intent(baseContext, MainActivity::class.java))
                                    finish()
                                }
                            }.addOnFailureListener {
                                Toasty.error(applicationContext, "Error when save active ride", Toast.LENGTH_SHORT, true).show()
                            }
                }
                hideProgressBar()
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
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadingBar.visibility = View.GONE
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    currentPositionMarker()
                    mMap.isMyLocationEnabled = true

                }
            } else {
                currentPositionMarker()

            }
        }
    }
    private fun currentPositionMarker() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    //convertCoordinateToAddress(location)
                    lastLocation = location
                    with(mMap) {
                        addMarker(MarkerOptions()
                                .position(LatLng(location!!.latitude, location!!.longitude))
                                .title(getString(R.string.your_position)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_marker)))
                        val latLng = LatLng(location.latitude, location.longitude)
                        var cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        animateCamera(cameraUpdate)
                        Log.d("HERE", "I AM HERE")

                    }
                }
            }
        }
    }


    private fun drawMap(origin: String, destination: String) {
        InputsHelper.hideKeyboard(applicationContext, window.decorView.rootView)
        try {
            val results = MapDrawHelper.getDirectionsDetails(origin, destination,
                    TravelMode.DRIVING, getString(R.string.apiKey))
            mMap.clear()
            MapDrawHelper.setupGoogleMapScreenSettings(mMap)
            MapDrawHelper.addPolyline(results!!, mMap)
            MapDrawHelper.positionCamera(results.routes[overview], mMap)
            MapDrawHelper.addMarkersToMap(results, mMap, activity = this@MapsActivity)
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

    private fun sendNotification() {
        with(communityDal) {
            getCommunity(ride.community, object : CommunityCallback {
                override fun getCommunity(community: Community) {
                    val users: List<String> = community.users.filterNot { it == MainActivity.userId }
                    getCommunityTokens(users as ArrayList<String>
                            , object : TokenCallback {
                        override fun getCommunityTokens(tokenList: ArrayList<String>) {
                            if (ride.type.toString() == Type.OFFERED.toString()) {
                                NotificationHelper.messageToCommunity(MainActivity.fmc, tokenList,
                                        resources.getString(R.string.new_ride_offered),
                                        resources.getString(R.string.new_ride_offered_body) + " " + communityDescription)
                            } else {
                                NotificationHelper.messageToCommunity(MainActivity.fmc, tokenList,
                                        resources.getString(R.string.new_ride_requested),
                                        resources.getString(R.string.new_ride_requested_body) + " " + communityDescription)

                            }
                        }

                    })
                }
            })
        }
    }

    private fun showProgressBar() {
        materialDialog.show()
        PermissionHelper.disableScreenInteraction(window)
    }

    private fun hideProgressBar() {
        materialDialog.dismiss()
        PermissionHelper.enableScreenInteraction(window)
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

    private fun getPermissionLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
    }
    private fun locationRequest(): LocationRequest {
        var locationRequest = LocationRequest()
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            with(locationRequest) {
                priority = PRIORITY_BALANCED_POWER_ACCURACY
                fastestInterval = 1000
                interval = 1000
                maxWaitTime = 1000

            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        with(mMap) {
                            addMarker(MarkerOptions()
                                    .position(LatLng(location!!.latitude, location!!.longitude))
                                    .title(getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(207f)))
                            val latLng = LatLng(location.latitude, location.longitude)
                            var cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                            animateCamera(cameraUpdate)
                            Log.d("HERE", "I AM HERE")

                        }
                        Toast.makeText(applicationContext, "HERE:${location.latitude}${location.latitude}", Toast.LENGTH_LONG).show()
                        Looper.myLooper()
                        // ...
                    }
                }
            }
        }
        return locationRequest

    }

    override fun onPause() {
        super.onPause()
        ConnectivityHelper.safelyDispose(connectivityDisposable)
        //  stopLocationUpdates()
        Log.d("STATE", "onPause")
    }

    override fun onResume() {
        super.onResume()
        connectivityDisposable = ConnectivityHelper.networkConnectionState(applicationContext, this@MapsActivity)
        // startLocationUpdates()
        Log.d("STATE", "onResume")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    //  startLocationUpdates()
                }
                PackageManager.PERMISSION_DENIED -> {
                    Toasty.warning(applicationContext, "Es necesario el permiso para mejorar la experiencia", Toast.LENGTH_LONG, true).show()
                }
            }
        }

    }


}
