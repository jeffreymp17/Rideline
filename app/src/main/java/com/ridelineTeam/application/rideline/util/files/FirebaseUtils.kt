package com.ridelineTeam.application.rideline.util.files

import com.google.firebase.database.FirebaseDatabase

/**
 * Created by jeffry on 07/10/18.
 */
class FirebaseUtils {
    companion object {
        private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

        init {
            firebaseDatabase.setPersistenceEnabled(true)
        }

        fun getDatabase() : FirebaseDatabase {
            return firebaseDatabase
        }
    }

}