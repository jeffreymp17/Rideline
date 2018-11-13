package com.ridelineTeam.application.rideline.dataAccessLayer

import android.app.Activity
import android.widget.Toast
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.UserCallback
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.files.USERS
import es.dmoral.toasty.Toasty


class User(private val activity: Activity){
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getUser(id: String, userCallback: UserCallback) {
        val reference = FirebaseDatabase.getInstance().reference.child(USERS).child(id)
        reference.runTransaction(object: Transaction.Handler {
            override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, data: DataSnapshot?) {
                if (databaseError!=null){
                    Toasty.error(activity.baseContext,"ERROR",Toast.LENGTH_LONG).show()
                }
                if (boolean){
                    val user = data!!.getValue(User::class.java)
                    userCallback.onGetUserCallback(user!!)
                }
            }
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val auxUser = mutableData.getValue(User::class.java)
                return if (auxUser == null)
                    Transaction.abort()
                else
                   return Transaction.success(mutableData)
            }
        })
    }

    fun update(user:User){
        val reference = database.reference.child(USERS).child(user.id)
        reference.child("name").setValue(user.name)
        reference.child("lastName").setValue(user.lastName)
        reference.child("status").setValue(user.status)
        reference.child("country").setValue(user.country)
        Toasty.success(activity, activity.getString(R.string.profileUpdate),
                Toast.LENGTH_SHORT, true).show()
    }
}