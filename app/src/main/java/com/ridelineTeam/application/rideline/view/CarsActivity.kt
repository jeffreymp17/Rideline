package com.ridelineTeam.application.rideline.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ridelineTeam.application.rideline.R
import com.ridelineTeam.application.rideline.model.Car
import com.ridelineTeam.application.rideline.util.helpers.FragmentHelper
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView
import kotlinx.android.synthetic.main.activity_cars.*
import com.ridelineTeam.application.rideline.dataAccessLayer.Car as CarDal
class CarsActivity : AppCompatActivity() {

    private lateinit var currentUser: FirebaseUser
    private lateinit var recycler: MultiSnapRecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var txtCarModel: EditText
    private lateinit var txtCarPlate: EditText
    private lateinit var txtCarModelLayout: TextInputLayout
    private lateinit var txtCarPlateLayout: TextInputLayout
    private lateinit var carDal: CarDal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cars)
        currentUser = FirebaseAuth.getInstance().currentUser!!
        recycler = findViewById(R.id.carsRecycler)
        carDal = CarDal(this@CarsActivity)
        initToolbar()
    }

    override fun onStart() {
        super.onStart()
        val dialog: AlertDialog = carDialog()
        carDal.all(recycler)
        fabNewCard.setOnClickListener{dialog.show()}
    }

    private fun carDialog() : AlertDialog {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.cars_dialog_view,null)
        builder.setView(dialogView)

        txtCarModel = dialogView.findViewById(R.id.txtCarModel)
        txtCarPlate = dialogView.findViewById(R.id.txtCarPlate)
        txtCarModelLayout = dialogView.findViewById(R.id.txtCarModelLayout)
        txtCarPlateLayout = dialogView.findViewById(R.id.txtCarPlateLayout)

        builder.setTitle(R.string.new_car)
        builder.setPositiveButton(this.resources.getString(R.string.save)) { _, _ -> getCarData() }
        builder.setNegativeButton(this.resources.getString(R.string.cancel)) { _, _ -> }
        return builder.create()

    }

    private fun initToolbar(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Your Cars"
        FragmentHelper.backButtonToFragment(toolbar, PeopleRideDetailActivity@ this)
    }

    private fun getCarData(){
        var isAllOk = true
        if (TextUtils.isEmpty(txtCarModel.text)){
            txtCarModelLayout.error = resources.getString(R.string.requiredFieldMessage)
            isAllOk = false
        }

        if (TextUtils.isEmpty(txtCarPlate.text)){
            txtCarPlateLayout.error = resources.getString(R.string.requiredFieldMessage)
            isAllOk = false
        }

        if(isAllOk){
            val car = Car()
            car.model = txtCarModel.text.toString()
            car.plate = txtCarPlate.text.toString()
            carDal.register(car)
        }
    }
}
