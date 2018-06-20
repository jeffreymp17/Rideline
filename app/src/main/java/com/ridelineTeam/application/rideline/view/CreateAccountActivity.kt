package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.PlaceAutocompleteAdapter
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_account.*


class CreateAccountActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var txtName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var txtPasswordLayout: TextInputLayout
    private lateinit var layoutConfirmPassword:TextInputLayout
    private lateinit var txtAddress: AutoCompleteTextView
    private lateinit var btnCreateAccount:Button
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var txtTelephone:EditText
    private var txtValidatePassword=""
    private lateinit var mAuth : FirebaseAuth
    private lateinit var txtTerms:TextView

    private lateinit var placeAutocompleteAdapter: PlaceAutocompleteAdapter
    private lateinit var mGoogleApiClient: GoogleApiClient

    private lateinit var checkTerms:CheckBox

    private var LAT_LONG_BOUNDS = LatLngBounds(
            LatLng((-40).toDouble(), (-168).toDouble()),
            LatLng((71).toDouble(), (136).toDouble())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        FragmentHelper.showToolbar(getString(R.string.createAccountActivity),true,findViewById(R.id.toolbar),this)
        initializePropertiesAndFunctions()
        btnCreateAccount.setOnClickListener{_ ->createUser()}
    }

    private fun initializePropertiesAndFunctions(){

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()

        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this,mGoogleApiClient,LAT_LONG_BOUNDS,null)
        txtName = findViewById(R.id.txtName)
        txtLastName = findViewById(R.id.txtLast_names)
        txtEmail = findViewById(R.id.txtEmail)
        txtPasswordLayout = findViewById(R.id.layoutPassword)
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)
        txtAddress = findViewById(R.id.txtAddress)
        txtAddress.setAdapter(placeAutocompleteAdapter)
        txtTelephone=findViewById(R.id.txtTelephone)
        progressBar = findViewById(R.id.progressBar)
        btnCreateAccount = findViewById(R.id.btn_createAccount)
        layoutConfirmPassword=findViewById(R.id.layoutConfirmPassword)
        txtTerms = findViewById(R.id.txtTerms)
        txtTerms.setOnClickListener({showTerms()})
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child(USERS)
        dbReference.keepSynced(true)
        checkTerms = findViewById(R.id.checkTerms)
        errorPassword(txtPasswordLayout)
        confirmPassword(layoutConfirmPassword)
    }
    private fun confirmPassword(confirmPassword:TextInputLayout){
        confirmPassword.editText!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                txtValidatePassword= txtPassword.text.toString()
                if (p0.toString() != txtValidatePassword) {
                    btn_createAccount.isEnabled=false
                    confirmPassword.error = getString(R.string.passwordMatchError)
                }
                else{
                    confirmPassword.isErrorEnabled = false
                    btn_createAccount.isEnabled=true
                    confirmPassword.error = ""
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0.toString()
            }
        })
    }

    private fun showTerms(){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.terms_conditions, null)
        dialogBuilder.setView(dialogView)
        val builder = dialogBuilder.create()
        builder.show()
    }

    private fun errorPassword(password: TextInputLayout) {
        password.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().length < 6) {
                    password.error = getString(R.string.passwordLengthError)
                } else {
                    password.isErrorEnabled = false
                    password.error = ""
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    private fun createUser() {
        if (validateFields()) {
            val user = User()
            user.apply {
                name = txtName.text.toString().capitalize()
                lastName = txtLastName.text.toString().capitalize()
                email = txtEmail.text.toString()
                address = txtAddress.text.toString()
                telephone=Integer.parseInt(txtTelephone.text.toString())
                status= getString(R.string.defaultStatus)
            }
            progressBar.visibility = View.VISIBLE
            mAuth.createUserWithEmailAndPassword(user.email, txtPassword.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isComplete) {
                    val newUser: FirebaseUser? = mAuth.currentUser
                    verifyEmail(newUser)
                    user.id = newUser?.uid!!
                    val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(user.name+" "+user.lastName)
                            .build()
                    newUser.updateProfile(profileUpdate)
                    dbReference.child(user.id).setValue(user)
                    progressBar.visibility = View.GONE
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    Toasty.error(this, "IT WAS NOT POSSIBLE TO REGISTER", Toast.LENGTH_SHORT,true).show()
                }
            }
        }else{
            Toasty.warning(this,"Please complete user information",Toast.LENGTH_SHORT,true).show()
        }
    }


    private fun validateFields():Boolean{
        when {
            TextUtils.isEmpty(txtName.text) -> {
                txtName.error = "Required field"
                return false
            }
            TextUtils.isEmpty(txtLastName.text) -> {
                txtLastName.error = "Required field"
                return false
            }
            TextUtils.isEmpty(txtEmail.text) -> {
                txtEmail.error = "Required field"
                return false
            }
            TextUtils.isEmpty(txtPassword.text) -> {
                txtPassword.error = "Required field"
                return false
            }
            TextUtils.isEmpty(txtConfirmPassword.text) -> {
                txtConfirmPassword.error = "Required field"
                return false
            }
            TextUtils.isEmpty(txtTelephone.text) -> {
                txtTelephone.error = "Required field"
                return false
            }
            TextUtils.isEmpty(txtAddress.text)->{
                txtAddress.error="Required field"
                return false
            }
            !checkTerms.isChecked ->{
                Toasty.warning(this,"You must accept terms and conditions",
                        Toast.LENGTH_LONG,true).show()
                return false
            }
            else ->{
                return true
            }
        }
    }

    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if (task.isComplete) {
                Toast.makeText(this, "Done",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "ERROR",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
}
