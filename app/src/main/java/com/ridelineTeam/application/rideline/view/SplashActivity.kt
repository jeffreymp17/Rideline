package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ridelineTeam.application.rideline.R
import android.content.Intent
import android.os.Handler


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val i = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
        }, 3000)
    }
}
