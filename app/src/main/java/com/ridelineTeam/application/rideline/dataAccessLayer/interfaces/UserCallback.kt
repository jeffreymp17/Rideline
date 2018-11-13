package com.ridelineTeam.application.rideline.dataAccessLayer.interfaces

import com.ridelineTeam.application.rideline.model.User

interface UserCallback {
    fun onGetUserCallback(user: User)
}