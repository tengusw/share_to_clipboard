package com.tengu.sharetoclipboard.Utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.widget.Toast;

import com.tengu.sharetoclipboard.CancelNotificationReceiver;
import com.tengu.sharetoclipboard.R;
import com.tengu.sharetoclipboard.ShareToOtherAppsActivity;

import static android.R.attr.id;

/**
 * Created by tal on 18/11/17.
 */

public class NotificationUtil {
    private static final String NOTIFICATION_CHANNEL = "general";
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_DURATION = 4000;

    public static void createNotification(Activity activity) {
        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL, activity.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_DEFAULT));
        }

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, NOTIFICATION_CHANNEL);
        builder.setContentTitle(activity.getString(R.string.notification_title));
        builder.setContentText(activity.getString(R.string.notification_content));
        builder.setAutoCancel(true);
        Intent intent = new Intent(activity, ShareToOtherAppsActivity.class);
        Intent clonedIntent = new Intent(activity.getIntent());
        intent.putExtra("intent", clonedIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        activity,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        int sdk = Build.VERSION.SDK_INT;
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVibrate(new long[0]);
            builder.setSmallIcon(R.drawable.ic_notification_logo);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
            Toast.makeText(activity, activity.getString(R.string.copied), Toast.LENGTH_LONG).show();
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // set up alarm
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent receiver = new Intent(activity, CancelNotificationReceiver.class);
        receiver.setAction("com.your.package.action.CANCEL_NOTIFICATION");
        receiver.putExtra("notification_id", id);
        PendingIntent pi = PendingIntent.getBroadcast(
                activity, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + NOTIFICATION_DURATION,
                pi);
    }

    public static void cancelNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
