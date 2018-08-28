package com.ridelineTeam.application.rideline.view.fragment


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
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
import com.ridelineTeam.application.rideline.MainActivity
import com.ridelineTeam.application.rideline.view.MapsActivity
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.util.files.COMMUNITIES
import com.ridelineTeam.application.rideline.util.DatePickerFragment
import com.ridelineTeam.application.rideline.util.helpers.DateTimeAndStringHelper
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.model.Community
import com.ridelineTeam.application.rideline.model.Ride
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.enums.Status
import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.ridelineTeam.application.rideline.util.helpers.PermissionHelper
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_ride.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.afollestad.materialdialogs.MaterialDialog
import com.ridelineTeam.application.rideline.util.enums.Restrictions
import java.lang.reflect.Array


/**
 * A simple [Fragment] subclass.
 */
class RideFragment : Fragment() {

    private lateinit var dateRide: EditText
    private lateinit var dateRideLayout: TextInputLayout
    private lateinit var time: EditText
    private lateinit var timeLayout: TextInputLayout
    private lateinit var spinnerRiders: MaterialSpinner
    private lateinit var spinnerRidersLayout: TextInputLayout
    private lateinit var spinnerCommunities: MaterialSpinner
    private lateinit var spinnerCommunitiesLayout: TextInputLayout
    private lateinit var btnNext: Button
    private var passenger: Int = 0
    private var community: String = ""
    private var typeOfRide: Type = Type.REQUESTED
    private lateinit var userId: String
    private lateinit var relativeSpinner: RelativeLayout
    private lateinit var arrayCommunitiesNames: ArrayList<String>
    private lateinit var arrayCommunitiesIds: ArrayList<String>
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var btnGoCommunity: Button
    private lateinit var radioTypeRequest: RadioRealButton
    private lateinit var radioTypeOffer: RadioRealButton
    private lateinit var radioGroupType: RadioRealButtonGroup
    private lateinit var materialDialog: MaterialDialog
    private lateinit var btnRestrictions: Button
    private lateinit var arrayOfRestrictions: ArrayList<Any>
    private lateinit var arrayOfPosition: ArrayList<Int>

