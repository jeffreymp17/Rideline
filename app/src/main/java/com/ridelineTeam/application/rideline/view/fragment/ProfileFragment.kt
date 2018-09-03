package com.ridelineTeam.application.rideline.view.fragment


import android.app.Activity
import android.content.Context
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
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.MainActivity.Companion.userId
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.ProfileAdapter
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.enums.Status
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.enums.Restrictions
import com.ridelineTeam.application.rideline.util.files.*
import com.ridelineTeam.application.rideline.util.helpers.*
import com.ridelineTeam.application.rideline.view.PeopleRideDetailActivity
import com.squareup.picasso.Picasso
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.cardview.*
import kotlinx.android.synthetic.main.fragment_ride.*


class ProfileFragment : Fragment() {
    private lateinit var profilePicture: CircleImageView
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var imageCircle: CircleImageView
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var id: String
    private lateinit var pictureBytes: ByteArray
    private lateinit var photoRef: String
    private lateinit var fireStorage: FirebaseStorage
    private lateinit var sReference: StorageReference
    private lateinit var cardView: CardView
    private lateinit var btnEdit: FloatingActionButton
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
    private lateinit var materialDialog: MaterialDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_profile, container, false)
        database = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        btnEdit = rootView.findViewById(R.id.edit_profile)
        cardView = rootView.findViewById(R.id.cardViewRide)
        id = user!!.uid
        fireStorage = FirebaseStorage.getInstance()
        sReference = fireStorage.reference.child(PROFILE_PICTURES)
        imageCircle = rootView.findViewById(R.id.my_picture_profile)
        recycler = rootView.findViewById(R.id.recycler_profile)
        reference = database.reference.child(USERS)
        name = rootView.findViewById(R.id.profile_name)
        email = rootView.findViewById(R.id.user_email)
        profilePicture = rootView.findViewById(R.id.my_picture_profile)
        btnEdit.setOnClickListener {
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

        getUserProfile()
        materialDialog = MaterialDialog.Builder(context!!)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .progress(true, 0).build()
        return rootView
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title=getString(R.string.profile)
    }
    override fun onStart() {
        super.onStart()
        statistics()
        frameLayoutCard.setOnClickListener{
            startActivity(Intent(context, PeopleRideDetailActivity::class.java)
                .putExtra("rideObject", user!!.activeRide)) 
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK) {
            uri = data!!.data
            photoRef = uri.lastPathSegment
            pictureBytes = ImageHelper.resizeBytesImage(context, imageCircle, data)
            if(user!!.pictureUrl != ""){
                deleteProfilePicture(user!!.pictureUrl)
            }
            showProgressBar()
            uploadProfileImage()
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
    private fun statistics() {
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
    private fun deleteProfilePicture(imageUrl:String){
        val deletePhoto:StorageReference = FirebaseStorage.getInstance().reference.child(PROFILE_PICTURES)
         deletePhoto.storage.getReferenceFromUrl(imageUrl).delete().addOnSuccessListener {
            Log.d("FOUND", "onSuccess: deleted file")
        }.addOnFailureListener {
            Log.d("NOT FOUND", "onFailure: did not delete file")

        }

    }
    //EDIT AND CHNAGE DATA IN FIREBASE
    private fun newProfile(newName: String, newLastNames: String,
                           newStatus:String) {
        reference.child(id).child("name").setValue(newName)
        reference.child(id).child("lastName").setValue(newLastNames)
        reference.child(id).child("status").setValue(newStatus)
        Toasty.success(context!!, getString(R.string.profileUpdate), Toast.LENGTH_SHORT, true).show()
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

        dialogBuilder.setTitle(R.string.personal_information)
        dialogBuilder.setPositiveButton(R.string.done) { _, _ ->
            if (!TextUtils.isEmpty(txtName.text) && !TextUtils.isEmpty(txtLastName.text)) {
                newProfile(txtName.text.toString(), txtLastName.text.toString()
                        ,txtStatus.text.toString())

            }

        }
        dialogBuilder.setNegativeButton(R.string.cancel) { _, _ ->

        }

        val b = dialogBuilder.create()
        b.show()
    }
    //THIS METHOD UPLOAD PROFILE PICTORE IN FIREABSE SERVER
    private fun uploadProfileImage() {
        reference = database.reference.child(USERS).child(id)
        sReference = fireStorage.reference.child(PROFILE_PICTURES).child(photoRef)
        sReference.putBytes(pictureBytes).addOnSuccessListener {
            if (it.task.isComplete) {
                sReference.downloadUrl.addOnSuccessListener({ uri ->
                    with(reference) {
                        child("pictureUrl").setValue(uri.toString())

                    }
                    Picasso.with(context).load(uri.toString()).into(imageCircle)
                    hideProgressBar()
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
                var fullName = user!!.name+" "+user.lastName
                fullName = DateTimeAndStringHelper.truncate(fullName, 15)
                if(user.id==id) fullName = getString(R.string.you)
                userCard.text=fullName
                if (!user.pictureUrl.isEmpty())
                    Picasso.with(activity).load(user.pictureUrl).fit().into(userPicture)
                else
                    Picasso.with(activity).load(R.drawable.avatar).fit().into(userPicture)

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
                    .append(DateTimeAndStringHelper.formatRoute(ride.destination))
        val originText = StringBuilder()
        originText
                .append(resources.getString(R.string.originText))
                .append(" ")
                .append(DateTimeAndStringHelper.formatRoute(ride.origin))
        destination.text = destinationText
        origin.text =  originText
        hour.text = ride.time
        btnCancelCard.visibility=View.VISIBLE
        btnCancelCard.setOnClickListener{
            cancelRideDialog(ride,activity!!)
        }
    }

    private fun showProgressBar(){
        materialDialog.show()
        PermissionHelper.disableScreenInteraction(activity!!.window)
    }
    private fun hideProgressBar(){
        materialDialog.dismiss()
        PermissionHelper.enableScreenInteraction(activity!!.window)
    }

    companion object {
         fun cancelRideDialog(ride:Ride,activity: Activity) {
            val builder = AlertDialog.Builder(activity)
            // Set the alert dialog title
            builder.setTitle(R.string.cancel_ride)
            // Display a message on alert dialog
            builder.setMessage(R.string.ride_cancel_message)
            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton(activity.resources.getString(R.string.yes)) { _, _ ->
                cancelRequestedRide(ride,activity)
            }
            // Display a negative button on alert dialog
            builder.setNegativeButton(activity.resources.getString(R.string.no)) { _, _ ->
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()
        }
        private fun cancelRequestedRide(ride:Ride,activity: Activity){
            val database = FirebaseDatabase.getInstance()
            val db = database.reference.child(USERS)
            database.reference.child(RIDES).child(ride.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Toasty.error(activity.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val rideData = dataSnapshot.getValue(Ride::class.java)
                    //Si no hay pasajeros o alguien mas en el ride solo lo cancela
                    if(rideData?.passengers == null ||rideData.passengers.isEmpty()){
                        db.child(ride.user).child("activeRide").removeValue()
                        db.child(ride.user).child("taked").setValue(0)
                        database.reference.child(RIDES).child(ride.id).child("status").setValue(Status.CANCELED)
                        Toasty.success(activity.applicationContext,
                                activity.resources.getString(R.string.ride_canceled),
                                Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        //si el que cancela el ride es el mismo usuario que lo publico.
                        if (ride.user == currentUser!!.uid){
                            val tokens = ArrayList<String>()
                            rideData.passengers.values.mapTo(tokens) { it.token }
                            NotificationHelper.messageToCommunity(MainActivity.fmc,
                                    tokens,activity.resources.getString(R.string.ride_canceled),
                                    currentUser.displayName + activity.resources.getString(R.string.cancel_ride_notification))
                            db.child(ride.user).child("activeRide").removeValue()
                            for (passenger in rideData.passengers.values){
                                db.child(passenger.id).child("activeRide").removeValue()
                                db.child(passenger.id).child("taked").setValue(0)
                            }
                            database.reference.child(RIDES).child(ride.id).child("status")
                                    .setValue(Status.CANCELED)
                            database.reference.child(RIDES).child(ride.id).child("passengers")
                                    .removeValue()
                            Toasty.success(activity.applicationContext,
                                    activity.resources.getString(R.string.ride_canceled),
                                    Toast.LENGTH_SHORT).show()
                        }
                        //cuando un usuario distinto al que publico el ride lo cancela.
                        else{
                            database.reference.child(USERS).child(ride.user)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Toasty.error(activity.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val rideUser = dataSnapshot.getValue(User::class.java)
                                            NotificationHelper.message(MainActivity.fmc,rideUser!!.token,
                                                    activity.resources.getString(R.string.ride_canceled),
                                                    currentUser.displayName+ activity.resources.getString(R.string.has_canceled))
                                            db.child(currentUser.uid).child("activeRide").removeValue()
                                            db.child(currentUser.uid).child("taked").setValue(0)
                                            database.reference.child(RIDES).child(ride.id).child("status").setValue(Status.ACTIVE)
                                            database.reference.child(RIDES).child(ride.id)
                                                    .child("passengers")
                                                    .child(currentUser.uid).removeValue()
                                            Toasty.success(activity.applicationContext,
                                                    activity.resources.getString(R.string.ride_canceled),
                                                    Toast.LENGTH_SHORT).show()
                                        }
                                    })
                        }
                    }
                }
            })
        }
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
                            activity.resources.getString(R.string.ride_timeout_body))
                    db.child(ride.user).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(databaseError: DatabaseError) {
                            Toasty.error(activity.applicationContext,databaseError.message,Toast.LENGTH_SHORT).show()
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(User::class.java)
                            NotificationHelper.message(MainActivity.fmc,user!!.token,activity.resources.getString(R.string.ride_timeout_tittle),
                                    activity.resources.getString(R.string.ride_timeout_body))
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
