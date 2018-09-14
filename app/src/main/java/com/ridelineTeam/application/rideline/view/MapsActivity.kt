package com.ridelineTeam.application.rideline.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.maps.model.TravelMode
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.MainActivity.Companion.PERMISSION_REQUEST_ACCESS_FINE_LOCATION
import com.ridelineTeam.application.rideline.MainActivity.Companion.PLACE_PICKER_REQUEST
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.PlaceAutocompleteAdapter
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.files.*
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.MapDrawHelper
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper
import com.ridelineTeam.application.rideline.util.helpers.PermissionHelper
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
    private lateinit var manager: LocationManager
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var listOfTokens = ArrayList<String>()
    private var usersIds = ArrayList<String>()
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

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toasty.error(this, p0.errorMessage.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        ride = intent.getSerializableExtra("rideObject") as Ride
        val country = intent.getStringExtra("country")
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
        Log.d("gettingObject", ride.toString())
        Log.d("MY COMMUNITY::", "" + getCommunityForNotification(ride.community))
        // currentPosition()
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        getPermissionLocation()
    }



    override fun onDestroy() {
        super.onDestroy()
        //  stopLocationUpdates()
        Log.d("STATE", "onDestroy")
    }
    /*
       private fun stopLocationUpdates() {
           fusedLocationClient.removeLocationUpdates(
                   locationCallback)
       }

         override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
              super.onActivityResult(requestCode, resultCode, data)
              Log.d("STARTGPS", "onActivityResult() called with: requestCode = [$requestCode], resultCode = [$resultCode], data = [$data]");
              if (resultCode == Activity.RESULT_OK) {
                  Log.d("STARTGPS", "ENBLED")
                 /// permissionGranted=true
                 // getLocation()
                  startLocationUpdates()

              } else if (resultCode == Activity.RESULT_CANCELED) {
                  Toasty.warning(applicationContext, getString(R.string.permissionGPS), Toast.LENGTH_LONG, true).show()

              }
          }*/
    /* private fun currentPosition() {
           if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
               FragmentHelper.startGPS(this@MapsActivity)

           } else {
             locationRequest()
           }
       }*/
    //else {
    //  startLocationUpdates()
    /*     locationListener = object : LocationListener {
             override fun onLocationChanged(location: Location?) {
                 with(mMap) {
                     addMarker(MarkerOptions()
                             .position(LatLng(location!!.latitude, location!!.longitude))
                             .title("You are Here").icon(BitmapDescriptorFactory.defaultMarker(207f)))
                     val latLng = LatLng(location.latitude, location.longitude)
                     var cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                     animateCamera(cameraUpdate)
                     manager.removeUpdates(locationListener)
                     Log.d("HERE", "I AM HERE")

                 }
             }

             override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
             }

             override fun onProviderEnabled(provider: String?) {
             }

             override fun onProviderDisabled(provider: String?) {
             }

         }

         manager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 0, 0f, locationListener)
*/

    //}

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
                     getCommunityUsers()
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
        loadingBar.visibility = View.GONE
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                currentPositionMarker()
            }
        } else {
            currentPositionMarker()

        }
    }

    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                //   placeMarkerOnMap(place.latLng)
            }
        }
    }

    private fun currentPositionMarker() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    with(mMap) {
                        isMyLocationEnabled = true
                        addMarker(MarkerOptions()
                                .position(LatLng(location!!.latitude, location!!.longitude))
                                .title(getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(207f)))
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
                val users = usersIds.filterNot { it == MainActivity.userId }
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
                            resources.getString(R.string.new_ride_offered),
                            resources.getString(R.string.new_ride_offered_body) + " " + communityDescription)
                } else {
                    NotificationHelper.messageToCommunity(MainActivity.fmc, listOfTokens,
                            resources.getString(R.string.new_ride_requested),
                            resources.getString(R.string.new_ride_requested_body) + " " + communityDescription)

                }
            }
        })
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

    private fun startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest(),
                    locationCallback, Looper.myLooper())

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
        //  stopLocationUpdates()
        Log.d("STATE", "onPause")
    }

    override fun onResume() {
        super.onResume()
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
