package com.ridelineTeam.application.rideline.view.fragment

import android.app.Activity
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
import android.widget.EditText
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

class ChatCommunityActivity : AppCompatActivity() {
    private lateinit var  community: Community
    private lateinit var txtMessage: EditText
    private lateinit var btn_send: FloatingActionButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var userId:String
    private lateinit var RecyclerChat: RecyclerView
    private lateinit var adpater: ChatCommunityAdapter.ChatCommunityAdapterRecycler
    private var listOfTokens = ArrayList<String>()
    private var usersIds = ArrayList<String>()
    private  var user: FirebaseUser? = null
    companion object {
        var activityInstance: Activity? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_community)
        community = intent.getSerializableExtra("community") as Community
        FragmentHelper.showToolbar(community.name,true,findViewById(R.id.toolbar),this)
        Log.d("DATA", "---------->$community")
        database= FirebaseDatabase.getInstance()
        databaseReference=database.reference.child(COMMUNITIES)
        RecyclerChat=findViewById(R.id.recycler_chat)
        user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid
        txtMessage=findViewById(R.id.txtMessage)
        btn_send=findViewById(R.id.send)
        btn_send.setOnClickListener {
            sendMessage()
        }
        Log.d("id", community.id)
        activityInstance = this
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
        if(!TextUtils.isEmpty(txtMessage.text.toString())) {
            val messages = Messages()
            messages.apply {
                userName = userId
                message = txtMessage.text.toString()
                time = getTimeMessage()
            }
            databaseReference.child(community.id).child("messages").push().setValue(messages).addOnCompleteListener {
                if (it.isSuccessful) {
                    txtMessage.text.clear()
                    getCommunityUsers(messages.message)
                    loadConversation()
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
        RecyclerChat.layoutManager = linearLayoutManager
        adpater= ChatCommunityAdapter.ChatCommunityAdapterRecycler(this,ref,this@ChatCommunityActivity,userId)
        RecyclerChat.adapter = adpater

    }
    private fun getCommunityUsers(message:String) {
        val ref: DatabaseReference = database.reference
        val query: Query = ref.child(COMMUNITIES).child(community.id).child(COMMUNITY_USERS)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                for (users in data.children) {
                    usersIds.add(users.value.toString())
                }
                getTokens(usersIds,message)
                Log.d("USERS", "LIST--->$usersIds")
            }


        })
    }
    private fun getTokens(list: ArrayList<String>,message:String) {

        val ref: DatabaseReference = database.reference
        val query: Query = ref.child(USERS)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(userToken: DataSnapshot) {
                for (items in list) {
                    listOfTokens.add(userToken.child(items).child(TOKEN).value.toString())
                }

                NotificationHelper.messageToCommunity(MainActivity.Companion.fmc, listOfTokens, "${community.name}"
                            , "${user!!.displayName}:$message")
                }
        })
}

}
