package com.ridelineTeam.application.rideline.cloudMessageServices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ridelineTeam.application.rideline.MainActivity;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.Community;
import com.ridelineTeam.application.rideline.model.Ride;
import com.ridelineTeam.application.rideline.view.fragment.ChatCommunityActivity;

import java.util.ArrayList;

import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.COMMUNITIES;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    ArrayList<String> notifications = new ArrayList<>();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
       String message= remoteMessage.getNotification().getClickAction();
       Log.d("MESSAGE","CLICK_ACTION:"+message);
        if(remoteMessage.getNotification()!=null){
            if(remoteMessage.getData().equals("riders")){
                Ride ride=new Ride();
                ride.setCommunity(remoteMessage.getData().get("community"));
                ride.setOrigin(remoteMessage.getData().get("origin"));
                ride.setDestination(remoteMessage.getData().get("destination"));
                showNotification(remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody(),ride);
            }
             else{
                 String key=remoteMessage.getData().get("communityChat");
                 showNotification(remoteMessage.getNotification().getTitle(),
                         remoteMessage.getNotification().getBody(),key);
            }
        }
    }

    private void showNotification(String title, String body,Ride ride) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.if_citycons_car_1342944);
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ride_thumb)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bmp)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setTimeoutAfter(1000)
                .setSound(uriRingtone).setContentIntent(pendingIntent);

        NotificationManager notification=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notification.notify(0,builder.build());
    }

    private void showNotification(String title, String body) {
        NotificationCompat.InboxStyle style=new NotificationCompat.InboxStyle();
        notifications.add(body);
        for (int i=0; i < notifications.size(); i++) {
            Log.d("-------------_>","NOTI:"+notifications);
            Log.d("Count",""+i);
            style.addLine(notifications.get(i));
        }
        style.setBigContentTitle(title);
        style.setSummaryText(""+notifications.size()+ getResources().getString(R.string.inboxMessages));
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.if_citycons_car_1342944);
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ride_thumb)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(body)
                .setOnlyAlertOnce(true)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bmp)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setStyle(style)
                .setAutoCancel(true)
                .setTimeoutAfter(1000)
                .setGroup("community")
                .setSound(uriRingtone).setContentIntent(pendingIntent);

        NotificationManager notification=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notification.notify(2,builder.build());

    }
    private void showNotification(String title, String body,String community) {
        NotificationCompat.InboxStyle style=new NotificationCompat.InboxStyle();
        notifications.add(body);
        for (int i=0; i < notifications.size(); i++) {
            Log.d("-------------_>","NOTI:"+notifications);
            Log.d("Count",""+i);
            style.addLine(notifications.get(i));
        }
        style.setBigContentTitle(title);
        style.setSummaryText(""+notifications.size()+ getResources().getString(R.string.inboxMessages));
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.if_citycons_car_1342944);
        Intent intent=new Intent(this,ChatCommunityActivity.class);
        intent.putExtra("communityKey",community);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ride_thumb)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(body)
                .setOnlyAlertOnce(true)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bmp)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setStyle(style)
                .setAutoCancel(true)
                .setTimeoutAfter(1000)
                .setGroup("community")
                .setSound(uriRingtone).setContentIntent(pendingIntent);

        NotificationManager notification=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notification.notify(2,builder.build());

    }
}
