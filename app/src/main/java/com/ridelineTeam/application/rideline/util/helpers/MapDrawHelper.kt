package com.ridelineTeam.application.rideline.util.helpers

import android.Manifest
import android.app.Activity
import android.location.Location
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import org.joda.time.DateTime
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.model.CameraPosition




class MapDrawHelper {
    companion object Map {
        var overview: Int = 0

        fun setupGoogleMapScreenSettings(mMap: GoogleMap) {
            mMap.isBuildingsEnabled = true
            mMap.isIndoorEnabled = true
            mMap.isTrafficEnabled = false
            mMap.mapType=GoogleMap.MAP_TYPE_NORMAL
            val mUiSettings = mMap.uiSettings
            mUiSettings.isZoomControlsEnabled = false
            mUiSettings.isCompassEnabled = true
            mUiSettings.isMyLocationButtonEnabled = true
            mUiSettings.isScrollGesturesEnabled = true
            mUiSettings.isZoomGesturesEnabled = true
            mUiSettings.isTiltGesturesEnabled = true
            mUiSettings.isRotateGesturesEnabled = true
        }

        fun addPolyline(results: DirectionsResult, mMap: GoogleMap) {
            val decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.encodedPath)
            mMap.addPolyline(PolylineOptions().addAll(decodedPath))
        }

        fun positionCamera(route: DirectionsRoute, mMap: GoogleMap) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(
                    route.legs[overview].startLocation.lat,
                    route.legs[overview].startLocation.lng), 9f))
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                    .target(mMap.cameraPosition.target)
                    .zoom(17f)
                    .bearing(30f)
                    .tilt(45f)
                    .build()))
        }

        fun addMarkersToMap(results: DirectionsResult, mMap: GoogleMap) {
            with(mMap) {
                addMarker(MarkerOptions().position(LatLng(
                        results.routes[overview].legs[overview].startLocation.lat,
                        results.routes[overview].legs[overview].startLocation.lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(207f))
                        .title(results.routes[overview].legs[overview].startAddress)).showInfoWindow()
                addMarker(MarkerOptions().position(LatLng(
                        results.routes[overview].legs[overview].endLocation.lat,
                        results.routes[overview].legs[overview].endLocation.lng))
                        .title(results.routes[overview].legs[overview].endAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(45.0f))
                        .snippet(getEndLocationTitle(results))).showInfoWindow()
            }

        }

        fun getDirectionsDetails(origin: String, destination: String, mode: TravelMode, apiKey: String): DirectionsResult? {
            val now = DateTime()
            try {
                return DirectionsApi.newRequest(getGeoContext(apiKey))
                        .mode(mode)
                        .origin(origin)
                        .destination(destination)
                        .departureTime(now)
                        .await()
            } catch (e: ApiException) {
                e.printStackTrace()
                return null
            } catch (e: InterruptedException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        fun getCurrentLocation(activity: Activity): LatLng {

            var current = LatLng(10.134894, -85.446627)
            val mFusedLocationClient = FusedLocationProviderClient(activity)
            if (PermissionHelper.checkPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    current = LatLng(location!!.latitude, location.longitude)
                }
            }
            return current
        }

        private fun getEndLocationTitle(results: DirectionsResult): String {
            return "Time :" + results.routes[overview].legs[overview].duration.humanReadable +
                    " Distance :" + results.routes[overview].legs[overview].distance.humanReadable
        }

        private fun getGeoContext(apiKey: String): GeoApiContext {

            val geoApiContext = GeoApiContext()
            return geoApiContext.setQueryRateLimit(3)
                    .setApiKey(apiKey)
                    .setConnectTimeout(1, TimeUnit.SECONDS)
                    .setReadTimeout(1, TimeUnit.SECONDS)
                    .setWriteTimeout(1, TimeUnit.SECONDS)
        }
    }
}