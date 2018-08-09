package com.ridelineTeam.application.rideline.view.fragment

import android.app.Activity
import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.format.Time
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.ChatCommunityAdapter
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.Messages
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.files.COMMUNITY_USERS
import com.ridelineTeam.application.rideline.util.files.TOKEN
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper
import com.ridelineTeam.application.rideline.view.CommunityDetailActivity
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*

class ChatCommunityActivity : AppCompatActivity() {
    private lateinit var community: Community
    private lateinit var txtMessage: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var userId:String
    private lateinit var recyclerChat: RecyclerView
    private lateinit var adapter: ChatCommunityAdapter.ChatCommunityAdapterRecycler
    private lateinit var titleTextView:TextView
    private lateinit var subtitleTextView:TextView

    private  var user: FirebaseUser? = null
    //private val token= MyFirebaseInstanceIDService().onTokenRefresh()
    private lateinit var toolbar: android.support.v7.widget.Toolbar
    companion object {
        @SuppressLint("StaticFieldLeak")
        var activityInstance: Activity? = null
    }
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_community)
        community = intent.getSerializableExtra("community") as Community
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(COMMUNITIES)
        recyclerChat = findViewById(R.id.recycler_chat)
        titleTextView = findViewById(R.id.chat_toolbar_title)
        subtitleTextView = findViewById(R.id.chat_toolbar_subtitle)

        user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid
        txtMessage=findViewById(R.id.txtMessage)

        btnSend=findViewById(R.id.send)
        btnSend.setOnClickListener {
            sendMessage()
        }

        toolbar=findViewById(R.id.chat_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        super.setTitle("")
        titleTextView.text=community.name
        subtitleTextView.text = getString(R.string.chat_subtitle)
        val tittleLayout = findViewById<LinearLayout>(R.id.titleLayout)
        tittleLayout.setOnClickListener({
            val intent = Intent(this@ChatCommunityActivity,CommunityDetailActivity::class.java)
            intent.putExtra("community", community)
            startActivity(intent)
        })
        FragmentHelper.backButtonToFragment(toolbar,ChatCommunityActivity@this)
        activityInstance = this
        subtitleAnimation()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.community_group -> {
                 val intent = Intent(this@ChatCommunityActivity,CommunityDetailActivity::class.java)
                 intent.putExtra("community", community)
                 startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun sendMessage(){
        if(!TextUtils.isEmpty(txtMessage.text.trim().toString())) {
            btnSend.isEnabled=false
            val messages = Messages()
            messages.apply {
                userName = userId
                message = txtMessage.text.toString()
                time = getTimeMessage()
            }
            databaseReference.child(community.id).child("messages").push().setValue(messages).addOnCompleteListener {
                if (it.isSuccessful) {
                    txtMessage.setText("")
                    getCommunityUsers(messages.message)
                    loadConversation()
                    btnSend.isEnabled=true
                }
            }
        }else{
            Toasty.warning(applicationContext,getString(R.string.write_message),Toast.LENGTH_SHORT,true).show()
        }
    }
    private fun  getTimeMessage():String{
        val time = Time()
        time.setToNow()
        if(time.minute<10){
            return time.hour.toString() + ":" + "${0}${time.minute} "

        }
        return time.hour.toString() + ":" + time.minute
    }


    override fun onStart() {
        super.onStart()
        loadConversation()
    }
    private fun loadConversation(){
        val ref:DatabaseReference=FirebaseDatabase.getInstance().reference.child(COMMUNITIES)
                .child(community.id).child("messages")
        ref.addValueEventListener(object:ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {
            Log.d("CONVERSATIONS","ADD"+p0.value)
        }

    })
        val linearLayoutManager = LinearLayoutManager(this@ChatCommunityActivity.applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        linearLayoutManager.stackFromEnd=true
        recyclerChat.layoutManager = linearLayoutManager
        adapter= ChatCommunityAdapter.ChatCommunityAdapterRecycler(this,ref,this@ChatCommunityActivity,userId)
        recyclerChat.adapter = adapter

    }
    private fun getCommunityUsers(message:String) {
        val usersIds = ArrayList<String>()
        val ref: DatabaseReference = database.reference
        val query: Query = ref.child(COMMUNITIES).child(community.id).child(COMMUNITY_USERS)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                for (users in data.children) {
                    usersIds.add(users.value.toString())
                }
               val users= usersIds.filterNot{ it == userId }
                getTokens(users as ArrayList<String>,message)
            }


        })
    }
    private fun getTokens(list: ArrayList<String>,message:String) {
        val listOfTokens = ArrayList<String>()
        val ref: DatabaseReference = database.reference
        val query: Query = ref.child(USERS)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(userToken: DataSnapshot) {
                for (items in list) {
                        listOfTokens.add(userToken.child(items).child(TOKEN).value.toString())
                }
                Log.d("Tokens in data change","$listOfTokens")
                NotificationHelper.messageToCommunity(MainActivity.fmc, listOfTokens, community.name
                        , "${user!!.displayName} $message")
                }

        })
    }
    private fun subtitleAnimation(){
        val anim = AlphaAnimation(1f, 0.1f)
        anim.duration = 3000
        subtitleTextView.startAnimation(anim)
        anim.fillAfter=true
        android.os.Handler().postDelayed({
            subtitleTextView.visibility = View.GONE
        },3000)
    }
}