    private lateinit var roundTripItem:CheckBox
    private var country = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(USERS)
        arrayCommunitiesIds = ArrayList()
        arrayCommunitiesNames = ArrayList()
        arrayOfPosition=ArrayList()
        arrayOfRestrictions = ArrayList()
        materialDialog = MaterialDialog.Builder(context!!)
                .title(getString(R.string.loading))
                .content(getString(R.string.please_wait))
                .progress(true, 0).build()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_ride, container, false)
        dateRide = rootView.findViewById(R.id.dateRide)
        dateRideLayout = rootView.findViewById(R.id.dateRideLayout)
        btnNext = rootView.findViewById(R.id.btn_next)
        time = rootView.findViewById(R.id.time)
        btnRestrictions = rootView.findViewById(R.id.btn_restrictions)
        timeLayout = rootView.findViewById(R.id.timeLayout)
        spinnerRiders = rootView.findViewById(R.id.spinnerRiders)
        spinnerRidersLayout = rootView.findViewById(R.id.spinnerRidersLayout)
        spinnerCommunities = rootView.findViewById(R.id.spinnerCommunities)
        spinnerCommunitiesLayout = rootView.findViewById(R.id.spinnerCommunitiesLayout)
        btnGoCommunity = rootView.findViewById(R.id.go_to_communitites)
        relativeSpinner = rootView.findViewById(R.id.relative_spinnerCommnunity)
        radioGroupType = rootView.findViewById(R.id.radioGroupType)
        radioTypeRequest = rootView.findViewById(R.id.radioTypeRequest)
        radioTypeOffer = rootView.findViewById(R.id.radioTypeOffer)
        roundTripItem = rootView.findViewById(R.id.roundTripItem)
        getCommunitiesIds()
        btnRestrictions.setOnClickListener {
            Log.d("her", "yes")
            multiSelectDialog()

        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        showProgressBar()
        cantCreateRideWhenActive()
        hideProgressBar()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.ride)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerRiders.setItems(createPassengersItems())
        val user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid
        dateRide.setOnClickListener { _ ->
            val datePickerFragment = DatePickerFragment.newInstance({ _, year, month, day ->
                val selectedDate = "$day/${(month + 1)}/$year"
                dateRide.setText(selectedDate)
                dateRideLayout.error = null
            })
            datePickerFragment.show(activity!!.fragmentManager, "Datepicker")
        }
        time.setOnClickListener { _ ->
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                        if (minutes < 10) {
                            val timeText = hourOfDay.toString() + ":" + "0" + minutes
                            time.setText(timeText)
                        } else {
                            val timeText = hourOfDay.toString() + ":" + minutes
                            time.setText(timeText)
                        }
                        timeLayout.error = null
                    },
                    hour, minute, true)
            timePickerDialog.show()
        }
        radioGroupType.setOnClickedButtonListener { _, position ->
            typeOfRide = if (position == 1) {
                Type.OFFERED
            } else {
                Type.REQUESTED
            }
        }
        btnNext.setOnClickListener { _ ->
            showProgressBar()
            if (validateFields()) {
                val fullDate = dateRide.text.toString() + " " + time.text.toString()
                if (validateDateAndHour(fullDate)) {
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
                            time = time.text.toString(),
                            restrictions = arrayOfRestrictions
                    )
                    startActivity(Intent(context, MapsActivity::class.java)
                            .putExtra("rideObject", ride)
                            .putExtra("country",country))
                }
            }
            hideProgressBar()
        }

        spinnerRiders.setOnItemSelectedListener({ _, _, _, item ->
            val itemParts = item.toString().split(" ")
            passenger = Integer.parseInt(itemParts[0])
            spinnerRidersLayout.error = null
        })

        spinnerCommunities.setOnItemSelectedListener({ _, position, _, _ ->
            spinnerCommunitiesLayout.error = null
            community = arrayCommunitiesIds[position]
        })
        btnGoCommunity.setOnClickListener {
            FragmentHelper.changeFragment(CommunitiesFragment(), activity!!.supportFragmentManager)
            (activity as MainActivity).supportActionBar!!.title = getString(R.string.communityList)


        }
    }

    private fun multiSelectDialog() {
        val indicesLocal = ArrayList<Int>()
        val enumRestrictions = ArrayList<Restrictions>()
        val restrictionsLocal = ArrayList<Any>()
        enumRestrictions.addAll(listOf(Restrictions.PET, Restrictions.FOOD, Restrictions.CHILD, Restrictions.SLEEP, Restrictions.FOOD))
        val res = resources.getStringArray(R.array.restrictions_array)
        val restrictions = ArrayList<Any>()
        restrictions.addAll(res)
        MaterialDialog.Builder(context!!)
                .title(R.string.restrictions)
                .items(restrictions)
                .itemsCallbackMultiChoice(arrayOfPosition.toTypedArray(), { dialog, which, text ->
                    for (i in which) {
                        Log.d("IS", "MY:::$i-------->:$text")
                        Log.d("DATA", "FROM EMUN${enumRestrictions[i]}")
                        restrictionsLocal.add(enumRestrictions[i])
                        indicesLocal.add(i)
                        Log.d("DATA", "ARRAY OF SELECTRED:$restrictionsLocal")
                        Log.d("DATA", "ARRAY OF SELECTRED indix:$indicesLocal")

                    }
                    arrayOfPosition=indicesLocal
                    arrayOfRestrictions = restrictionsLocal
                    Log.d("ARRAY GLOBAL", "ARRAY---------:$arrayOfRestrictions")
                    return@itemsCallbackMultiChoice true
                }).positiveText("Ok")
                .negativeText("Cancel").show()
    }

    private fun validateFields(): Boolean {
        return when {
            passenger == 0 -> {
                spinnerRidersLayout.error = getString(R.string.requiredFieldMessage)
                false
            }
            dateRide.text.toString().trim().isEmpty() -> {
                dateRideLayout.error = getString(R.string.requiredFieldMessage)
                false
            }
            time.text.toString().trim().isEmpty() -> {
                timeLayout.error = getString(R.string.requiredFieldMessage)
                false
            }
            community == "" -> {
                spinnerCommunitiesLayout.error = getString(R.string.requiredFieldMessage)
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
            if (user.communities.isEmpty()) {
                relativeSpinner.visibility = View.GONE
                txtPublishIn.visibility = View.GONE
                btnNext.visibility = View.GONE
                btnGoCommunity.visibility = View.VISIBLE
                txtNoHaveCommunities.visibility = View.VISIBLE
            } else {
                relativeSpinner.visibility = View.VISIBLE
                txtPublishIn.visibility = View.VISIBLE
                btnGoCommunity.visibility = View.GONE
                txtNoHaveCommunities.visibility = View.GONE
                btnNext.visibility = View.VISIBLE
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

    private fun validateDateAndHour(fullDate: String): Boolean {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateFormat = DateTimeAndStringHelper.dateFormat(fullDate)
        val date = formatter.parse(dateFormat)
        val currentDate = Date()
        return if (date.before(currentDate)) {
            Toasty.error(activity!!.applicationContext,
                    resources.getString(R.string.Incorrect_time_message),
                    Toast.LENGTH_LONG).show()
            false
        } else {
            true
        }
    }

    private fun createPassengersItems(): ArrayList<String> {
        val items: ArrayList<String> = ArrayList()
        for (i in 1..12) {
            val value: String = if (i == 1) "$i ${getString(R.string.passengerText)}"
            else "$i ${getString(R.string.passengersText)}"
            items.add(value)
        }
        return items
    }

    private fun cantCreateRideWhenActive() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        val reference: DatabaseReference = databaseReference
        reference.child(currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(dataError: DatabaseError) {
                Toasty.error(activity!!.applicationContext, dataError.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                country = user!!.country
                if (user.activeRide != null) {
                    FragmentHelper.changeFragment(HomeFragment(),fragmentManager!!)
                    Toasty.info(activity!!.applicationContext, getString(R.string.rideActiveMessage), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showProgressBar() {
        materialDialog.show()
        PermissionHelper.disableScreenInteraction(activity!!.window)
    }

    private fun hideProgressBar() {
        materialDialog.dismiss()
        PermissionHelper.enableScreenInteraction(activity!!.window)
    }

}// Required empty public constructor
