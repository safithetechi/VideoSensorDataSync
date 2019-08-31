package com.example.safi.videosensordatasync;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by safi on 7/25/17.
 */

public class SensorData implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mLight;

    private Sensor accelerometer;
    private Sensor head;
    private Sensor gyro;

    static  float linear_acc_x = -1;
    static float linear_acc_y = -1;
    static float linear_acc_z = -1;


    static float gyro_x = -1;
    static float gyro_y = -1;
    static float gyro_z = -1;

    SensorData(SensorManager sm) {


        mSensorManager =sm;
        accelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        gyro=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);



    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {



            linear_acc_x = event.values[0];
            linear_acc_y = event.values[1];
            linear_acc_z = event.values[2];



    }


    String[] GetGyro(){

        String[] gy= new String[3];

        gy[0]=""+gyro_x;
        gy[1]=""+gyro_y;
        gy[2]=""+gyro_z;

        return gy;
    }




    String[] GetAccel(){

        String[] gy= new String[3];

        gy[0]=""+linear_acc_x;
        gy[1]=""+linear_acc_y;
        gy[2]=""+linear_acc_z;

        return gy;
    }

}
