package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatButton
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var txtEmail: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var btnResetPassword: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        isUserInSession()
        FragmentHelper.showToolbar(getString(R.string.btn_resetPassword),true,findViewById(R.id.toolbar),this)
        txtEmail=findViewById(R.id.txtEmail)
        progressBar=findViewById(R.id.progressBar)
        btnResetPassword = findViewById(R.id.btn_resetPassword)
        auth= FirebaseAuth.getInstance()
        btnResetPassword.setOnClickListener { _ -> sendEmail()}

    }
    private fun sendEmail(){
        val email:String=txtEmail.text.toString()
        if(!TextUtils.isEmpty(email)){
            auth.sendPasswordResetEmail(email).addOnCompleteListener(this){
                task->
                if(task.isSuccessful){
                    progressBar.visibility= View.VISIBLE
                    startActivity(Intent(this,LoginActivity::class.java))
                }else{
                    task.addOnFailureListener(this){
                        Toast.makeText(this,"Error to send email ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
            Toast.makeText(this,"please enter your email", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isUserInSession() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
