package com.ridelineTeam.application.rideline.view.fragment


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import co.ceryle.radiorealbutton.RadioRealButton
import co.ceryle.radiorealbutton.RadioRealButtonGroup
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jaredrummler.materialspinner.MaterialSpinner
import com.ridelineTeam.application.rideline.view.MapsActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.DatePickerFragment
import com.ridelineTeam.application.rideline.util.helpers.DateTimeAndStringHelper
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.model.enums.Status
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_ride.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class RideFragment : Fragment() {

    private lateinit var dateRide: EditText
    private lateinit var time: EditText
    private lateinit var spinnerRiders: MaterialSpinner
    private lateinit var spinnerCommunities: MaterialSpinner
    private lateinit var btnNext: Button
    private var passenger: Int = 0
    private var community: String = ""
    private var typeOfRide:Type = Type.REQUESTED
    private lateinit var userId: String
    private lateinit var relativeSpinner: RelativeLayout
    private lateinit var arrayCommunitiesNames: ArrayList<String>
    private lateinit var arrayCommunitiesIds: ArrayList<String>
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var btn_go_community:Button
    private lateinit var radioTypeRequest:RadioRealButton
    private lateinit var radioTypeOffer:RadioRealButton
    private lateinit var radioGroupType:RadioRealButtonGroup

    private lateinit var roundTripItem:CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_ride, container, false)
        dateRide = rootView.findViewById(R.id.dateRide)
        btnNext = rootView.findViewById(R.id.btn_next)
        time = rootView.findViewById(R.id.time)
        spinnerRiders = rootView.findViewById(R.id.spinnerRiders)
        spinnerCommunities = rootView.findViewById(R.id.spinnerCommunities)
        btn_go_community=rootView.findViewById(R.id.go_to_communitites)
        relativeSpinner=rootView.findViewById(R.id.relative_spinnerCommnunity)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(USERS)
        arrayCommunitiesIds = ArrayList()
        arrayCommunitiesNames = ArrayList()
        radioGroupType = rootView.findViewById(R.id.radioGroupType)
        radioTypeRequest = rootView.findViewById(R.id.radioTypeRequest)
        radioTypeOffer = rootView.findViewById(R.id.radioTypeOffer)
        roundTripItem = rootView.findViewById(R.id.roundTripItem)
        getCommunitiesIds()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerRiders.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid
        dateRide.setOnClickListener { _ ->
            val datePickerFragment = DatePickerFragment.newInstance({ _, year, month, day ->
                val selectedDate = "$day/${(month + 1)}/$year"
                dateRide.setText(selectedDate)
                dateRide.error = null
            })
            datePickerFragment.show(activity!!.fragmentManager, "Datepicker")
        }
        time.setOnClickListener { _ ->
            val c = Calendar.getInstance()
            val hour = c .get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                        if(minutes<10){
                            time.setText(hourOfDay.toString() + ":" +"0"+ minutes)
                        }else{
                            time.setText(hourOfDay.toString() + ":" + minutes)

                        }
                    },
                    hour, minute, true)
            timePickerDialog.show()
        }
        radioGroupType.setOnClickedButtonListener { _, position ->
            typeOfRide = if(position==1){
                Type.OFFERED
            } else{
                Type.REQUESTED
            }
        }
        btnNext.setOnClickListener { _ ->
            if (validateFields()) {
                val fullDate = dateRide.text.toString()+" "+time.text.toString()
                if (validateDateAndHour(fullDate)){
                    val ride = Ride(
                            date = dateRide.text.toString(),
                            riders = passenger,
                            roundTrip = roundTripItem.isChecked,
                            status = Status.ACTIVE,
                            type = typeOfRide,
                            user = userId,
                            destination = "",
                            origin = "",
                            community = community,
                            time = time.text.toString()
                    )
                    startActivity(Intent(context, MapsActivity::class.java)
                            .putExtra("rideObject", ride))
                }
            }
        }

        spinnerRiders.setOnItemSelectedListener({ _, _, _, item ->
            passenger = Integer.parseInt(item.toString())
            spinnerRiders.error = null
        })

        spinnerCommunities.setOnItemSelectedListener({ _, position, _, _ ->
            spinnerCommunities.error = null
            community = arrayCommunitiesIds[position]
        })
        btn_go_community.setOnClickListener {
            FragmentHelper.changeFragment(CommunityFragment(),activity!!.supportFragmentManager)

        }
    }

    private fun validateFields(): Boolean {
        return when {
            passenger == 0 -> {
                spinnerRiders.error = "Required field."
                false
            }
            dateRide.text.toString().trim().isEmpty() -> {
                dateRide.error = "Required field."
                false
            }
            community == "" -> {
                spinnerCommunities.error = "Required field."
                false
            }
            time.text.toString().trim().isEmpty()->{
                time.error="Required field."
                false
            }
            else -> true
        }
    }
    /**Obtiene las comunidades a las que pertenece el usuario (Codigos del nodo (ID))*/
    private fun getCommunitiesIds() {
        val dbSource = TaskCompletionSource<DataSnapshot>()
        val dbTask = dbSource.task
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = databaseReference.child(currentUser!!.uid)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dbSource.setResult(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        dbTask.addOnSuccessListener {
            val dataSnapshot = dbTask.result
            val user = dataSnapshot.getValue(User::class.java)
            arrayCommunitiesIds = user!!.communities
            if(user!!.communities.isEmpty()){
                relativeSpinner.visibility=View.GONE
                txtPublishIn.visibility=View.GONE
                btnNext.visibility=View.GONE
                btn_go_community.visibility=View.VISIBLE
                txtNoHaveCommunities.visibility=View.VISIBLE
            }else{
                relativeSpinner.visibility=View.VISIBLE
                txtPublishIn.visibility=View.VISIBLE
                btn_go_community.visibility=View.GONE
                txtNoHaveCommunities.visibility=View.GONE
                btnNext.visibility=View.VISIBLE
                getCommunitiesNames()

            }
        }
    }

    /**Obtiene los nombres de las comunidades del usuario para mostrarlas en el dropdown menu*/
    private fun getCommunitiesNames() {
        for (id: String in arrayCommunitiesIds) {
            val dbSource = TaskCompletionSource<DataSnapshot>()
            val dbTask = dbSource.task
            val db = databaseReference.database.reference.child(COMMUNITIES)
            db.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dbSource.setResult(dataSnapshot)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            dbTask.addOnSuccessListener {
                val dataSnapshot = dbTask.result
                val community = dataSnapshot.getValue(Community::class.java)
                arrayCommunitiesNames.add(community!!.name)
                spinnerCommunities.setItems(arrayCommunitiesNames)
            }
        }
    }

    private fun validateDateAndHour(fullDate:String):Boolean{
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val dateFormat = DateTimeAndStringHelper.dateFormat(fullDate)
        val date =  formatter.parse(dateFormat)
        val currentDate = Date()
        return if (date.before(currentDate)){
            Toasty.error(activity!!.applicationContext,
                    resources.getString(R.string.Incorrect_time_message),
                    Toast.LENGTH_LONG).show()
            false
        }else{
            true
        }
    }
}// Required empty public constructor
