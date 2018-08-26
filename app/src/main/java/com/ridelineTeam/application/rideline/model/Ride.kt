package com.ridelineTeam.application.rideline.model


import com.ridelineTeam.application.rideline.model.enums.Status
import com.ridelineTeam.application.rideline.model.enums.Type
import java.io.Serializable

data class Ride(
                var id:String="",
                var date: String = "",
                var origin:String = "",
                var destination:String = "",
                var riders:Int=0,
                var type: Type= Type.REQUESTED,
                var roundTrip:Boolean=false,
                var status: Status=Status.ACTIVE,
                var community:String = "",
                var user:String="",
                var time:String="",
                var passengers: Map<String,User> = HashMap(),
                var restrictions:ArrayList<Any> = ArrayList()
                ) : Serializable
