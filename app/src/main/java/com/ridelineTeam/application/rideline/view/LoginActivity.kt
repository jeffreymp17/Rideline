package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import es.dmoral.toasty.Toasty






class LoginActivity : AppCompatActivity() {

    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtForgotPassword: TextView
    private lateinit var txtSignUp: TextView
    private lateinit var btnLogin: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        txtForgotPassword = findViewById(R.id.forgot_password)
        txtSignUp = findViewById(R.id.link_sign_up)
        btnLogin = findViewById(R.id.btn_login)
        mAuth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progressBar)

        isUserInSession()
    }

    override fun onStart() {
        super.onStart()
        clickableText(txtSignUp,SpannableString(resources.getString(R.string.link_sign_up)),
                Intent(this, CreateAccountActivity::class.java))
        clickableText(txtForgotPassword,SpannableString(resources.getString(R.string.forgot_password)),
                Intent(this, ResetPasswordActivity::class.java))
        btnLogin.setOnClickListener { _ ->
            validateUserInformation(txtEmail.text.toString(), txtPassword.text.toString())
        }
    }

    private fun validateUserInformation(email: String, password: String) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.visibility = View.VISIBLE
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            if (isEmailVerified()) {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            task.addOnFailureListener(this) {
                                Toasty.error(this, "${it.message}", Toast.LENGTH_SHORT,
                                        true).show()
                            }
                        }
                        progressBar.visibility = View.GONE
                    }
        } else {
            progressBar.visibility = View.GONE
            Toasty.warning(this, getString(R.string.emptyUserPassToast), Toast.LENGTH_SHORT,
                    true).show()
        }
    }

    private fun isUserInSession() {
        val currentUser = mAuth.currentUser
        if (currentUser != null && isEmailVerified()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun isEmailVerified(): Boolean {
        val currentUser = mAuth.currentUser
        return if (currentUser!!.isEmailVerified) {
            true
        } else {
            Toasty.warning(this, getString(R.string.verifiedText),
                    Toast.LENGTH_SHORT, true).show()
            false
        }
    }

    private fun clickableText(textView: TextView,spannableString: SpannableString,intent: Intent){
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View?) {
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds!!.color = ContextCompat.getColor(baseContext, R.color.icons)
            }
        }
        spannableString.setSpan(clickableSpan,0,spannableString.length,0)
        textView.movementMethod =LinkMovementMethod.getInstance()
        textView.setText(spannableString,TextView.BufferType.SPANNABLE)
    }
}
