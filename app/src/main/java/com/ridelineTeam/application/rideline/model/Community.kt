package com.ridelineTeam.application.rideline.model

import com.ridelineTeam.application.rideline.model.enums.Type
import com.ridelineTeam.application.rideline.util.enums.CommunityType
import java.io.Serializable
import kotlin.collections.ArrayList


data class Community(var name:String="",
                     var description:String="",
                     var users:ArrayList<String> = ArrayList(),
                     var createdBy:String="",
                     var admin:String="",
                     var createdDate:String="",
                     var type:CommunityType=CommunityType.PRIVATE,
                     var id:String=""): Serializable