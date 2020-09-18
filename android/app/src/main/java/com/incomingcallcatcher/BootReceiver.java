package com.incomingcallcatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.incomingcallcatcher.service.CallService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = context.getSharedPreferences("com.incomingcallcatcher.preferences",Context.MODE_PRIVATE);
        boolean isCallServiceActive = sharedPref.getBoolean("isCallServiceActive", false);
        if (isCallServiceActive) context.startService(new Intent(context,CallService.class));
    }
}
