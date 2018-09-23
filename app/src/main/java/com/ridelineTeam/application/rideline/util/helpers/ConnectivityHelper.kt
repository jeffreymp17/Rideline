package com.ridelineTeam.application.rideline.util.helpers

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.design.widget.Snackbar
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.ridelineTeam.application.rideline.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by jeffry on 11/09/18.
 */
class ConnectivityHelper {

    companion object {
       private var snack: Snackbar? = null
        fun networkConnectionState(context: Context, activity: Activity): Disposable {
            return ReactiveNetwork
                    .observeNetworkConnectivity(context)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it.state() != NetworkInfo.State.CONNECTED || !connection(context)) {
                            snack = this.messageConnectionState(activity)
                            snack!!.show()
                        } else {
                            if (snack != null)
                                this.snack!!.dismiss()

                        }

                        Log.d("internet state", "Available:${it.available()}-->State:${it.state()}other:{${it.detailedState()}")
                    }
        }

        fun safelyDispose(disposable: Disposable?) {
            if (disposable != null && !disposable.isDisposed) disposable.dispose()
        }

        fun connection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }

        fun messageConnectionState(activity: Activity): Snackbar {
            return Snackbar.make(activity.findViewById(android.R.id.content), // Parent view
                    activity.getString(R.string.connectonStateError),// Message to show
                    Snackbar.LENGTH_INDEFINITE // How long to display the message.
            )

            //Toasty.success(activity.applicationContext, activity.getString(R.string.connectionState), Toast.LENGTH_LONG).show()
            //Toasty.error(activity.applicationContext, activity.getString(R.string.connectonStateError), Toast.LENGTH_LONG).show()

        }


    }
}
//}