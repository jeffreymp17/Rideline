package com.ridelineTeam.application.rideline.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ridelineTeam.application.rideline.MainActivity.Companion.userId
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.RideAdapter
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.util.enums.Status
import com.ridelineTeam.application.rideline.util.files.RIDES
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper

class CommunityRidesActivity : AppCompatActivity() {
private lateinit var community:Community
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private  var adapter: RideAdapter.RideAdapterRecycler?=null
    private lateinit var recycler: RecyclerView
    private lateinit var noRidesText: TextView
    private lateinit var toolbar: android.support.v7.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_rides)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(RIDES)
        recycler=findViewById(R.id.communityRidesRecycler)
        noRidesText = findViewById(R.id.noRidesText)
        community = intent.getSerializableExtra("community") as Community
        Log.d("COMMUNITY","IS:$community")
        FragmentHelper.showToolbar(community.name,true,
                findViewById(R.id.toolbar),this)
        loadCommunityRide()
        FragmentHelper.backButtonToFragment(toolbar, ChatCommunityActivity@ this)


    }
    private fun loadCommunityRide(){
        val query1 = databaseReference.orderByChild("status").equalTo(Status.ACTIVE.toString())
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        adapter = RideAdapter.RideAdapterRecycler(applicationContext,databaseReference,
                this@CommunityRidesActivity,query1, userId,noRidesText,community.id)
        recycler.adapter=adapter
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_community_rides, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.community_group -> {
                val intent = Intent(this@CommunityRidesActivity, CommunityDetailActivity::class.java)
                intent.putExtra("community", community)
                startActivity(intent)
                true
            }
            R.id.community_chat->{
                val intent = Intent(this@CommunityRidesActivity, ChatCommunityActivity::class.java)
                intent.putExtra("community", community)
                startActivity(intent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
