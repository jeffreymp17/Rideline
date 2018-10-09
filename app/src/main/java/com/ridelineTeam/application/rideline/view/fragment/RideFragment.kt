package com.ridelineTeam.application.rideline.view.fragment


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
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
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.DialogPlusBuilder
import com.orhanobut.dialogplus.ViewHolder
import com.ridelineTeam.application.rideline.model.RideCost
import com.ridelineTeam.application.rideline.util.enums.Cost
import com.ridelineTeam.application.rideline.util.enums.Restrictions
import kotlinx.android.synthetic.main.custom_price.*
import java.lang.Double
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
    private lateinit var arrayOfRestrictions: ArrayList<Restrictions>
    private lateinit var arrayOfPosition: ArrayList<Int>
    private var country = ""
    private lateinit var btnCustom:Button
    private lateinit var btnCustomPrice: FloatingActionButton
    private lateinit var btnFree:FloatingActionButton
    private lateinit var txtCustomPrice:EditText
    private lateinit var btnCloseBottomDialog:Button
    private lateinit var customDialogView:View
    private lateinit var priceLayout: RelativeLayout
    private  var cost: RideCost?=null


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
        btnCustom = rootView.findViewById(R.id.custom_cost)
        getCommunitiesIds()
        btnRestrictions.setOnClickListener {
            Log.d("her", "yes")
            multiSelectDialog()

        }

        btnCustom.setOnClickListener {
         dialogCustomPrice()
        }
        return rootView
    }

    private fun dialogCustomPrice() {
       val dialog:DialogPlus? = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(R.layout.custom_price))
                .setExpanded(true)
                .setCancelable(false)
                .create()
        dialog!!.footerView
        dialog!!.show()
        dialogPaymentData(dialog)
    }

    private fun dialogPaymentData(dialog: DialogPlus) {
        customDialogView = dialog.holderView
        btnCustomPrice = customDialogView.findViewById(R.id.btnCustomPrice)
        btnFree = customDialogView.findViewById(R.id.btnFree)
        btnCloseBottomDialog = customDialogView.findViewById(R.id.btnCloseBottomDialog)
        txtCustomPrice = customDialogView.findViewById(R.id.txtCustomPrice)
        priceLayout = customDialogView.findViewById(R.id.insert_price_layout)
        btnCustomPrice.setOnClickListener {
            priceLayout.visibility = View.VISIBLE
        }
        btnFree.setOnClickListener {
            priceLayout.visibility = View.GONE
        }
        btnCloseBottomDialog.setOnClickListener {
            cost = if (!TextUtils.isEmpty(txtCustomPrice.text.toString())) {
                RideCost(Cost.PAID.toString(), Double.parseDouble(txtCustomPrice.text.toString()))


            } else {
                RideCost(Cost.FREE.toString(), 0.0)
            }
            Log.d("PRICE", "RIDE:$cost")
            dialog.dismiss()
        }
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
                            status = Status.ACTIVE,
                            type = typeOfRide,
                            user = userId,
                            destination = "",
                            origin = "",
                            community = community,
                            time = time.text.toString(),
                            restrictions = arrayOfRestrictions,
                            cost=cost

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
        val restrictionsLocal = ArrayList<Restrictions>()
        enumRestrictions.addAll(listOf(Restrictions.PET, Restrictions.FOOD, Restrictions.CHILD, Restrictions.SLEEP, Restrictions.FOOD))
        val res = resources.getStringArray(R.array.restrictions_array)
        val restrictions = ArrayList<Any>()
        restrictions.addAll(res)
        MaterialDialog.Builder(context!!)
                .title(R.string.restrictions)
                .items(restrictions)
                .itemsCallbackMultiChoice(arrayOfPosition.toTypedArray(), { _, which, _ ->
                    for (i in which) {
                        restrictionsLocal.add(enumRestrictions[i])
                        indicesLocal.add(i)

                    }
                    arrayOfPosition=indicesLocal
                    arrayOfRestrictions = restrictionsLocal
                    return@itemsCallbackMultiChoice true
                }).positiveText("Ok")
                .negativeText(resources.getString(R.string.cancel)).show()
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
            cost==null->{
                Toasty.warning(context!!,getString(R.string.required_payment),Toast.LENGTH_LONG).show()
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
        arrayCommunitiesNames.clear()
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
