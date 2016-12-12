package com.jeanboy.quicktoolsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private RadioButton btn_wifi, btn_gps, btn_move_data, btn_ring, btn_flash_light,
            btn_orientation, btn_bluetooth, btn_airport, btn_sync;
    private Button btn_calc, btn_clean_memory;
    private SeekBar btn_screen_light, btn_sound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_wifi = (RadioButton) findViewById(R.id.btn_wifi);
        btn_gps = (RadioButton) findViewById(R.id.btn_gps);
        btn_move_data = (RadioButton) findViewById(R.id.btn_move_data);
        btn_ring = (RadioButton) findViewById(R.id.btn_ring);
        btn_flash_light = (RadioButton) findViewById(R.id.btn_flash_light);
        btn_orientation = (RadioButton) findViewById(R.id.btn_orientation);
        btn_bluetooth = (RadioButton) findViewById(R.id.btn_bluetooth);
        btn_airport = (RadioButton) findViewById(R.id.btn_airport);
        btn_sync = (RadioButton) findViewById(R.id.btn_sync);

        btn_calc = (Button) findViewById(R.id.btn_calc);
        btn_clean_memory = (Button) findViewById(R.id.btn_clean_memory);

        btn_screen_light = (SeekBar) findViewById(R.id.btn_screen_light);
        btn_sound = (SeekBar) findViewById(R.id.btn_sound);

        btn_wifi.setOnClickListener(this);
        btn_gps.setOnClickListener(this);
        btn_move_data.setOnClickListener(this);
        btn_ring.setOnClickListener(this);
        btn_flash_light.setOnClickListener(this);
        btn_orientation.setOnClickListener(this);
        btn_bluetooth.setOnClickListener(this);
        btn_airport.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        btn_calc.setOnClickListener(this);
        btn_clean_memory.setOnClickListener(this);


        phoneStateHelper = new PhoneStateHelper(new PhoneStateListener() {
            @Override
            public void onToast(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWifiChange(boolean isOn) {
                btn_wifi.setChecked(isOn);
            }

            @Override
            public void onGPSChange(boolean isOn) {
                btn_gps.setChecked(isOn);
            }

            @Override
            public void onMobileDataChange(boolean isOn) {
                btn_move_data.setChecked(isOn);
            }

            @Override
            public void onBluetoothChange(boolean isOn) {
                btn_bluetooth.setChecked(isOn);
            }

//            @Override
//            public void onFlashLightChange(boolean isOn) {
//                btn_flash_light.setChecked(isOn);
//            }

            @Override
            public void onRotationChange(boolean isOn) {
                btn_orientation.setChecked(isOn);
            }

            @Override
            public void onAirplaneModeChange(boolean isOn) {
                btn_airport.setChecked(isOn);
            }

            @Override
            public void onSyncSwitchChange(boolean isOn) {
                btn_sync.setChecked(isOn);
            }

            @Override
            public void onRingModeChange(int mode) {

            }

            @Override
            public void onAudioVolumeChange(int value) {
                if (!isSoundMoving) {
                    btn_sound.setProgress(phoneStateHelper.getManager().getAudioNow(MainActivity.this));
                }
            }

            @Override
            public void onScreenBrightnessChange(int value) {
                if (!isScreenMoving) {
                    btn_screen_light.setProgress(phoneStateHelper.getManager().getScreenBrightness(MainActivity.this));
                }
            }
        });
        phoneStateHelper.register(this);

        init();
    }

    private PhoneStateHelper phoneStateHelper;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        phoneStateHelper.unregister();
    }

    private boolean isScreenMoving = false, isSoundMoving = false;

    private int screenMode = 0;

    private void init() {
        btn_wifi.setChecked(phoneStateHelper.getManager().isWifiOpen(this));
        btn_gps.setChecked(phoneStateHelper.getManager().isGPSOpen(this));
        btn_move_data.setChecked(phoneStateHelper.getManager().isMobileDataOpen(this));
        btn_bluetooth.setChecked(phoneStateHelper.getManager().isBluetoothOpen());
        btn_flash_light.setChecked(phoneStateHelper.getManager().isFlashLightOpen(this));
        btn_orientation.setChecked(phoneStateHelper.getManager().isRotation(this));
        btn_airport.setChecked(phoneStateHelper.getManager().isAirplaneModeOn(this));
        btn_sync.setChecked(phoneStateHelper.getManager().isSyncSwitchOn(this));

        btn_screen_light.setMax(phoneStateHelper.getManager().getScreenBrightnessMax());
        btn_screen_light.setProgress(phoneStateHelper.getManager().getScreenBrightness(MainActivity.this));
        btn_sound.setMax(phoneStateHelper.getManager().getAudioMax(MainActivity.this));
        btn_sound.setProgress(phoneStateHelper.getManager().getAudioNow(MainActivity.this));


        btn_screen_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                phoneStateHelper.getManager().changeScreenBrightness(MainActivity.this, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isScreenMoving = true;
                screenMode = phoneStateHelper.getManager().getScreenBrightnessMode(MainActivity.this);
                if (phoneStateHelper.getManager().isAutoBrightness(MainActivity.this)) {
                    phoneStateHelper.getManager().stopAutoBrightness(MainActivity.this);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isScreenMoving = false;
                phoneStateHelper.getManager().updateBrightnessMode(MainActivity.this, screenMode);

            }
        });
        btn_sound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                phoneStateHelper.getManager().changeAudio(MainActivity.this, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSoundMoving = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSoundMoving = false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi:
                phoneStateHelper.getManager().toggleWifi(this);
                break;
            case R.id.btn_gps:
                phoneStateHelper.getManager().toOpenGPS(this);
                break;
            case R.id.btn_move_data:
                phoneStateHelper.getManager().toggleMobileData(this);
                break;
            case R.id.btn_ring:
                phoneStateHelper.getManager().toggleRingMode(this);
                break;
            case R.id.btn_flash_light:
                phoneStateHelper.getManager().toggleFlashLight(this);
                break;
            case R.id.btn_orientation:
                phoneStateHelper.getManager().toggleRotation(this);
                break;
            case R.id.btn_bluetooth:
                phoneStateHelper.getManager().toggleBluetooth();
                break;
            case R.id.btn_airport:
                phoneStateHelper.getManager().toggleAirplaneMode(this);
                break;
            case R.id.btn_sync:
                phoneStateHelper.getManager().toggleSyncSwitch(this);
                break;
            case R.id.btn_calc:
                phoneStateHelper.getManager().toOpenCalc(this);
                break;
            case R.id.btn_clean_memory:

                break;
        }
    }
}
