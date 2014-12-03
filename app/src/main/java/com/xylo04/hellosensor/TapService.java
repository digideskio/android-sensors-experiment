package com.xylo04.hellosensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TapService extends Service implements SensorEventListener {

    private static final String TAG = TapService.class.getSimpleName();

    private SensorManager sensorManager;

    private Vibrator vibrator;

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) == null) {
            Log.w(TAG, "There is no accelerometer on this device");
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "There is no vibrator on this device");
        }

        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        registerAccelerometer();
    }

    private void registerAccelerometer() {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            Log.d(TAG, "Registered accelerometer");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float xAccel = sensorEvent.values[0];
        float yAccel = sensorEvent.values[1];
        float zAccel = sensorEvent.values[2];
        double magnitude = Math
                .sqrt(Math.pow(xAccel, 2) + Math.pow(yAccel, 2) + Math.pow(zAccel, 2));
        if (magnitude > 3.5) {
            Log.d(TAG, String.format("Tap! Mag %f", magnitude));
            unregisterAccelerometer();
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(100L);
            }
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    registerAccelerometer();
                }
            }, 200, TimeUnit.MILLISECONDS);
        }
    }

    private void unregisterAccelerometer() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Unregistered accelerometer");
    }

}