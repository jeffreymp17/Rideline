package com.ridelineTeam.application.rideline.model

data class User(var id:String = "",
                var email: String="",
                var name:String="",
                var lastName:String="",
                var address:String="",
                var pictureUrl:String="",
                var token:String="",
                var status:String="",
                var communities:ArrayList<String> = ArrayList(),
                var taked:Int=0,
                var telephone:Int=0,
                var activeRide:Ride?=null)