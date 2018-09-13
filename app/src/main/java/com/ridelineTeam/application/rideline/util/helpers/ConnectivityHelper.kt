package com.ridelineTeam.application.rideline.util.helpers

import android.content.Context
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by jeffry on 11/09/18.
 */
class ConnectivityHelper {

    companion object {
        private var connectivityDisposable: Disposable? = null
        private var internetDisposable: Disposable? = null

        fun checkConnectionStatus(context: Context) {
            internetDisposable = ReactiveNetwork.observeInternetConnectivity()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { isConnectedToInternet ->
                        Log.d("CONNECTED", "IS" + isConnectedToInternet.toString())
                    }
        }
         fun safelyDispose(disposable: Disposable?) {
            if (disposable != null && !disposable.isDisposed) {
                disposable.dispose()
            }
        }


        fun isOnline(): Boolean {
            try {
                return Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8").waitFor() == 0 //  "8.8.8.8" is the server to ping
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }
    }
}