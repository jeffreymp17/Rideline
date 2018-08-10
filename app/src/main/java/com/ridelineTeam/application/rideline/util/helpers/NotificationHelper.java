package com.ridelineTeam.application.rideline.util.helpers;


import android.app.Activity;
import android.media.RingtoneManager;

import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Ride;
import org.pixsee.fcm.Message;
import org.pixsee.fcm.Notification;
import org.pixsee.fcm.Sender;

import java.util.List;


/**
 * Se crean metodos para las notificaciones
 * que puede ser invocados en cualquier parte del codigo que se necesiten
 * */
public class NotificationHelper {

    //CREA LA NOTIFICACION QUE VA SER ENVIA CON LOS PARAMETROS QUE DESEE EN EL CUERPO Y TITULO.
    private static Notification notification(String tittle,String body, Activity activity){
        Notification notification = new Notification(tittle, body);
        notification.setIcon(activity.getResources().getString(R.string.notification_icon));
        notification.setSound(String.valueOf(RingtoneManager.TYPE_NOTIFICATION));
        return notification;
    }

    public static void message(Sender fcm, String toClientToken, String tittle, String body, Activity activity){
        Message message = new Message.MessageBuilder()
                .toToken(toClientToken) // single android/ios device
                .notification(notification(tittle,body,activity))
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }

    //CREA EL MENSAJE QUE ENVIARA A LAS COMUNIDADES A LAS QUE PERTENEZCA
    public static void messageToCommunity(Sender fcm, List<String> tokens, String tittle, String body,Activity activity){
        Message message = new Message.MessageBuilder()
                .addRegistrationToken(tokens)
                .notification(notification(tittle,body,activity))
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
    public static void messageToCommunity(Sender fcm, List<String> tokens, String tittle,
                                          String body,String communityKey,Activity activity){
        Message message = new Message.MessageBuilder()
                .addRegistrationToken(tokens)
                .notification(notification(tittle,body,activity))
                .addData("communityChat",communityKey)
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
}
