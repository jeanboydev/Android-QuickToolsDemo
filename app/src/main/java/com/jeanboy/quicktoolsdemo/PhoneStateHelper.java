package com.jeanboy.quicktoolsdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;


/**
 * Created by jeanboy on 2016/10/25.
 */

public class PhoneStateHelper implements PhoneSettingsManager.MessageListener {

    private final static String TAG = PhoneStateHelper.class.getSimpleName();

    public static final String ACTION_SYNC_CONN_STATUS_CHANGED = "com.android.sync.SYNC_CONN_STATUS_CHANGED";
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    private static PhoneSettingsManager phoneSettingsManager;

    private PhoneStateListener phoneStateListener;

    private static PhoneStateReceiver phoneStateReceiver;
    private IntentFilter intentFilter;

    public PhoneStateHelper(PhoneStateListener phoneStateListener) {
        this.phoneStateListener = phoneStateListener;
        phoneSettingsManager = new PhoneSettingsManager(this);
        phoneStateReceiver = new PhoneStateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.setPriority(1000);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//Wifi
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);//飞行模式
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);//GPS
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//移动数据
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//移动数据
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙
        intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);//响铃
        intentFilter.addAction(ACTION_SYNC_CONN_STATUS_CHANGED);//同步
        intentFilter.addAction(VOLUME_CHANGED_ACTION);//媒体音量
    }

    @Override
    public void onToast(String msg) {
        if (phoneStateListener != null) {
            phoneStateListener.onToast(msg);
        }
    }

    public PhoneSettingsManager getManager() {
        return phoneSettingsManager;
    }

    private Context context;

    public void register(Context context) {
        this.context = context.getApplicationContext();
        this.context.registerReceiver(phoneStateReceiver, intentFilter);

        this.context.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                true, brightnessObserver);//屏幕亮度

        this.context.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                true, rotationObserver);//屏幕旋转
    }

    public void unregister() {
        if (this.context == null) return;
        context.unregisterReceiver(phoneStateReceiver);
        context.getContentResolver().unregisterContentObserver(brightnessObserver);
        context.getContentResolver().unregisterContentObserver(rotationObserver);
        phoneStateReceiver = null;
        brightnessObserver = null;
        rotationObserver = null;
        this.context = null;
    }

    ContentObserver brightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (phoneStateListener != null) {
                phoneStateListener.onScreenBrightnessChange(phoneSettingsManager.getScreenBrightness(context));
            }
        }
    };

    ContentObserver rotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (phoneStateListener != null) {
                phoneStateListener.onRotationChange(phoneSettingsManager.isRotation(context));
            }
        }
    };


    public class PhoneStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (phoneStateListener == null) return;
            String message = "onReceive";
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//wifi开关状态
                message = "wifi开关";
                phoneStateListener.onWifiChange(phoneSettingsManager.isWifiOpen(context));
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {//飞行模式
                message = "飞行模式";
                phoneStateListener.onAirplaneModeChange(phoneSettingsManager.isAirplaneModeOn(context));
            } else if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {//GPS
                message = "GPS";
                phoneStateListener.onGPSChange(phoneSettingsManager.isGPSOpen(context));
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {//移动数据
                message = "移动数据NETWORK_STATE_CHANGED_ACTION";
                phoneStateListener.onMobileDataChange(phoneSettingsManager.isMobileDataOpen(context));

            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {//移动数据
                message = "移动数据CONNECTIVITY_ACTION";
                phoneStateListener.onMobileDataChange(phoneSettingsManager.isMobileDataOpen(context));

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {//蓝牙
                message = "蓝牙";
                phoneStateListener.onBluetoothChange(phoneSettingsManager.isBluetoothOpen());
            } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(intent.getAction())) {//响铃
                message = "响铃";
                phoneStateListener.onRingModeChange(phoneSettingsManager.getRingModeStatus(context));
            } else if (PhoneStateHelper.ACTION_SYNC_CONN_STATUS_CHANGED.equals(intent.getAction())) {//同步
                message = "同步";
                phoneStateListener.onSyncSwitchChange(phoneSettingsManager.isSyncSwitchOn(context));
            } else if (PhoneStateHelper.VOLUME_CHANGED_ACTION.equals(intent.getAction())) {//媒体音量
                message = "媒体音量";
                phoneStateListener.onAudioVolumeChange(phoneSettingsManager.getAudioNow(context));
            }
            Log.d(TAG, message);
        }
    }
}
