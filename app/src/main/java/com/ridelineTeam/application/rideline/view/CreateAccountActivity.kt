package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
import com.ridelineTeam.application.rideline.util.helpers.InputsHelper
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_account.*


class CreateAccountActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toasty.error(applicationContext,connectionResult.errorMessage.orEmpty(),Toast.LENGTH_SHORT).show()
    }

    private lateinit var txtName: EditText
    private lateinit var txtNameLayout:TextInputLayout
    private lateinit var txtLastName: EditText
    private lateinit var txtLastNamesLayout:TextInputLayout
    private lateinit var txtEmail: EditText
    private lateinit var txtEmailLayout:TextInputLayout
    private lateinit var txtPassword: EditText
    private lateinit var txtPasswordLayout: TextInputLayout
    private lateinit var txtConfirmPassword: EditText
    private lateinit var txtConfirmPasswordLayout:TextInputLayout
    private lateinit var txtTelephone:EditText
    private lateinit var txtTelephoneLayout:TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var txtAddress: AutoCompleteTextView
    private lateinit var txtAddressLayout:TextInputLayout
    private lateinit var btnCreateAccount:Button
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var txtValidatePassword=""
    private lateinit var mAuth : FirebaseAuth
    private lateinit var txtTerms:TextView
    private lateinit var placeAutocompleteAdapter: PlaceAutocompleteAdapter
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var checkTerms:CheckBox
    private var latLongBounds = LatLngBounds(
            LatLng((-40).toDouble(), (-168).toDouble()),
            LatLng((71).toDouble(), (136).toDouble())
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        FragmentHelper.showToolbar(getString(R.string.createAccountActivity),true,findViewById(R.id.toolbar),this)
        initializeProperties()
    }
    override fun onStart() {
        super.onStart()
        btnCreateAccount.setOnClickListener{_ ->createUser()}
        clickableText(txtTerms,SpannableString(resources.getString(R.string.terms1)))
        InputsHelper.required(txtNameLayout,resources)
        InputsHelper.required(txtLastNamesLayout,resources)
        InputsHelper.email(txtEmailLayout,resources)
        InputsHelper.required(txtTelephoneLayout,resources)
        InputsHelper.required(txtAddressLayout,resources)
        errorPassword(txtPasswordLayout)
        confirmPassword(txtConfirmPasswordLayout)
        dbReference.keepSynced(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    private fun createUser() {
        progressBar.visibility = View.VISIBLE
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
            emailExist(user)
        }else{
            progressBar.visibility = View.GONE
            Toasty.warning(this,getString(R.string.completeUserForm),Toast.LENGTH_SHORT,true).show()
        }
    }
    private fun clickableText(textView: TextView, spannableString: SpannableString){
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View?) {
                showTerms()
            }
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds!!.color = ContextCompat.getColor(baseContext, R.color.colorPrimaryDark)
            }
        }
        spannableString.setSpan(clickableSpan,0,spannableString.length,0)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(spannableString,TextView.BufferType.SPANNABLE)
    }
    private fun confirmPassword(confirmPassword:TextInputLayout){
        confirmPassword.editText!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                txtValidatePassword= txtPassword.text.toString()
                if (editable.toString() != txtValidatePassword) {
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
            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                charSequence.toString()
            }
        })
    }
    private fun errorPassword(password: TextInputLayout) {
        password.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().length < 6) {
                    password.error = getString(R.string.passwordLengthError)
                } else {
                    password.isErrorEnabled = false
                    password.error = ""
                }
            }
            override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }
    private fun initializeProperties(){
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()
        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this,mGoogleApiClient,
                latLongBounds,null)
        txtName = findViewById(R.id.txtName)
        txtNameLayout = findViewById(R.id.txtNameLayout)
        txtLastName = findViewById(R.id.txtLastNames)
        txtLastNamesLayout = findViewById(R.id.txtLastNamesLayout)
        txtEmail = findViewById(R.id.txtEmail)
        txtEmailLayout =findViewById(R.id.txtEmailLayout)
        txtPassword = findViewById(R.id.txtPassword)
        txtPasswordLayout = findViewById(R.id.txtPasswordlayout)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)
        txtConfirmPasswordLayout=findViewById(R.id.txtConfirmPasswordlayout)
        txtTelephone=findViewById(R.id.txtTelephone)
        txtTelephoneLayout=findViewById(R.id.txtTelephoneLayout)
        txtAddress = findViewById(R.id.txtAddress)
        txtAddressLayout = findViewById(R.id.txtAddressLayout)
        txtAddress.setAdapter(placeAutocompleteAdapter)
        progressBar = findViewById(R.id.progressBar)
        btnCreateAccount = findViewById(R.id.btn_createAccount)
        txtTerms = findViewById(R.id.txtTerms)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child(USERS)
        checkTerms = findViewById(R.id.checkTerms)
    }
    private fun showTerms(){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.terms_conditions, null)
        dialogBuilder.setView(dialogView)
        val builder = dialogBuilder.create()
        builder.show()
    }
    private fun validateFields():Boolean{
        when {
            TextUtils.isEmpty(txtName.text) -> {
                txtNameLayout.error = getString(R.string.requiredFieldMessage)
                return false
            }
            TextUtils.isEmpty(txtLastName.text) -> {
                txtLastNamesLayout.error = getString(R.string.requiredFieldMessage)
                return false
            }
            TextUtils.isEmpty(txtEmail.text) -> {
                txtEmailLayout.error = getString(R.string.requiredFieldMessage)
                return false
            }
            TextUtils.isEmpty(txtPassword.text) -> {
                txtPasswordLayout.error = getString(R.string.requiredFieldMessage)
                return false
            }
            TextUtils.isEmpty(txtConfirmPassword.text) -> {
                txtConfirmPasswordLayout.error = getString(R.string.requiredFieldMessage)
                return false
            }
            TextUtils.isEmpty(txtTelephone.text) -> {
                txtTelephoneLayout.error = getString(R.string.requiredFieldMessage)
                return false
            }
            TextUtils.isEmpty(txtAddress.text)->{
                txtAddressLayout.error=getString(R.string.requiredFieldMessage)
                return false
            }
            !checkTerms.isChecked ->{
                Toasty.warning(this,getString(R.string.termsAndConditionsMessage),
                        Toast.LENGTH_LONG,true).show()
                return false
            }
            else ->{
                return true
            }
        }
    }
    private fun emailExist(user:User){
        mAuth.fetchSignInMethodsForEmail(user.email).addOnCompleteListener(this) { task ->
            if (task.isComplete){
                if(!task.result.signInMethods!!.isEmpty()){
                    txtEmailLayout.error=getString(R.string.emailError)
                    progressBar.visibility = View.GONE
                }
                else{
                    registerUserAccount(user)
                }
            }
        }
    }
    private fun registerUserAccount(user:User){
        mAuth.createUserWithEmailAndPassword(user.email, txtPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isComplete) {
                        val newUser: FirebaseUser? = mAuth.currentUser
                        newUser?.sendEmailVerification()
                        user.id = newUser?.uid!!
                        val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(user.name+" "+user.lastName)
                                .build()
                        newUser.updateProfile(profileUpdate)
                        dbReference.child(user.id).setValue(user)
                        progressBar.visibility = View.GONE
                        startActivity(Intent(this, LoginActivity::class.java))
                    }else {
                        progressBar.visibility = View.GONE
                        Toasty.error(this, task.exception!!.message!!,
                                Toast.LENGTH_SHORT,true).show()
                    }
                }
    }
}
