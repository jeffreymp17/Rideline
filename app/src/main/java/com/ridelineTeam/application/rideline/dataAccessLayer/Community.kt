package com.ridelineTeam.application.rideline.dataAccessLayer

import android.app.Activity
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.CommunityCallback
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.TokenCallback
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.files.FirebaseUtils
import com.ridelineTeam.application.rideline.util.files.TOKEN
import com.ridelineTeam.application.rideline.util.files.USERS
import es.dmoral.toasty.Toasty

/**
 * Created by jeffry on 13/11/18.
 */
class Community(private val activity: Activity) {
    fun getCommunity(communityId: String, communityCallback: CommunityCallback) {
        FirebaseUtils.getDatabase().getReference(COMMUNITIES)
                .child(communityId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        if (databaseError != null) {
                            Toasty.error(activity.baseContext, "ERROR", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onDataChange(data: DataSnapshot) {
                        val community = data.getValue(Community::class.java)
                        communityCallback.getCommunityUsers(community!!)
                    }

                })
    }
    fun getCommunityTokens(tokenList:ArrayList<String>,tokenCallback: TokenCallback){
        var tokens=ArrayList<String>()
        FirebaseUtils.getDatabase().getReference(USERS)
                .addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                if (databaseError != null) {
                    Toasty.error(activity.baseContext, "ERROR", Toast.LENGTH_LONG).show()
                }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (items in tokenList) {
                    tokens.add(dataSnapshot.child(items).child(TOKEN).value.toString())
                }
                tokenCallback.getCommunityTokens(tokens)
            }

        })
    }
}