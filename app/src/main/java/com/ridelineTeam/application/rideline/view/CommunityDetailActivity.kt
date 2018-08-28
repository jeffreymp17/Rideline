package com.ridelineTeam.application.rideline.view

import android.content.Intent
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
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.helpers.PermissionHelper
import com.ridelineTeam.application.rideline.view.fragment.CommunityFragment
import es.dmoral.toasty.Toasty
import java.util.*


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
    private lateinit var  btnLeaveCommunity:Button
    private lateinit var materialDialog: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_detail)
        FragmentHelper.showToolbar("",true,findViewById(R.id.toolbar),this)
        database = FirebaseDatabase.getInstance()
        collapsibleLayout=findViewById(R.id.collapsingToolbar)
        toolbar=findViewById(R.id.toolbar)
        appBarLayout=findViewById(R.id.appBar)
        databaseReference = database.reference.child(USERS)
        communityNameDetail = findViewById(R.id.communityNameDetail)
        btnLeaveCommunity = findViewById(R.id.btnLeaveCommunity)
        communityCreated = findViewById(R.id.communityCreated)
        membersCount = findViewById(R.id.membersCount)
        community = intent.getSerializableExtra("community") as Community
        recycler = findViewById(R.id.usersRecycler)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        FragmentHelper.backButtonToFragment(toolbar,CommunityDetailActivity@this)
        materialDialog = MaterialDialog.Builder(this)
                .title(getString(R.string.loading))
                .content(R.string.please_wait)
                .progress(true, 0).build()
    }

    override fun onStart() {
        super.onStart()
        communityNameDetail.text = community.name
        val text = community.users.size.toString() + " "+getString(R.string.members)
        membersCount.text = text
        getCreator()
        drawDetail()
        collapsibleTittle(collapsibleLayout)
        btnLeaveCommunity.setOnClickListener({_->
            verifyIsHaveAnActiveRide()
        })
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
                                community.users,community.admin)
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
                    collapsingToolbarLayout.title = community.name
                    isShow = true
                } else if (isShow) {
                    collapsingToolbarLayout.title = " "
                    isShow = false
                }
            }
        })
    }
    private fun leaveCommunity(){
        val builder = AlertDialog.Builder(this@CommunityDetailActivity)
        // Set the alert dialog title
        builder.setTitle(getString(R.string.leaveGroup))
        if (community.users.size==1){
            builder.setMessage(getString(R.string.leave_delete_community_message))
        }
        else{
            builder.setMessage(R.string.leave_community_message)
        }
        // Display a message on alert dialog
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            showProgressBar()
            if (community.users.size==1){
                quitCommunityForUser(true)
            }
            else{
                quitUserForCommunity()
            }
        }
        // Display a negative button on alert dialog
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
        }
        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()
        // Display the alert dialog on app interface
        dialog.show()
    }

    private fun quitUserForCommunity(){
        val db = database.reference.child(COMMUNITIES).child(community.id)
        db.runTransaction(object:Transaction.Handler {
            override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, p2: DataSnapshot?) {
                if (databaseError!=null){
                    hideProgressBar()
                    Toasty.error(applicationContext,getString(R.string.leave_community_error)
                            ,Toast.LENGTH_LONG).show()
                }

                if (boolean)
                    quitCommunityForUser(false)

            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val community = mutableData.getValue(Community::class.java)
                val members = ArrayList<String>()
                val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUser==null){
                    Transaction.abort()
                }
                for (userId in community!!.users){
                    if (currentUser != userId){
                        members.add(userId)
                    }
                }
                if (currentUser==community.admin){
                    val randomPos = Random().nextInt(members.size)
                    community.admin = members[randomPos]
                }
                community.users=members
                mutableData.value=community
                return Transaction.success(mutableData)
            }
        })
    }

    private fun quitCommunityForUser(deleteCommunity: Boolean){
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val db = database.reference.child(USERS).child(currentUser)
        db.runTransaction(object:Transaction.Handler {
            override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, p2: DataSnapshot?) {
                if (databaseError!=null){
                    hideProgressBar()
                    Toasty.error(applicationContext,getString(R.string.leave_community_error)
                            ,Toast.LENGTH_LONG).show()
                }
                if (boolean){
                    if (deleteCommunity){
                        database.reference.child(COMMUNITIES).child(community.id).removeValue()
                    }
                    hideProgressBar()
                    startActivity(Intent(this@CommunityDetailActivity, MainActivity::class.java)
                            .putExtra("fragment", CommunityFragment::class.java.name))
                    //FragmentHelper.changeFragment(CommunityFragment(),supportFragmentManager)
                    Toasty.success(applicationContext,getString(R.string.leave_community_success)
                            ,Toast.LENGTH_SHORT).show()
                    ChatCommunityActivity.activityInstance!!.finish()
                    finish()
                }

            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val user = mutableData.getValue(User::class.java)
                val communities = ArrayList<String>()
                for (communityId in user!!.communities){
                    if (communityId != community.id){
                        communities.add(communityId)
                    }
                }
                user.communities=communities
                mutableData.value=user
                return Transaction.success(mutableData)
            }
        })
    }
    private fun verifyIsHaveAnActiveRide(){
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val db = database.reference.child(USERS).child(currentUser)
        db.runTransaction(object:Transaction.Handler {
            override fun onComplete(databaseError: DatabaseError?, boolean: Boolean, p2: DataSnapshot?) {
                if (databaseError!=null){
                    Toasty.error(applicationContext,getString(R.string.leave_community_error)
                            ,Toast.LENGTH_LONG).show()
                }
                Log.d("BOLEAN",boolean.toString())
                if (boolean){
                    leaveCommunity()
                }
                else{
                    Toasty.info(applicationContext,"Tienes un ride activo en esta comunidad, " +
                            "debes esperar que finalize o cancelarlo si quieres salir.",Toast.LENGTH_LONG).show()
                }

            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val user = mutableData.getValue(User::class.java)
                return if (user!!.activeRide !=null && user.activeRide!!.community==community.id)
                    Transaction.abort()
                else
                    Transaction.success(mutableData)
            }
        })
    }
    private fun showProgressBar(){
        materialDialog.show()
        PermissionHelper.disableScreenInteraction(window)
    }
    private fun hideProgressBar(){
        materialDialog.dismiss()
        PermissionHelper.enableScreenInteraction(window)
    }

}
