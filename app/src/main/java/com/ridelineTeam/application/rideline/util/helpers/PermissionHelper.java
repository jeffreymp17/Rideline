package com.ridelineTeam.application.rideline.util.helpers;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {
    /**
     * Permite validar si ya existe algun permiso para a la app
     * como la ubicacion por ejemplo
     */
    @NonNull
    public  static  Boolean checkPermission(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
