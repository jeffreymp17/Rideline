package com.ridelineTeam.application.rideline.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.CommunityDetailAdapter
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.view.GravityCompat
import android.R.id.toggle
import android.support.v4.app.FragmentManager
import android.support.v7.widget.Toolbar
import android.view.View
import com.ridelineTeam.application.rideline.MainActivity


class CommunityDetailActivity : AppCompatActivity() {

    private lateinit var  community:Community
    private lateinit var communityNameDetail:TextView
    private lateinit var communityCreated:TextView
    private lateinit var membersCount:TextView
    private lateinit var  database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recycler: RecyclerView
    private lateinit var adapter:CommunityDetailAdapter.CommunityDetailAdapterRecycler
    private lateinit var collapsibleLayout:CollapsingToolbarLayout
    private lateinit var appBarLayout:AppBarLayout
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_detail)
        database = FirebaseDatabase.getInstance()
        FragmentHelper.showToolbar("",true,findViewById(R.id.toolbar),this)
        collapsibleLayout=findViewById(R.id.collapsingToolbar)
        toolbar=findViewById(R.id.toolbar)
        appBarLayout=findViewById(R.id.appBar)
        databaseReference = database.reference.child(USERS)
        communityNameDetail = findViewById(R.id.communityNameDetail)
        communityCreated = findViewById(R.id.communityCreated)
        membersCount = findViewById(R.id.membersCount)
        community = intent.getSerializableExtra("community") as Community
        recycler = findViewById(R.id.usersRecycler)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        getCreator()
        drawDetail()
        collapsibleTittle(collapsibleLayout)
        FragmentHelper.backButtonToFragment(toolbar,CommunityDetailActivity@this)
    }

    override fun onStart() {
        super.onStart()
        communityNameDetail.text = community.name
        val text = community.users.size.toString() + " "+getString(R.string.members)
        membersCount.text = text
    }
    private fun getCreator(){
        val db = databaseReference.child(community.createdBy)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                val text=resources.getString(R.string.communityCreated)+
                        " "+user!!.name+", "+community.createdDate
                communityCreated.text=text
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
    private fun drawDetail(){
        val users = ArrayList<User>()
        community.users.forEach { key:String ->
            val dbSource = TaskCompletionSource<DataSnapshot>()
            val dbTask = dbSource.task
            val db = databaseReference.child(key)
            Log.d("ARRAYS", key)
            db.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dbSource.setResult(dataSnapshot)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            dbTask.addOnSuccessListener {
                val dataSnapshot = dbTask.result
                val user = dataSnapshot.getValue(User::class.java)
                users.add(user!!)
                adapter = CommunityDetailAdapter
                        .CommunityDetailAdapterRecycler(this,this,users,
                                community.users,community.createdBy)
                recycler.adapter = adapter
            }
        }
        Log.d("ARRAYS", users.toString())
    }
    private fun collapsibleTittle(collapsingToolbarLayout:CollapsingToolbarLayout){
        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            internal var isShow = true
            internal var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.title = getString(R.string.members)
                    isShow = true
                } else if (isShow) {
                    collapsingToolbarLayout.title = " "
                    isShow = false
                }
            }
        })
    }



}
