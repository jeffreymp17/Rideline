package com.ridelineTeam.application.rideline.model

import java.io.Serializable
import kotlin.collections.ArrayList


data class Community(var name:String="",
                     var description:String="",
                     var users:ArrayList<String> = ArrayList(),
                     var createdBy:String="",
                     var createdDate:String="",
                     var id:String=""): Serializable