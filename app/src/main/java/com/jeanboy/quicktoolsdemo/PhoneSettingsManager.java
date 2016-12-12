package com.jeanboy.quicktoolsdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by jeanboy on 2016/10/26.
 */

public class PhoneSettingsManager {

    private final static String TAG = PhoneSettingsManager.class.getSimpleName();

    private final static String MSG_WIFI = "WIFI unavailable!";
    private final static String MSG_GPS = "GPS unavailable!";
    private final static String MSG_MOBILE_DATA = "Mobile Data unavailable!";
    private final static String MSG_SOUND_MODE = "Sound Mode unavailable!";
    private final static String MSG_SOUND_VOLUME = "Sound Volume unavailable!";
    private final static String MSG_BLUETOOTH = "Bluetooth unavailable!";
    private final static String MSG_SYNC = "Sync unavailable!";
    private final static String MSG_SCREEN_BRIGHTNESS = "Screen Brightness unavailable!";
    private final static String MSG_FLASH_LIGHT = "Flash Light unavailable!";
    private final static String MSG_ROTATION = "Rotation unavailable!";
    private final static String MSG_AIRPLANE_MODE = "Airplane Mode unavailable!";

    private final static String MSG_PERMISSION_WRITE_SETTINGS = "WRITE_SETTINGS permission not granted!";
    private final static String MSG_PERMISSION_CAMERA = "CAMERA permission not granted!";
    private final static String MSG_NO_FLASH_LIGHT = "The flash is not supported!";
    private final static String MSG_NO_CALCULATOR = "Calculator not found!";
    private final static String MSG_CALCULATOR_OPEN_FAILED = "Calculator open failed!";


    public interface MessageListener {
        void onToast(String msg);
    }

    private MessageListener messageListener;

    public PhoneSettingsManager(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    private WifiManager wifiManager;//wifi
    private LocationManager locationManager;//GPS
    private ConnectivityManager connectivityManager;//移动数据，同步
    private AudioManager audioManager;//响铃模式，音量调节
    private BluetoothAdapter bluetoothAdapter;//蓝牙
    private ContentResolver contentResolver;//亮度

    //手电筒
    private Camera camera;
    private CameraManager cameraManager;
    private boolean isFlashOpen = false;

    private WifiManager getWifiManager(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager;
    }

    private LocationManager getLocationManager(Context context) {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager;
    }

    private ConnectivityManager getConnectivityManager(Context context) {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return connectivityManager;
    }

    private AudioManager getAudioManager(Context context) {
        if (audioManager == null) {
            audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return bluetoothAdapter;
    }

    private ContentResolver getContentResolver(Context context) {
        if (contentResolver == null) {
            contentResolver = context.getApplicationContext().getContentResolver();
        }
        return contentResolver;
    }

    private CameraManager getCameraManager(Context context) {
        if (cameraManager == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraManager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            }
        }
        return cameraManager;
    }

    private void toast(String msg) {
        if (messageListener != null) {
            messageListener.onToast(msg);
        }
    }

    //wifi
    public boolean toggleWifi(Context context) {
        try {
            wifiManager = getWifiManager(context);
            if (wifiManager.isWifiEnabled()) {
                return wifiManager.setWifiEnabled(false);
            } else {
                return wifiManager.setWifiEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_WIFI);
            return false;
        }
    }

    public boolean isWifiOpen(Context context) {
        try {
            wifiManager = getWifiManager(context);
            return wifiManager.isWifiEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_WIFI);
            return false;
        }
    }

    //GPS
    public void toOpenGPS(Context context) {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isGPSOpen(Context context) {
        try {
            locationManager = getLocationManager(context);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_GPS);
            return false;
        }
    }

    //移动数据
    public void toggleMobileData(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                ComponentName cName = new ComponentName("com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity");
                intent.setComponent(cName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                connectivityManager = getConnectivityManager(context);
                boolean isOpen = isMobileDataOpen(context);
                Method method = connectivityManager.getClass().getMethod("setMobileDataEnabled", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(connectivityManager, !isOpen);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_MOBILE_DATA);
        }
    }

