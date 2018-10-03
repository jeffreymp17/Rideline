package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
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
import com.ridelineTeam.application.rideline.util.helpers.InputsHelper
import com.ridelineTeam.application.rideline.view.fragment.ProfileFragment
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_update_profile.*


class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var  countryPicker : CountryPicker
    private lateinit var  toolbar       : android.support.v7.widget.Toolbar
    private lateinit var  countryCode: String
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
                        .listener {
                            txtCountryName.text=it.name
                            Picasso.with(UpdateProfileActivity@this)
                                    .load(it.flag)
                                    .fit()
                                    .into(imgCountryFlag)
                            countryCode = it.code
                        }.build()

        cardCountry.setOnClickListener {
            countryPicker.showDialog(supportFragmentManager)
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = MaterialDialog.Builder(this)
                .title("New car")
                .customView(R.layout.cars_dialog_view, true)
                .positiveText("register")
                .build()
        InputsHelper.required(txtProfileNameLayout,resources)
        InputsHelper.required(txtProfileLastNamesLayout,resources)

        getUserProfile()

        fabNewCard.setOnClickListener{
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.update_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_update_profile -> {
                updateProfile()
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
        countryCode = user.country
        txtProfileName.setText(user.name, TextView.BufferType.EDITABLE)
        txtProfileLastNames.setText(user.lastName, TextView.BufferType.EDITABLE)
        txtProfileStatus.setText(user.status, TextView.BufferType.EDITABLE)
        val country = countryPicker.getCountryByISO(countryCode)
        txtCountryName.text = country.name
        Picasso.with(UpdateProfileActivity@this)
                .load(country.flag)
                .fit()
                .into(imgCountryFlag)
        txtProfileTelephone.setText(user.telephone.toString(),TextView.BufferType.EDITABLE)
        txtProfileEmail.setText(user.email,TextView.BufferType.EDITABLE)
    }

    private fun updateProfile() {
        var update = true
        if (TextUtils.isEmpty(txtProfileName.text)){
            txtProfileName.error = resources.getString(R.string.requiredFieldMessage)
            update = false
        }

        if (TextUtils.isEmpty(txtProfileLastNames.text)){
            txtProfileLastNamesLayout.error = resources.getString(R.string.requiredFieldMessage)
            update = false
        }
        if(update){
            val id = FirebaseAuth.getInstance().currentUser!!.uid
            val reference = FirebaseDatabase.getInstance().reference.child(USERS)
            reference.child(id).child("name").setValue(txtProfileName.text.toString())
            reference.child(id).child("lastName").setValue(txtProfileLastNames.text.toString())
            reference.child(id).child("status").setValue(txtProfileStatus.text.toString())
            reference.child(id).child("country").setValue(countryCode)
            Toasty.success(this, getString(R.string.profileUpdate), Toast.LENGTH_SHORT, true).show()
            FragmentHelper.changeFragment(ProfileFragment(),supportFragmentManager)
            finish()
        }

    }

    fun registerCar(){

    }
}
