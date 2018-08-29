package com.ridelineTeam.application.rideline.util.helpers;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;


public class PermissionHelper {
    /**
     * Permite validar si ya existe algun permiso para a la app
     * como la ubicacion por ejemplo
     */
    @NonNull
    public static Boolean checkPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void disableScreenInteraction(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void enableScreenInteraction(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
