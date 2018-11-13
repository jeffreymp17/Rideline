package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mukesh.countrypicker.CountryPicker
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.dataAccessLayer.interfaces.UserCallback
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.InputsHelper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_update_profile.*
import com.ridelineTeam.application.rideline.dataAccessLayer.User as UserDal

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var  countryPicker : CountryPicker
    private lateinit var  toolbar       : android.support.v7.widget.Toolbar
    private lateinit var  countryCode : String
    private lateinit var  currentUser: FirebaseUser
    private lateinit var userDal:UserDal
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        currentUser = FirebaseAuth.getInstance().currentUser!!
        userDal = UserDal(this@UpdateProfileActivity)
        user = User()
        initToolbar()
        initCountryPicker()
    }

    override fun onStart() {
        super.onStart()
        InputsHelper.required(txtProfileNameLayout,resources)
        InputsHelper.required(txtProfileLastNamesLayout,resources)
        userDal.getUser(
            currentUser.uid,
            object : UserCallback {
                override fun onGetUserCallback(user: User) {
                    this@UpdateProfileActivity.user = user
                    loadUserData()
                }
            }
        )
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

    private fun initToolbar(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Edit Profile"
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_delete)
        FragmentHelper.backButtonToFragment(toolbar, PeopleRideDetailActivity@ this)
    }

    private fun initCountryPicker(){
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

    private fun loadUserData(){
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
            user.name=txtProfileName.text.toString()
            user.lastName=txtProfileLastNames.text.toString()
            user.status = txtProfileStatus.text.toString()
            user.country=countryCode
            userDal.update(user)
            finish()
        }

    }

}