    public boolean isMobileDataOpen(Context context) {
        try {
            connectivityManager = getConnectivityManager(context);
            Class ownerClass = connectivityManager.getClass();
            Method method = ownerClass.getMethod("getMobileDataEnabled");
            Boolean isOpen = (Boolean) method.invoke(connectivityManager);
            return isOpen;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_MOBILE_DATA);
            return false;
        }
    }

    //响铃模式
    public void toggleRingMode(Context context) {
        try {
            audioManager = getAudioManager(context);
            switch (getRingModeStatus(context)) {
                case AudioManager.RINGER_MODE_NORMAL:
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_MODE);
        }

    }

    public int getRingModeStatus(Context context) {
        try {
            audioManager = getAudioManager(context);
            return audioManager.getRingerMode();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_MODE);
            return 0;
        }
    }

    //手电筒
    private boolean isFlashSupported(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void toggleFlashLight(Context context) {
        try {
            if (!isFlashSupported(context)) {
                toast(MSG_NO_FLASH_LIGHT);
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager = getCameraManager(context);
                cameraManager.setTorchMode("0", !isFlashOpen);
                isFlashOpen = !isFlashOpen;
            } else {
                if (camera == null) {
                    camera = Camera.open();
                }
                Camera.Parameters parameters = camera.getParameters();
                if (Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.setPreviewTexture(new SurfaceTexture(0));
                    camera.startPreview();
                }
            }
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
            Log.e(TAG, MSG_FLASH_LIGHT);
            toast(MSG_PERMISSION_CAMERA);
        }
    }

    public boolean isFlashLightOpen(Context context) {
        try {
            if (!isFlashSupported(context)) {
                toast(MSG_NO_FLASH_LIGHT);
                return false;
            }
            if (camera == null) {
                camera = Camera.open();
            }
            Camera.Parameters parameters = camera.getParameters();
            return Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode());
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
            Log.e(TAG, MSG_FLASH_LIGHT);
            return false;
        }
    }

    //屏幕旋转 WRITE_SETTINGS
    public void toggleRotation(Context context) {
        try {
            contentResolver = getContentResolver(context);
            Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, isRotation(context) ? 0 : 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_ROTATION);
        }
    }

    public boolean isRotation(Context context) {
        try {
            contentResolver = getContentResolver(context);
            return Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_ROTATION);
            return false;
        }
    }

    //蓝牙
    public void toggleBluetooth() {
        try {
            bluetoothAdapter = getBluetoothAdapter();
            if (isBluetoothOpen()) {
                bluetoothAdapter.disable();
            } else {
                bluetoothAdapter.enable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_BLUETOOTH);
        }
    }

    public boolean isBluetoothOpen() {
        try {
            bluetoothAdapter = getBluetoothAdapter();
            return BluetoothAdapter.STATE_ON == bluetoothAdapter.getState();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_BLUETOOTH);
            return false;
        }
    }

    //飞行模式 WRITE_SETTINGS
    public void toggleAirplaneMode(Context context) {
        try {
            contentResolver = getContentResolver(context);
            boolean enable = isAirplaneModeOn(context);
            Settings.System.putInt(contentResolver,
                    Settings.System.AIRPLANE_MODE_ON, !enable ? 1 : 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", !enable);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_AIRPLANE_MODE);
        }
    }

    public boolean isAirplaneModeOn(Context context) {
        try {
            contentResolver = getContentResolver(context);
            return Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_AIRPLANE_MODE);
            return false;
        }
    }

    //同步
    public void toggleSyncSwitch(Context context) {
        try {
            ContentResolver.setMasterSyncAutomatically(!isSyncSwitchOn(context));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SYNC);
        }
    }

    public boolean isSyncSwitchOn(Context context) {
        try {
            connectivityManager = getConnectivityManager(context);
            return connectivityManager.getBackgroundDataSetting() && ContentResolver.getMasterSyncAutomatically();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SYNC);
            return false;
        }
    }

    //计算器
    public void toOpenCalc(Context context) {
        try {
            PackageInfo pak = getAllAppList(context, "Calculator", "calculator"); //大小写
            if (pak != null) {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pak.packageName);
                context.startActivity(intent);
            } else {
                toast(MSG_NO_CALCULATOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast(MSG_CALCULATOR_OPEN_FAILED);
        }
    }

    private PackageInfo getAllAppList(Context context, String appFlag1, String appFlag2) {
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = pManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = packageInfoList.get(i);
            if (pak.packageName.contains(appFlag1) || pak.packageName.contains(appFlag2)) {
                return pak;
            }
        }
        return null;
    }

    //亮度调节
    public void changeScreenBrightness(Context context, int value) {
        try {
            contentResolver = getContentResolver(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                toast(MSG_PERMISSION_WRITE_SETTINGS);
                return;
            }
            Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value);
            contentResolver.notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
        }
    }

    public int getScreenBrightness(Context context) {
        try {
            contentResolver = getContentResolver(context);
            return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
            return 0;
        }
    }

    public int getScreenBrightnessMax() {
        return 255;
    }

    public int getScreenBrightnessMode(Context context) {
        try {
            contentResolver = getContentResolver(context);
            return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
            return 0;
        }
    }

    public void updateBrightnessMode(Context context, int mode) {
        try {
            contentResolver = getContentResolver(context);
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
        }
    }

    public boolean isAutoBrightness(Context context) {
        try {
            contentResolver = getContentResolver(context);
            return getScreenBrightnessMode(context) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
            return false;
        }
    }

    public void stopAutoBrightness(Context context) {
        try {
            contentResolver = getContentResolver(context);
            Settings.System.putInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
        }
    }

    public void startAutoBrightness(Context context) {
        try {
            contentResolver = getContentResolver(context);
            Settings.System.putInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SCREEN_BRIGHTNESS);
        }
    }

    //音量调节
    public void changeAudio(Context context, int value) {
        try {
            audioManager = getAudioManager(context);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_VOLUME);
        }
    }

    public int getAudioNow(Context context) {
        try {
            audioManager = getAudioManager(context);
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_VOLUME);
            return 0;
        }
    }

    public int getAudioMax(Context context) {
        try {
            audioManager = getAudioManager(context);
            return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_VOLUME);
            return 0;
        }
    }

    public void setAudioLower(Context context) {
        try {
            audioManager = getAudioManager(context);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_VOLUME);
        }
    }

    public void setAudioRaise(Context context) {
        try {
            audioManager = getAudioManager(context);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, MSG_SOUND_VOLUME);
        }
    }

}