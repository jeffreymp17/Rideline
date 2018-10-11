package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.adapter.CarsAdapter
import com.ridelineTeam.application.rideline.model.Car
import com.ridelineTeam.application.rideline.model.User
import com.ridelineTeam.application.rideline.util.files.USERS
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_cars.*

class CarsActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    private  var adapter: CarsAdapter.CarsAdapterRecycler?=null
    private lateinit var recycler: MultiSnapRecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var txtCarModel: EditText
    private lateinit var txtCarPlate: EditText
    private lateinit var txtCarModelLayout: TextInputLayout
    private lateinit var txtCarPlateLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cars)
        currentUser = FirebaseAuth.getInstance().currentUser!!
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child(USERS).child(currentUser.uid).child("cars")
        recycler = findViewById(R.id.carsRecycler)

        initDialogComponents()
        initToolbar()
    }

    override fun onStart() {
        super.onStart()
        loadCars()

        val dialog: AlertDialog = carDialog()

        fabNewCard.setOnClickListener{
            dialog.show()
        }
    }

    private fun carDialog() : AlertDialog {
        val builder = AlertDialog.Builder(this)
        // Set the alert dialog title
        builder.setTitle(R.string.new_car)
        builder.setView(R.layout.cars_dialog_view)
        // Display a message on alert dialog
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(this.resources.getString(R.string.save)) { _, _ ->
            getCarData()
        }
        // Display a negative button on alert dialog
        builder.setNegativeButton(this.resources.getString(R.string.cancel)) { _, _ ->
        }
        // Finally, make the alert dialog using builder
        return builder.create()

    }

    private fun initDialogComponents(){
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.cars_dialog_view,null)
        txtCarModel = dialogView.findViewById(R.id.txtCarModel)
        txtCarPlate = dialogView.findViewById(R.id.txtCarPlate)
        txtCarModelLayout = dialogView.findViewById(R.id.txtCarModelLayout)
        txtCarPlateLayout = dialogView.findViewById(R.id.txtCarPlateLayout)
    }

    private fun initToolbar(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Your Cars"
        FragmentHelper.backButtonToFragment(toolbar, PeopleRideDetailActivity@ this)
    }

    private fun loadCars(){
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        adapter = CarsAdapter.CarsAdapterRecycler(this, databaseReference, this)
        recycler.adapter = adapter
        noCarMessage()
    }

    private fun noCarMessage(){
        if(adapter!!.itemCount==0){
            noCarsText.text = getString(R.string.noCars)
        }
        else{
            noCarsText.text=""
        }
    }

    private fun registerCar(car: Car){
        Toasty.info(this,"in 2",Toast.LENGTH_SHORT).show()
        val db = database.reference.child(USERS).child(currentUser.uid)
        car.id = db.push().key!!
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Log.d("USER",user.toString())
                        user.cars.add(car)
                        db.setValue(user)
                    }
                }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getCarData(){
        Toasty.info(this,"in",Toast.LENGTH_SHORT).show()

        var register = true
        if (TextUtils.isEmpty(txtCarModel.text)){
            txtCarModelLayout.error = resources.getString(R.string.requiredFieldMessage)
            register = false
        }

        if (TextUtils.isEmpty(txtCarPlate.text)){
            txtCarPlateLayout.error = resources.getString(R.string.requiredFieldMessage)
            register = false
        }
        if(register){
            Toasty.info(this,"in 1",Toast.LENGTH_SHORT).show()

            val car = Car()
            car.model = txtCarModel.text.toString()
            car.plate = txtCarPlate.text.toString()

            registerCar(car)

        }
    }
}
