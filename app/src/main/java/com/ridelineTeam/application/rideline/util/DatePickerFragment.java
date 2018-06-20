package com.ridelineTeam.application.rideline.util;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by Hedryan on 29/03/2018.
 */

public class DatePickerFragment extends DialogFragment{



    private DatePickerDialog.OnDateSetListener listener;


     public static DatePickerFragment  newInstance(DatePickerDialog.OnDateSetListener listener){
         DatePickerFragment datePickerFragment = new DatePickerFragment();
         datePickerFragment.listener= listener;
         return datePickerFragment;
     }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(getActivity(),listener,year,month,day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        return datePickerDialog;
    }

}
