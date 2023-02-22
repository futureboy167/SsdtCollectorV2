package com.example.sscollectorv2;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sscollectorv2.databinding.ActivityMainBinding;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    private Button btRecord;
    private Spinner spinnerAct;
    private EditText repetition;
    private MediaRecorder recorder = null;

    private String[] actions;
    private String action, newName;
    private String ax, ay, az, gx, gy, gz;
    private String filePath, fileName, audioFileName;
    private FileWriter writer;

    private boolean record = false;
    private boolean flagA = false;
    private boolean flagG = false;

    @Override
//    Run on creating app
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        btRecord = findViewById(R.id.btRecord);
        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dataCollector(arg0);
            }
        });

        repetition = findViewById(R.id.rep);

        filePath = Environment.getExternalStorageDirectory() + "/SensorData";
        File file = new File(Environment.getExternalStorageDirectory()
                + "/SensorData");

        if (!file.exists())
            file.mkdirs();

        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            flagA = true;
            ax = String.format(Locale.ROOT,"%.4f", event.values[0]);
            ay = String.format(Locale.ROOT,"%.4f", event.values[1]);
            az = String.format(Locale.ROOT,"%.4f", event.values[2]);
        }

        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            flagG = true;
            gx = String.format(Locale.ROOT,"%.4f", event.values[0]);
            gy = String.format(Locale.ROOT,"%.4f", event.values[1]);
            gz = String.format(Locale.ROOT,"%.4f", event.values[2]);
        }

        if (flagA && flagG) {
            String date = String.valueOf(System.currentTimeMillis());
            date +=  "," + ax + "," + ay + "," + az + "," + gx + "," + gy + "," + gz + "\n";

            if (!date.isEmpty() && record)
                dataReader(date);

            flagA = false;
            flagG = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startSensor(){

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, 10000);
            Log.d(TAG, "Registered accelerometer listener");
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, 10000);
            Log.d(TAG, "Registered gyroscope listener");
        }
    }
    public void stopSensor(){
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Stop sensor");
    }

    public void dataCollector(View arg0) {
        if (record == false) {
            newName = repetition.getText().toString().trim();
            if (newName.equals("")){
                Toast.makeText(getApplicationContext(), "Please input file name",
                        Toast.LENGTH_SHORT).show();
                return;
                }

            audioFileName = Environment.getExternalStorageDirectory() + "/SensorData";
            Toast.makeText(getApplicationContext(), "Recording",
                    Toast.LENGTH_SHORT).show();

            Date date = new Date(System.currentTimeMillis());
            String timeMilli = "" + date.getTime();
            record = true;
            startRecording();

            fileName = timeMilli + "_" + newName + ".csv";
            audioFileName += "/"+timeMilli + "_" + newName+".3gp";
            try {
                writer = new FileWriter(new File(filePath, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            btRecord.setText("Stop");
        }

        else {
            record = false;
            repetition.setEnabled(true);
            btRecord.setText("Start");
            stopRecording();
            Toast.makeText(getApplicationContext(), "Saving",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void dataReader(String data) {
        try {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();

        } catch (IOException e) {
            Log.e("start", "prepare() failed");
        }
        startSensor();
        recorder.start();
    }

    private void stopRecording() {
        stopSensor();
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}