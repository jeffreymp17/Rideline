package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {

    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtForgotPassword : TextView
    private lateinit var txtSignUp : TextView
    private lateinit var btnLogin: Button
    private lateinit var progressBar:ProgressBar
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword= findViewById(R.id.txtPassword)
        txtForgotPassword =findViewById(R.id.forgot_password)
        txtSignUp = findViewById(R.id.link_sign_up)
        btnLogin = findViewById(R.id.btn_login)
        progressBar =findViewById(R.id.progressBar)

        btnLogin.setOnClickListener{ _ ->
            validateInformation(txtEmail.text.toString(),txtPassword.text.toString())
        }

        txtSignUp.setOnClickListener{_->
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
        txtForgotPassword.setOnClickListener{_->
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        if(mAuth.currentUser!= null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!= null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun validateInformation(email:String, password:String){
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.visibility = View.VISIBLE
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressBar.visibility = View.GONE
                    if(mAuth.currentUser!!.isEmailVerified){
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    else{
                        Toasty.warning(this,getString(R.string.verifiedText), Toast.LENGTH_LONG, true).show()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    task.addOnFailureListener(this) {
                        Toasty.error(this, "${it.message}", Toast.LENGTH_SHORT,true).show()
                    }
                }
            }
        } else {
            progressBar.visibility = View.GONE
            Toasty.warning(this,getString(R.string.emptyUserPassToast), Toast.LENGTH_SHORT, true).show()
        }
    }
}
