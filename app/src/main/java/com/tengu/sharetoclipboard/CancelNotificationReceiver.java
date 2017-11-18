package com.tengu.sharetoclipboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tengu.sharetoclipboard.Utils.NotificationUtil;

public class CancelNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationUtil.cancelNotifications(context);
    }
}
