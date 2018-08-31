package com.ridelineTeam.application.rideline.util.helpers;

import android.app.Activity;
import org.pixsee.fcm.Message;
import org.pixsee.fcm.Sender;
import java.util.List;



/**
 * Se crean metodos para las notificaciones
 * que puede ser invocados en cualquier parte del codigo que se necesiten
 * */
public class NotificationHelper {

    public static void message(Sender fcm, String toClientToken, String title, String body){
        Message message = new Message.MessageBuilder()
                .toToken(toClientToken) // single android/ios device
                .addData("title",title)
                .addData("body",body)
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }

    //CREA EL MENSAJE QUE ENVIARA A LAS COMUNIDADES A LAS QUE PERTENEZCA
    public static void messageToCommunity(Sender fcm, List<String> tokens, String title, String body){
        Message message = new Message.MessageBuilder()
                .addRegistrationToken(tokens)
                .addData("title",title)
                .addData("body",body)
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
    public static void messageToCommunity(Sender fcm, List<String> tokens, String title,
                                          String body,String communityKey){
        Message message = new Message.MessageBuilder()
                .addRegistrationToken(tokens)
                .addData("community_key",communityKey)
                .addData("title",title)
                .addData("body",body)
                .priority(Message.Priority.HIGH)
                .build();
        fcm.send(message);
    }
}
