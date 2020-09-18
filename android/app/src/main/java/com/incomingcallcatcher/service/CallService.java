package com.incomingcallcatcher.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.core.app.NotificationCompat;

import com.facebook.react.HeadlessJsTaskService;
import com.incomingcallcatcher.MainActivity;
import com.incomingcallcatcher.R;

import java.io.File;
import java.io.IOException;

public class CallService extends Service {

    private static final int SERVICE_NOTIFICATION_ID = 1235;
    private static final String CHANNEL_ID = "INCOMINGCALLCATCHER";

    private BroadcastReceiver callStateReceiver;
    private int oldState = -1;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephony;

    private MediaRecorder recorder;
    private boolean recordstarted = false;
    private AudioManager am;

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "CALLCATCHER", importance);
            channel.setDescription("INCOMINGCALLCATCHER");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        registerCallStateReceiver();
    }

    private void registerCallStateReceiver() {
        callStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Intent callIntent = new Intent(context, CallEventService.class);
                phoneStateListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        callIntent.putExtra("number", incomingNumber);
                        if (oldState != state) {
                            if (state == TelephonyManager.CALL_STATE_RINGING) {
                                oldState = state;
                                callIntent.putExtra("state", "extra_state_ringing");
                            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                                oldState = state;
                                callIntent.putExtra("state", "extra_state_offhook");
                                if (!recordstarted) startRecord();

                            } else if (state == TelephonyManager.CALL_STATE_IDLE && oldState == TelephonyManager.CALL_STATE_OFFHOOK) {
                                oldState = state;
                                callIntent.putExtra("state", "extra_state_idle");
                                if (recordstarted) {
                                    recordstarted = false;
                                    recorder.stop();
                                    recorder.reset();
                                    recorder.release();
                                    recorder = null;
                                }
                                Intent resumeActivityIntent = new Intent(getApplicationContext(),MainActivity.class);
                                resumeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(resumeActivityIntent);
                                context.startService(callIntent);
                                HeadlessJsTaskService.acquireWakeLockNow(context);
                            }
                        }
                    }
                };
                telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        };
        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(callStateReceiver);
        callStateReceiver = null;
        if (phoneStateListener != null)
            telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        stopService(new Intent(this, CallEventService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call service")
                .setContentText("Running...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        startForeground(SERVICE_NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    private void startRecord() {
        File sampleDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Recordings");
        if (!sampleDir.exists()) {
            sampleDir.mkdir();
        }
        File audioFile = new File(Environment.getExternalStorageDirectory() + "/Recordings/Record");
        if (!audioFile.exists()) {
            try {
                audioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setAudioEncodingBitRate(32 * 44100);
        recorder.setAudioSamplingRate(44100);

        recorder.setOutputFile(audioFile.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
        recordstarted = true;
    }
}