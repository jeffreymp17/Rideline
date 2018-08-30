package com.ridelineTeam.application.rideline.util.helpers

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.files.LOCATION_SETTINGS_REQUEST


class FragmentHelper {
    companion object {
        fun changeFragment(fragment: Fragment, manager: FragmentManager) {
            val backStateName = fragment.javaClass.name
            val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)
            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                manager.beginTransaction()
                        .replace(R.id.containerMain, fragment, backStateName)
                        .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(backStateName)
                        .commit()
            }
        }

        fun showToolbar(tittle: String, upButton: Boolean, view: Toolbar, activity: Activity) {
            (activity as AppCompatActivity).setSupportActionBar(view)
            activity.supportActionBar!!.title = tittle
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(upButton)
        }
         fun backButtonToFragment(toolbar:Toolbar,activity:Activity){
            toolbar.setNavigationOnClickListener { activity.onBackPressed() }
        }
        fun startGPS(activity: Activity) {
            val mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval((10 * 1000).toLong())
                    .setFastestInterval((1 * 1000).toLong())
            val settingsBuilder = LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest)
            settingsBuilder.setAlwaysShow(true)
            val client: SettingsClient = LocationServices.getSettingsClient(activity)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(settingsBuilder.build())
            task.addOnCompleteListener {
                try {
                    task.getResult(ApiException::class.java)
                    Log.d("activado","GPS")
                } catch (ex: ApiException) {
                    when (ex.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                val resolvableApiException = ex as ResolvableApiException
                                resolvableApiException
                                        .startResolutionForResult(activity,
                                                LOCATION_SETTINGS_REQUEST)
                                Log.d("myLocations:", "hwe---------------------")
                            } catch (e: IntentSender.SendIntentException) {
                                Log.d("myLocations:", "NOOOOOO")

                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                        }
                        LocationSettingsStatusCodes.SUCCESS->{
                            Log.d("activado","GPS")
                        }


                    }
                }
            }
        }

    }
}

