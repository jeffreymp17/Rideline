package com.ridelineTeam.application.rideline.util.helpers;


import android.content.Context;
import android.media.RingtoneManager;

import com.ridelineTeam.application.rideline.BuildConfig;
import com.ridelineTeam.application.rideline.model.Community;
import com.ridelineTeam.application.rideline.model.Ride;

import org.pixsee.fcm.Message;
import org.pixsee.fcm.Notification;
import org.pixsee.fcm.Sender;

import java.util.List;

import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.FIREBASE_SERVER_DEV;

/**
 * Se crean metodos para las notificaciones
 * que puede ser invocados en cualquier parte del codigo que se necesiten
 * */
public class NotificationHelper {

    //CREA LA NOTIFICACION QUE VA SER ENVIA CON LOS PARAMETROS QUE DESEE EN EL CUERPO Y TITULO.
    private static Notification notification(String tittle,String body){
        Notification notification = new Notification("title", "body");
        notification.setTitle(tittle);
        notification.setBody(body);
        notification.setSound(String.valueOf(RingtoneManager.TYPE_NOTIFICATION));
        return notification;
    }


    public static void message(Sender fcm, String toClientToken, String tittle, String body){
        Message message = new Message.MessageBuilder()
                .toToken(toClientToken) // single android/ios device
                .notification(new Notification(tittle,body))
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
    //NOTIFICACION DE PUNTO A PUNTO
    public static void message(Sender fcm,String toClientToken,String tittle,String body,Ride ride){
        Message message = new Message.MessageBuilder()
                .toToken(toClientToken) // single android/ios device
                .addData("date",ride.getDate())
                .addData("roundTrip",ride.getRoundTrip())
                .addData("riders",ride.getRiders())
                .addData("origin",ride.getOrigin())
                .addData("destination",ride.getDestination())
                .addData("community",ride.getCommunity())
                .notification(new Notification(tittle,body))
                .toTopic("ride")
                .priority(Message.Priority.HIGH)
                .build();

        fcm.send(message);
    }

    //CREA EL MENSAJE QUE ENVIARA A LAS COMUNIDADES A LAS QUE PERTENEZCA
    public static void messageToCommunity(Sender fcm, List<String> tokens, String tittle, String body, Ride ride){
        Message message = new Message.MessageBuilder()
                .addRegistrationToken(tokens)
                .addData("date",ride.getDate())
                .addData("roundTrip",ride.getRoundTrip())
                .addData("riders",ride.getRiders())
                .addData("origin",ride.getOrigin())
                .addData("destination",ride.getDestination())
                .addData("community",ride.getCommunity())
                .notification(notification(tittle,body))
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
    public static void messageToCommunity(Sender fcm, List<String> tokens, String tittle, String body){
        Message message = new Message.MessageBuilder()
                .addRegistrationToken(tokens)
                .notification(notification(tittle,body))
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
}
