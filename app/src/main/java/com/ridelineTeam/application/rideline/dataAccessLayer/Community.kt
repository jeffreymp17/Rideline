package com.ridelineTeam.application.rideline.dataAccessLayer

import android.app.Activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.CommunityCallback
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.TokenCallback
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.UserCallback
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.files.FirebaseUtils
import com.ridelineTeam.application.rideline.util.files.TOKEN
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper
import es.dmoral.toasty.Toasty

class Community(private val activity: Activity) {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val currentUser:FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    fun getCommunity(communityId: String, communityCallback: CommunityCallback) {
        FirebaseUtils.getDatabase().getReference(COMMUNITIES)
                .child(communityId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toasty.error(activity.baseContext, "ERROR", Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(data: DataSnapshot) {
                        val community = data.getValue(Community::class.java)
                        communityCallback.getCommunityUsers(community!!)
                    }

                })
    }
    fun getCommunityTokens(tokenList:ArrayList<String>,tokenCallback: TokenCallback){
        val tokens=ArrayList<String>()
        FirebaseUtils.getDatabase().getReference(USERS)
                .addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(activity.baseContext, "ERROR", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (items in tokenList) {
                    tokens.add(dataSnapshot.child(items).child(TOKEN).value.toString())
                }
                tokenCallback.getCommunityTokens(tokens)
            }

        })
    }
    fun join(communityId: String){
        val communityRef = database.reference.child(COMMUNITIES).child(communityId)
        communityRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val community = dataSnapshot.getValue(Community::class.java)
                community!!.users.add(currentUser.uid)
                communityRef.setValue(community)

               val userDal = com.ridelineTeam.application.rideline.dataAccessLayer.User(activity)
                userDal.getUser(community.admin,object:UserCallback{
                    override fun onGetUserCallback(user: User) {
                        NotificationHelper.message(com.ridelineTeam.application.rideline.MainActivity.fmc,
                                user.token, activity.resources.getString(R.string.join_title),
                                currentUser.displayName + " " +
                                        activity.resources.getString(R.string.join_body))
                    }
                })
                userDal.addCommunity(communityId)
            }
        })

    }

}