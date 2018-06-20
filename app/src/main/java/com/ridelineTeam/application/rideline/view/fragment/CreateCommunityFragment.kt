package com.ridelineTeam.application.rideline.view.fragment


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.MainActivity

import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.files.DATE_FORMAT
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.User
import java.text.SimpleDateFormat
import java.util.*


class CreateCommunityFragment : Fragment() {
    private lateinit var txtCommunityName: TextView
    private lateinit var txtCommunityDescription: TextView
    private lateinit var btnCreateCommunity: Button
    private lateinit var  database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_create_community, container, false)
        txtCommunityName =  rootView.findViewById(R.id.txtCommunityName)
        txtCommunityDescription = rootView.findViewById(R.id.txtCommunityDescription)
        btnCreateCommunity= rootView.findViewById(R.id.btn_createCommunity)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(COMMUNITIES).push()
        return rootView
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        btnCreateCommunity.setOnClickListener({
            if (validateFields()){
                val community= Community()
                community.apply {
                    name= txtCommunityName.text.toString().toLowerCase()
                    description=txtCommunityDescription.text.toString().capitalize().replace("\n","")
                    createdBy=user!!.uid
                    users.add(user.uid)
                    createdDate= SimpleDateFormat(DATE_FORMAT).format(Date())
                    id=databaseReference.key.toString()
                }
                databaseReference.setValue(community).addOnSuccessListener {
                    addCommunityToCreator(databaseReference.key!!,user!!.uid )
                    startActivity(Intent(context, MainActivity::class.java))

                }
            }
        })

    }
    private fun validateFields():Boolean{
        return when {
            txtCommunityName.text.toString().trim().isEmpty() -> {
                txtCommunityName.error="Required field"
                false
            }
            txtCommunityDescription.text.toString().trim().isEmpty() -> {
                txtCommunityDescription.error="Required field"
                false
            }
            else -> true
        }
    }

    private fun addCommunityToCreator(key: String,uid:String ){
        val db = databaseReference.database.reference.child(USERS)
        db.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                user!!.communities.add(key)
                db.child(uid).setValue(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }
}// Required empty public constructor
