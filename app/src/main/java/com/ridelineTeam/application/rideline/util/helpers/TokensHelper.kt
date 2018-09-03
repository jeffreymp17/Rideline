package com.ridelineTeam.application.rideline.util.helpers

import android.util.Log
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.ridelineTeam.application.rideline.util.files.TOKEN

/**
 * Created by jeffry on 02/09/18.
 */
class TokensHelper {

    companion object {
         fun userToken(userId:String,reference: DatabaseReference):String{
             var token=""
             val dbSource = TaskCompletionSource<DataSnapshot>()
             val dbTask = dbSource.task
             val db = reference.child(userId)
             db.child(TOKEN).addValueEventListener(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(userToken: DataSnapshot) {
                    dbSource.setResult(userToken)

                }

            })
             dbTask.addOnCompleteListener {
                 val dataSnapshot=dbTask.result
                 if(dataSnapshot.exists())
                     token=dataSnapshot.value as String
                 Log.d("Token","is:${dataSnapshot.value}")
             }
            return token
        }
    }
}