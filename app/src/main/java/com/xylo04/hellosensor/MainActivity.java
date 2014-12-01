package com.xylo04.hellosensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SensorManager sensorManager;
    private Vibrator vibrator;
    private ScheduledExecutorService scheduledExecutorService;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = (TextView) findViewById(R.id.status_text);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) == null) {
            Log.w(TAG, "There is no accelerometer on this device");
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "There is no vibrator on this device");
        }

        scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        double magnitude = Math.sqrt(Math.pow(xAccel, 2) + Math.pow(yAccel, 2) + Math.pow(zAccel, 2));
        statusText.setText(String.format(
                "x: %f\ny:%f\nz: %f\n|a| %f",
                xAccel,
                yAccel,
                zAccel,
                magnitude));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterAccelerometer();
    }

    private void unregisterAccelerometer() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Unregistered accelerometer");
    }
}
