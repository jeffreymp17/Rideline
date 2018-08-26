package com.ridelineTeam.application.rideline.cloudMessageServices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ridelineTeam.application.rideline.MainActivity;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.view.fragment.ChatCommunityActivity;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final Integer CHAT_ID = 121;
    private static final Integer NORMAL_CHAT = 122;
    private static final String NOTIFICATION_CHANNEL="RIDELINE_APPLICATION";
    private static Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
    private static int value = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData().containsKey("community_key")) {
            String key = remoteMessage.getData().get("community_key");
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            showNotificationInboxStyle(title, body, key);
        }
        else{
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            showNotificationBigTextStyle(title,body);
        }
    }
    private void showNotificationBigTextStyle(String title, String body) {
        Uri uriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent piResult = PendingIntent.getActivity(this, 0, intent, 0);
        Notification.Builder builder= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ride_thumb)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(new Notification.BigTextStyle().bigText(body))
                    .setSound(uriRingtone)
                    .setContentIntent(piResult);
        }

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        assert builder != null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, TAG, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify("Rideline",NORMAL_CHAT,builder.build());


    }
    private void showNotificationInboxStyle(String title,String body,String community){
        value++;
        inboxStyle.setBigContentTitle("Here Your Messages");
        inboxStyle.addLine(body);
        inboxStyle.setSummaryText("+"+value+" more");
        Uri uriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent=new Intent(this,ChatCommunityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("communityKey",community);
        PendingIntent piResult = PendingIntent.getActivity(this, 0, intent, 0);
        Notification.Builder builder= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ride_thumb)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText("Have new messages")
                    .setSound(uriRingtone)
                    .setContentIntent(piResult);
            builder.setStyle(inboxStyle);
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        assert builder != null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, TAG, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify("Rideline",CHAT_ID,builder.build());


    }
}
