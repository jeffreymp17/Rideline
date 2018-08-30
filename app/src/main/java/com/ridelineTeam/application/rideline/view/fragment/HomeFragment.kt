package com.ridelineTeam.application.rideline.view.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.RideAdapter
import com.ridelineTeam.application.rideline.util.files.RIDES
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.enums.Status
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener




class HomeFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private  var adapter: RideAdapter.RideAdapterRecycler?=null
    private lateinit var user: FirebaseUser
    private lateinit var arrayCommunity:ArrayList<String>
    private lateinit var recycler: MultiSnapRecyclerView
    private lateinit var noRidesText:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(RIDES)
        user = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_home, container, false)
        recycler = rootView.findViewById(R.id.mainRecycler)
        noRidesText = rootView.findViewById(R.id.noRidesText)
        return  rootView
    }

    override fun onStart() {
        super.onStart()
        getOnlyMyRides()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title=getString(R.string.app_name)
    }
    private fun getOnlyMyRides() {
      arrayCommunity = ArrayList()
        val query = database.reference.child(USERS).child(user.uid)
        query.child("communities").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (communities in p0.children) {
                    arrayCommunity.add(communities.value.toString())
                    Log.d("COMMUNITIES", "USER:$arrayCommunity")
                }
                val query1 = databaseReference.orderByChild("status").equalTo(Status.ACTIVE.toString())
                val linearLayoutManager = LinearLayoutManager(context!!.applicationContext)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                recycler.layoutManager = linearLayoutManager
                adapter = RideAdapter.RideAdapterRecycler(context!!.applicationContext,
                        databaseReference, activity, query1, arrayCommunity, user.uid,noRidesText)
                recycler.adapter = adapter
            }

        })
    }
}// Required empty public constructor
