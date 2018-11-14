package com.ridelineTeam.application.rideline.view.fragment


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import co.ceryle.radiorealbutton.RadioRealButton
import co.ceryle.radiorealbutton.RadioRealButtonGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.MainActivity

import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.files.DATE_FORMAT
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.enums.CommunityType
import java.text.SimpleDateFormat
import java.util.*
import com.ridelineTeam.application.rideline.dataAccessLayer.Community as CommunityDal



class CreateCommunityFragment : Fragment() {
    private lateinit var txtCommunityName: TextView
    private lateinit var txtCommunityDescription: TextView
    private lateinit var btnCreateCommunity: Button
    private lateinit var  database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var communityDal:CommunityDal
    private lateinit var radioTypePublic: RadioRealButton
    private lateinit var radioTypePrivate: RadioRealButton
    private lateinit var radioGroupType: RadioRealButtonGroup
    private var typeOfRide: CommunityType =  CommunityType.PRIVATE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_create_community, container, false)
        communityDal=CommunityDal(activity!!)
        txtCommunityName =  rootView.findViewById(R.id.txtCommunityName)
        txtCommunityDescription = rootView.findViewById(R.id.txtCommunityDescription)
        btnCreateCommunity= rootView.findViewById(R.id.btn_createCommunity)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(COMMUNITIES).push()
        radioGroupType = rootView.findViewById(R.id.radioGroupType)
        radioTypePrivate = rootView.findViewById(R.id.radioTypePrivate)
        radioTypePublic = rootView.findViewById(R.id.radioTypePublic)
        return rootView
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title=getString(R.string.new_community)
    }

    override fun onStart() {
        super.onStart()
        createCommunity()

    }

    private fun createCommunity() {
        val user = FirebaseAuth.getInstance().currentUser
        radioGroupType.setOnClickedButtonListener { _, position ->
            typeOfRide = if (position == 1) {
                CommunityType.PUBLIC
            } else {
              CommunityType.PRIVATE

            }
        }
        btnCreateCommunity.setOnClickListener({
            if (validateFields()) {
                val community = Community()
                community.apply {
                    name = txtCommunityName.text.toString().toLowerCase()
                    description = txtCommunityDescription.text.toString().capitalize().replace("\n", "")
                    createdBy = user!!.uid
                    admin = user.uid
                    users.add(user.uid)
                    createdDate = SimpleDateFormat(DATE_FORMAT).format(Date())
                    id = databaseReference.key.toString()
                    type=typeOfRide
                }
                communityDal.saveCommunity(community, user!!.uid)
                startActivity(Intent(context, MainActivity::class.java))

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
}// Required empty public constructor
