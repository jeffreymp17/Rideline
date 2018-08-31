package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mukesh.countrypicker.CountryPicker
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var  countryPicker : CountryPicker
    private lateinit var  toolbar       : android.support.v7.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Edit Profile"
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_delete)

        FragmentHelper.backButtonToFragment(toolbar, PeopleRideDetailActivity@ this)

        countryPicker = CountryPicker.Builder()
                        .with(UpdateProfileActivity@this)
                        .listener({
                            Log.d("Country code",it.code)
                            txtCountryName.text=it.name
                            Picasso.with(UpdateProfileActivity@this)
                                    .load(it.flag)
                                    .fit()
                                    .into(imgCountryFlag)
                        }).build()

        cardCountry.setOnClickListener {
            countryPicker.showDialog(supportFragmentManager)
        }
    }

    override fun onStart() {
        super.onStart()
        getUserProfile()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_update_profile -> {
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getUserProfile() {
        val id = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference.child(USERS)
        reference.child(id).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                val user = data.getValue(User::class.java)
                loadData(user!!)
            }

        })
    }

    private fun loadData(user:User){
        txtProfileName.setText(user.name, TextView.BufferType.EDITABLE)
        txtProfileLastNames.setText(user.lastName, TextView.BufferType.EDITABLE)
        txtProfileStatus.setText(user.status, TextView.BufferType.EDITABLE)
        val country = countryPicker.getCountryByISO(user.country)
        txtCountryName.text = country.name
        Picasso.with(UpdateProfileActivity@this)
                .load(country.flag)
                .fit()
                .into(imgCountryFlag)
        txtProfileTelephone.setText(user.telephone.toString(),TextView.BufferType.EDITABLE)
        txtProfileEmail.setText(user.email,TextView.BufferType.EDITABLE)
    }
}
