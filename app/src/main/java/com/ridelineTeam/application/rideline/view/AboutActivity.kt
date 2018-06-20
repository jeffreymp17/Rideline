package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper

class AboutActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        toolbar=findViewById(R.id.toolbar)
        FragmentHelper.showToolbar(getString(R.string.AboutApplication)
                ,true,toolbar,this)

    }
}
