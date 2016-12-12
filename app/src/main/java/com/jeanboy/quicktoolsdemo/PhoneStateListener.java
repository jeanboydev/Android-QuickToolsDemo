package com.jeanboy.quicktoolsdemo;

/**
 * Created by jeanboy on 2016/10/25.
 */

public interface PhoneStateListener {

    void onToast(String msg);

    void onWifiChange(boolean isOn);

    void onGPSChange(boolean isOn);

    void onMobileDataChange(boolean isOn);

    void onBluetoothChange(boolean isOn);

    void onRotationChange(boolean isOn);

    void onAirplaneModeChange(boolean isOn);

    void onSyncSwitchChange(boolean isOn);

    void onRingModeChange(int mode);

    void onAudioVolumeChange(int value);

    void onScreenBrightnessChange(int value);
}
