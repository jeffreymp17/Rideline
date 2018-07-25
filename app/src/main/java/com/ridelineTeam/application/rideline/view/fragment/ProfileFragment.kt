package com.ridelineTeam.application.rideline.view.fragment


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.ProfileAdapter
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.model.enums.Status
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.files.*
import com.ridelineTeam.application.rideline.util.helpers.ImageHelper
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper
import com.squareup.picasso.Picasso
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var profile_picture: CircleImageView
    private lateinit var name: TextView
    private lateinit var place: TextView
    private lateinit var email: TextView
    private lateinit var imageCircle: CircleImageView
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var id: String
    private lateinit var picture_bytes: ByteArray
    private lateinit var photoRef: String
    private lateinit var fireStorage: FirebaseStorage
    private lateinit var sReference: StorageReference
    private lateinit var cardView: CardView
    private lateinit var btn_edit: FloatingActionButton
    private lateinit var recycler: MultiSnapRecyclerView
    private lateinit var adapter: ProfileAdapter.ProfileAdapterRecycler
    private var user: User? = null
    private lateinit var uri: Uri


    //Elementos de la carta del ride activo
    private lateinit var  dateCard : TextView
    private lateinit var userCard: TextView
    private lateinit var typeCard: TextView
    private lateinit var origin: TextView
    private lateinit var destination: TextView
    private lateinit var passengerCard: TextView
    private lateinit var rideImage: ImageView
    private lateinit var btnCancelCard: Button
    private lateinit var hour: TextView
    private lateinit var card: CardView
    private lateinit var noActiveRideText:TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_profile, container, false)
        database = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        btn_edit = rootView.findViewById(R.id.edit_profile)
        cardView = rootView.findViewById(R.id.cardViewRide)
        id = user!!.uid
        fireStorage = FirebaseStorage.getInstance()
        sReference = fireStorage.reference.child(PROFILE_PICTURES)
        imageCircle = rootView.findViewById(R.id.my_picture_profile)
        recycler = rootView.findViewById(R.id.recycler_profile)
        reference = database.reference.child(USERS)
        name = rootView.findViewById(R.id.profile_name)
        //community_name = rootView.findViewById(R.id.user_communities)
        place = rootView.findViewById(R.id.profile_place)
        email = rootView.findViewById(R.id.user_email)
        //done = rootView.findViewById(R.id.count)
        profile_picture = rootView.findViewById(R.id.my_picture_profile)
        //btn_cancel_ride=rootView.findViewById(R.id.btnCancelCard)
        btn_edit.setOnClickListener {
            showChangeLangDialog(container)
        }
        imageCircle.setOnClickListener {
            showGallery()
        }
        //Carta de ride activo
        card = rootView.findViewById(R.id.cardViewRide)
        dateCard = rootView.findViewById(R.id.dateCard)
        userCard = rootView.findViewById(R.id.userCard)
        typeCard = rootView.findViewById(R.id.typeCard)
        passengerCard = rootView.findViewById(R.id.passengerCard)
        rideImage = rootView.findViewById(R.id.typeRideImage)
        btnCancelCard = rootView.findViewById(R.id.btnCancelCard)
        destination = rootView.findViewById(R.id.txtDestination)
        origin = rootView.findViewById(R.id.txtOrigin)
        hour = rootView.findViewById(R.id.rideHour)
        noActiveRideText = rootView.findViewById(R.id.noActiveRideText)

        //community_name.movementMethod = ScrollingMovementMethod()
        getUserProfile()
        return rootView
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title=getString(R.string.profile)
    }
    override fun onStart() {
        super.onStart()
        test()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK) {
            uri = data!!.data
            photoRef = uri.lastPathSegment
            picture_bytes = ImageHelper.resizeBytesImage(context, imageCircle, data)
            uploadProfileImage(data)
        }
    }
    private fun showGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_INTENT)
    }

    //GET DATA FROM CURRENT USER
    private fun getUserProfile() {
        id = FirebaseAuth.getInstance().currentUser!!.uid
        reference.child(id).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                user = data.getValue(User::class.java)
                if(user!!.taked==1){
                    btnCancelCard.visibility=View.VISIBLE
                    btnCancelCard.setOnClickListener{
                        reference.child(id).child("taked").setValue(0).addOnCompleteListener {
                            btnCancelCard.visibility=View.GONE

                        }
                    }
                }
                user.apply {
                    val fullName =user!!.name + " " + user!!.lastName
                    name.text =  fullName
                    place.text = user!!.address
                    email.text = user!!.email
                    if (user!!.pictureUrl.isEmpty()) {
                        Picasso.with(context).load(R.drawable.if_profle_1055000).fit().into(imageCircle)

                    } else {
                        Picasso.with(context).load(user!!.pictureUrl).fit().into(imageCircle)
                    }
                    if(user!!.activeRide!=null){
                        card.visibility = View.VISIBLE
                        noActiveRideText.visibility=View.GONE
                        if(isAdded){
                            drawActiveRideCard(user!!.activeRide)
                        }
                    }
                    else{
                        card.visibility = View.GONE
                        noActiveRideText.visibility=View.VISIBLE
                    }
                }
                for (i in user!!.communities) {
                    userCommunities(i)
                }
                Log.d("COMMUNITIES", "----:" + data.child("communities").value.toString())


            }

        })
    }

    //RIDES REQUESTS AND OFFERED FROM CURRENT USER
    private fun test() {
        val ref = database.reference.child(RIDES)
        val query = ref.orderByChild("user").equalTo(id)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recycler.layoutManager = linearLayoutManager
        adapter = ProfileAdapter.ProfileAdapterRecycler(context, ref, activity, query)
        recycler.adapter = adapter
        counterWhenIsDone()
    }

    //COUNT RIDES TOTAL AND IS DONE
    private fun counterWhenIsDone() {
        var isDone = 0
        var finished = 0
        val ref = database.reference.child(RIDES)
        val query = ref.orderByChild("user").equalTo(id)
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                p0.toException()
            }

            override fun onDataChange(data: DataSnapshot) {
                Log.d("DATA", "-----------$data")
                for (done in data.children) {

                    if (done.child("status").value!! == Status.FINISHED.toString()) {
                        finished++
                    }
                    isDone++
                    Log.d("DATA", "-----------${done.child("status")}")


                }
                //done.text = "Total:$isDone/$finished"

            }
        })
    }

    //EDIT AND CHNAGE DATA IN FIREBASE
    private fun newProfile(newName: String, newLastNames: String,
                           newStatus:String) {
        reference.child(id).child("name").setValue(newName)
        reference.child(id).child("lastName").setValue(newLastNames)
        reference.child(id).child("status").setValue(newStatus)
        Toasty.success(context!!, "Updated successful", Toast.LENGTH_SHORT, true).show()
    }

    ///DIALOG TO CONFIRM CHANGE INFORMATION
    private fun showChangeLangDialog(viewGroup: ViewGroup?) {
        val dialogBuilder = AlertDialog.Builder(context!!)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.edit_profile,viewGroup,false)
        dialogBuilder.setView(dialogView)

        val txtName: EditText = dialogView.findViewById(R.id.txtProfile_name)
        val txtLastName: EditText = dialogView.findViewById(R.id.txtProfile_Last_names)
        val txtStatus: EditText = dialogView.findViewById(R.id.txtProfile_Status)
        with(user) {
            txtName.setText(user!!.name,TextView.BufferType.EDITABLE)
            txtLastName.setText(user!!.lastName,TextView.BufferType.EDITABLE)
            txtStatus.setText(user!!.status,TextView.BufferType.EDITABLE)
        }

        dialogBuilder.setTitle("Edit your information")
        dialogBuilder.setPositiveButton("Done") { _, _ ->
            if (!TextUtils.isEmpty(txtName.text) && !TextUtils.isEmpty(txtLastName.text)) {
                newProfile(txtName.text.toString(), txtLastName.text.toString()
                        ,txtStatus.text.toString())

            }

        }
        dialogBuilder.setNegativeButton("Cancel") { _, _ ->

        }

        val b = dialogBuilder.create()
        b.show()
    }

    //THIS METHOD UPLOAD PROFILE PICTORE IN FIREABSE SERVER
    private fun uploadProfileImage(data: Intent?) {
        reference = database.reference.child(USERS).child(id)
        sReference = fireStorage.reference.child(PROFILE_PICTURES).child(photoRef)
        sReference.putBytes(picture_bytes).addOnSuccessListener {
            if (it.task.isComplete) {
                sReference.downloadUrl.addOnSuccessListener({ uri ->
                    with(reference) {
                        child("pictureUrl").setValue(uri.toString())
                        imageCircle.setImageURI(data!!.data)

                    }

                })

            }

        }
    }

    ///GET NAME OF COMMUNITIES
    private fun userCommunities(key: String) {
        val query = database.reference.child(COMMUNITIES).child(key)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                p0.message
            }

            override fun onDataChange(community: DataSnapshot) {
                //community_name.text = community.child("name").value.toString() + "\n" + community_name.text.toString()
                Log.d("INFO", "-----------$community")

            }

        })

    }

    private  fun drawActiveRideCard(ride: Ride?){
        dateCard.text = ride!!.date
        database.reference.child(USERS).child(ride.user)
                .addListenerForSingleValueEvent(object :ValueEventListener
        {
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(activity!!.applicationContext,databaseError.message,Toast.LENGTH_SHORT)
                        .show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                val fullName =user!!.name+" "+user.lastName
                userCard.text=fullName
            }
        })
        if (Type.REQUESTED == ride.type){
            typeCard.text = resources.getString(R.string.radioTypeRequest)
            val passengerText = resources.getString(R.string.passengers)+" "+ride.riders.toString()
            passengerCard.text = passengerText
            rideImage.background = ResourcesCompat.getDrawable(resources,R.drawable.como,null)
        }else{
            typeCard.text = resources.getString(R.string.radioTypeOffer)
            database.reference.child(RIDES).child(ride.id).addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(databaseError: DatabaseError) {
                    Toasty.error(activity!!.applicationContext,
                            databaseError.message,Toast.LENGTH_SHORT).show()
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val auxRide = dataSnapshot.getValue(Ride::class.java)
                    val passengerText = StringBuilder()
                    passengerText.append(
                            resources.getString(R.string.seats)
                                    + " " + ride.riders + ", "
                                    + resources.getString(R.string.available)
                                    + " " +(ride.riders-auxRide!!.passengers.size))
                    passengerCard.text = passengerText
                }
            })
            rideImage.background = ResourcesCompat.getDrawable(resources,R.drawable.taxi,null)
        }
        val destinationText = StringBuilder()
            destinationText
                    .append(resources.getString(R.string.destinationText))
                    .append(" ")
                    .append(ride.destination)
        val originText = StringBuilder()
        originText
                .append(resources.getString(R.string.originText))
                .append(" ")
                .append(ride.origin)
        destination.text = destinationText
        origin.text =  originText
        hour.text = ride.time
        btnCancelCard.visibility=View.VISIBLE
        btnCancelCard.setOnClickListener{
            cancelRideDialog(ride)
        }
    }

    private fun cancelRideDialog(ride:Ride) {
        val builder = AlertDialog.Builder(activity!!)
        // Set the alert dialog title
        builder.setTitle("Cancel ride")
        // Display a message on alert dialog
        builder.setMessage("Do you want to cancel the ride?")
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            if (Type.REQUESTED == ride.type){
                cancelRequestedRide(ride)
            }
            else{
                cancelRequestedRide(ride)
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

    private fun cancelRequestedRide(ride:Ride){
        val db = database.reference.child(USERS)
        database.reference.child(RIDES).child(ride.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(activity!!.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rideData = dataSnapshot.getValue(Ride::class.java)
                //Si no hay pasajeros o alguien mas en el ride solo lo cancela
                if(rideData?.passengers == null ||rideData.passengers.isEmpty()){
                    db.child(ride.user).child("activeRide").removeValue()
                    db.child(ride.user).child("taked").setValue(0)
                    database.reference.child(RIDES).child(ride.id).child("status").setValue(Status.CANCELED)
                    Toasty.success(activity!!.applicationContext,
                            activity!!.resources.getString(R.string.ride_canceled),
                            Toast.LENGTH_SHORT).show()
                }
                else{
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    //si el que cancela el ride es el mismo usuario que lo publico.
                    if (ride.user == currentUser!!.uid){
                        val tokens = ArrayList<String>()
                        rideData.passengers.values.mapTo(tokens) { it.token }
                        NotificationHelper.messageToCommunity(MainActivity.fmc,tokens,activity!!.resources.getString(R.string.ride_canceled),
                                currentUser.displayName+" has canceled the ride",ride)
                        db.child(ride.user).child("activeRide").removeValue()
                        for (passenger in rideData.passengers.values){
                            db.child(passenger.id).child("activeRide").removeValue()
                            db.child(passenger.id).child("taked").setValue(0)
                        }
                        database.reference.child(RIDES).child(ride.id).child("status")
                                .setValue(Status.CANCELED)
                        database.reference.child(RIDES).child(ride.id).child("passengers")
                                .removeValue()

                    }
                    //cuando un usuario distinto al que publico el ride lo cancela.
                    else{
                        database.reference.child(USERS).child(ride.user)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Toasty.error(activity!!.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val rideUser = dataSnapshot.getValue(User::class.java)
                                        NotificationHelper.message(MainActivity.fmc,rideUser!!.token,
                                                activity!!.resources.getString(R.string.ride_canceled),
                                                currentUser.displayName+"has canceled")
                                        db.child(currentUser.uid).child("activeRide").removeValue()
                                        db.child(currentUser.uid).child("taked").setValue(0)
                                        database.reference.child(RIDES).child(ride.id).child("status").setValue(Status.ACTIVE)
                                        database.reference.child(RIDES).child(ride.id)
                                                .child("passengers")
                                                .child(currentUser.uid).removeValue()
                                    }
                                })
                    }
                }
            }
        })
    }


    companion object {
        fun changeStatusWhenTimeOver(ride:Ride, activity:Activity){
            val database = FirebaseDatabase.getInstance()
            val db = database.reference.child(USERS)
            database.reference.child(RIDES).child(ride.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Toasty.error(activity.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val rideData = dataSnapshot.getValue(Ride::class.java)
                    val tokens = ArrayList<String>()
                    rideData!!.passengers.values.mapTo(tokens) { it.token }
                    NotificationHelper.messageToCommunity(MainActivity.fmc,tokens,activity.resources.getString(R.string.ride_timeout_tittle),
                            activity.resources.getString(R.string.ride_timeout_body),ride)
                    db.child(ride.user).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(databaseError: DatabaseError) {
                            Toasty.error(activity.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(User::class.java)
                            NotificationHelper.message(MainActivity.fmc,user!!.token,activity.resources.getString(R.string.ride_timeout_tittle),
                                    activity.resources.getString(R.string.ride_timeout_body),ride)
                        }

                    })
                    db.child(ride.user).child("activeRide").removeValue()
                    db.child(ride.user).child("taked").setValue(0)
                    for (passenger in rideData.passengers.values){
                        db.child(passenger.id).child("activeRide").removeValue()
                        db.child(passenger.id).child("taked").setValue(0)
                    }
                    database.reference.child(RIDES).child(ride.id).child("status")
                            .setValue(Status.FINISHED)
                    database.reference.child(RIDES).child(ride.id).child("passengers")
                            .removeValue()

                }
            })
        }
    }
}// Required empty public constructor
