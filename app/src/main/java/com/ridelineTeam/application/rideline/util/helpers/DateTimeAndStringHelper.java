package com.ridelineTeam.application.rideline.util.helpers;


import android.support.annotation.NonNull;
import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class DateTimeAndStringHelper {

    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static Date getTime(){
       Time time=new Time();
       time.setToNow();
        String c=time.hour+":"+time.minute+":"+time.second;
        Date time1 = null;
        try {
            time1 = new SimpleDateFormat("HH:mm:ss").parse(c);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time1;
    }
    public static String dateFormat(String date){
        String[] parts = date.split("/");
        String day = parts[0];
        String month = parts[1];
        String year = parts[2];
        int monthNumber =  Integer.parseInt(parts[1]);
        if(monthNumber<=9){
            month =  "0"+monthNumber;
        }
        return day+"/"+month+"/"+year;
    }
}
