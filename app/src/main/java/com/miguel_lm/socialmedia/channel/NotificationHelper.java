package com.miguel_lm.socialmedia.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Message;
import java.util.Date;


public class NotificationHelper extends ContextWrapper {

    private final static String CHANNEL_ID = "my_notification_channel";
    public final static String CHANNEL_NAME = "Infinity";
    //private final static String GROUP_ID = "my_group_01";

    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


    private NotificationManager manager;


    public NotificationHelper(Context context){
        super(context);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels(){

        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){

        if(manager == null){
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationCompat.Builder getNotification(String title, String body){

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(Color.GRAY)
                .setSmallIcon(R.drawable.ic_stat_infinity)
                .setSound(soundUri)
                .setTicker("Infinity")
                .setContentInfo("nuevo")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                //.setContentIntent(contentIntent)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificationMessage(
            Message[] messages, String usernameSender,
            String usernameReceiver, String lastMessage,
            Bitmap bitmapSender, Bitmap bitmapReceiver, NotificationCompat.Action action){

        Person person1 = null;

        if(bitmapReceiver == null){
            person1 = new Person.Builder()
                    .setName(usernameReceiver)
                    .setIcon(IconCompat.createWithResource(getApplicationContext(),
                            R.drawable.ic_person_gray)).build();
        } else {
            person1 = new Person.Builder()
                    .setName(usernameReceiver)
                    .setIcon(IconCompat.createWithBitmap(bitmapReceiver)).build();
        }

        Person person2 = null;

        if(bitmapSender == null){
            person2 = new Person.Builder()
                    .setName(usernameSender)
                    .setIcon(IconCompat.createWithResource(getApplicationContext(),
                            R.drawable.ic_person_gray)).build();
        } else {
            person2 = new Person.Builder()
                    .setName(usernameSender)
                    .setIcon(IconCompat.createWithBitmap(bitmapSender)).build();
        }

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat
                .MessagingStyle(person1);

        NotificationCompat.MessagingStyle.Message message1 = new NotificationCompat.MessagingStyle
                .Message(lastMessage, new Date().getTime(), person1);

        messagingStyle.addMessage(message1);

        for(Message m : messages){
            NotificationCompat.MessagingStyle.Message message2 = new NotificationCompat.MessagingStyle
                    .Message(m.getMessage(), m.getTimestamp(), person2);

            messagingStyle.addMessage(message2);

        }

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_infinity)
                .setStyle(messagingStyle)
                .addAction(action);
    }
}
