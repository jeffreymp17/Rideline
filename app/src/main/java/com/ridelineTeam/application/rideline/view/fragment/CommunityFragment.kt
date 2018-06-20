package com.ridelineTeam.application.rideline.view.fragment


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.CommunityAdapter.CommunityAdapterRecycler
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper


/**
 * A simple [Fragment] subclass.
 */
class CommunityFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CommunityAdapterRecycler
    private lateinit var fab:FloatingActionButton
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView =inflater.inflate(R.layout.fragment_community, container, false)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(COMMUNITIES)
        recycler = rootView.findViewById(R.id.communityRecycler)
        fab = rootView.findViewById(R.id.fabCreateCommunity)
        fab.setOnClickListener({
            FragmentHelper.changeFragment(CreateCommunityFragment(), this.fragmentManager!!)
        })
        return rootView
    }
    private fun loadCommunities(){
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        adapter = CommunityAdapterRecycler(context, databaseReference, activity)
        recycler.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        loadCommunities()
    }
    override fun onStop() {
        super.onStop()
        this.adapter.cleanupListener()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_search,menu)
        val item = menu!!.findItem(R.id.action_search).actionView as SearchView
        searchResult(item)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun searchResult(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("TEXT", query)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("TEXT", newText)
                if (!newText.isEmpty()){
                    val query:Query= databaseReference.orderByChild("name")
                            .startAt(newText).endAt("$newText\uf8ff")
                    setAdapter(query)
                }
                else{
                    loadCommunities()
                }

                return true
            }
        })

    }
    fun setAdapter(query: Query){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "loadProducts:onCancelled", databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(activity,"ERROR DATA",
                        Toast.LENGTH_SHORT).show()
            }
        }
        this@CommunityFragment.databaseReference.addValueEventListener(valueEventListener)
        this@CommunityFragment.valueEventListener=valueEventListener
        val linearLayoutManager = LinearLayoutManager(context!!.applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        this@CommunityFragment.recycler.layoutManager = linearLayoutManager
        this@CommunityFragment.adapter = CommunityAdapterRecycler(
                context!!.applicationContext,
                this@CommunityFragment.databaseReference,
                activity,
                query
        )
        this@CommunityFragment.recycler.adapter = adapter

    }
    }// Required empty public constructor
