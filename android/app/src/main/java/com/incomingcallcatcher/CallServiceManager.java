package com.incomingcallcatcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.incomingcallcatcher.service.CallService;

public class CallServiceManager extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    CallServiceManager(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }
    @NonNull
    @Override
    public String getName() {
        return "CallServiceManager";
    }

    @ReactMethod
    public void startCallService(){
        reactContext.startService(new Intent(reactContext, CallService.class));
    }

    @ReactMethod
    public void stopCallService(){
        reactContext.stopService(new Intent(reactContext, CallService.class));
    }

    @ReactMethod
    public void saveIsCallServiceActive(boolean isCallServiceActive){
        SharedPreferences sharedPref = reactContext.getSharedPreferences("com.incomingcallcatcher.preferences",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isCallServiceActive",isCallServiceActive);
        editor.commit();
    }

    @ReactMethod
    public void startFormActivity(String phoneNumber){
        Intent intent = new Intent(reactContext,FormActivity.class);
        intent.putExtra("phoneNumber",phoneNumber);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }
}
