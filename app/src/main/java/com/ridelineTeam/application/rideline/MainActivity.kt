package com.ridelineTeam.application.rideline

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.files.ACCESS_FINE_LOCATION
import com.ridelineTeam.application.rideline.util.files.FIREBASE_SERVER_DEV
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.view.AboutActivity
import com.ridelineTeam.application.rideline.view.ChatCommunityActivity
import com.ridelineTeam.application.rideline.view.LoginActivity
import com.ridelineTeam.application.rideline.view.fragment.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import org.pixsee.fcm.Sender

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var toolbar: Toolbar
    private lateinit var database: FirebaseDatabase
    private lateinit var user: FirebaseUser
    private lateinit var id: String
    private var userObject: User? = null
    private lateinit var userName: TextView
    private lateinit var email: TextView
    private lateinit var image: CircleImageView
    private lateinit var navigationView: NavigationView
    //private lateinit var searchView:MaterialSearchView

    companion object {
        val refreshedToken = FirebaseInstanceId.getInstance().token!!
        val fmc: Sender = Sender(FIREBASE_SERVER_DEV)
        val userId=FirebaseAuth.getInstance().currentUser!!.uid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (intent.extras!= null){
            for (key in intent.extras.keySet()){
                if (key == "communityChat"){
                    startActivity(Intent(this, ChatCommunityActivity::class.java))
                }

            }
        }
        var displayingFragment:Fragment = HomeFragment()
        if(intent.getStringExtra("fragment")!=null){
            val fragmentName =intent.getStringExtra("fragment")
            displayingFragment = getDisplayingFragment(fragmentName)
        }
        init(displayingFragment)

        /*try{
            if (AppStatus.getInstance(applicationContext).isOnline) {}
         }catch (e:ExceptionInInitializerError){
            val snackbar = Snackbar.make(window.decorView,
                            getString(R.string.errorMessage),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.retry), {
              init()
            })
          snackbar.show()
          }*/

    }

    private fun init(displayingFragment:Fragment) {
        toolbar = findViewById(R.id.toolbar)
        user = FirebaseAuth.getInstance().currentUser!!

        id = user.uid
        setSupportActionBar(toolbar)
        navigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        userName = navigationView.getHeaderView(0).findViewById(R.id.nav_complete_name)
        email = navigationView.getHeaderView(0).findViewById(R.id.email_nav)
        image = navigationView.getHeaderView(0).findViewById(R.id.nav_image)
        FragmentHelper.changeFragment(displayingFragment,supportFragmentManager)
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.containerMain)
            if (fragment != null) {
                updateTittle(fragment)
            }
        }

        Log.d("TOKEN", "MESSAGE:$refreshedToken")
        sendRegistrationToServer(refreshedToken, id)
        getUserProfile()
    }


    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount == 1 -> finish()
            else -> super.onBackPressed()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                FragmentHelper.changeFragment(HomeFragment(),supportFragmentManager)
                //titleNav = getString(R.string.app_name)
            }
            R.id.nav_profile -> {
                FragmentHelper.changeFragment(ProfileFragment(),supportFragmentManager)
                //titleNav = getString(R.string.profile)
            }
            R.id.nav_ride -> {
                cantCreateRideWhenActive()
            }
            R.id.nav_community -> {
                if(userObject!!.communities.isEmpty()){
                    FragmentHelper.changeFragment(CommunitiesFragment(),supportFragmentManager)
                }else{
                    FragmentHelper.changeFragment(CommunityFragment(),supportFragmentManager)
                }

            }
            R.id.nav_about_us -> {
                startActivity(Intent(MainActivity@ this, AboutActivity::class.java))
            }
            R.id.nav_logout -> {
                logOut()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Maps features are available", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("RIDE", "PERMISO DENEGADO")
                    Toast.makeText(this, "Some features won't be available", Toast.LENGTH_LONG).show()

                }
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
            }
            Activity.RESULT_CANCELED -> {
            }
        }
    }


    private fun sendRegistrationToServer(token: String, user: String) {
        database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.reference
        ref.child(USERS).child(user).child("token").setValue(token)
        Log.d("ENTER", "HERE")

    }

    //muestra la informacion del usuario en la barra lateral
    private fun getUserProfile() {
        val reference: DatabaseReference = database.getReference(USERS)
        reference.child(id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                userObject = p0.getValue(User::class.java)

                userObject.apply {
                    val fullName = userObject!!.name + " " + userObject!!.lastName
                    userName.text = fullName
                    email.text = userObject!!.email
                    if (userObject!!.pictureUrl.isEmpty()) {
                        Picasso.with(applicationContext).load(R.drawable.if_profle_1055000).fit().into(image)

                    } else {
                        Picasso.with(applicationContext).load(userObject!!.pictureUrl).fit().into(image)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun cantCreateRideWhenActive() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val reference: DatabaseReference = database.getReference(USERS)
        reference.child(currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(dataError: DatabaseError) {
                Toasty.error(this@MainActivity, dataError.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user!!.activeRide == null) {
                    FragmentHelper.changeFragment(RideFragment(),supportFragmentManager)
                } else {
                    Toasty.info(this@MainActivity, getString(R.string.rideActiveMessage), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun updateTittle(fragment: Fragment) {
        val fragmentName = fragment.javaClass.name
        when (fragmentName) {
            ProfileFragment::class.java.name -> title = getString(R.string.profile)
            RideFragment::class.java.name -> title = getString(R.string.ride)
            CommunityFragment::class.java.name -> title = getString(R.string.community)
            HomeFragment::class.java.name -> title = getString(R.string.app_name)
        }
    }
    private fun getDisplayingFragment(fragmentName: String):Fragment {
        return when (fragmentName) {
            ProfileFragment::class.java.name -> ProfileFragment()
            RideFragment::class.java.name -> RideFragment()
            CommunityFragment::class.java.name -> CommunityFragment()
            else -> HomeFragment()
        }
    }


    private fun logOut() {
        val builder = AlertDialog.Builder(this@MainActivity)
        // Set the alert dialog title
        builder.setTitle(getString(R.string.title_close_your_session))
        // Display a message on alert dialog
        builder.setMessage(getString(R.string.message_when_close_your_session))
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            FirebaseAuth.getInstance().signOut()
            finish()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
        // Display a negative button on alert dialog
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
        }
        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()
        // Display the alert dialog on app interface
        dialog.show()
    }


}

