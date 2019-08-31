package com.example.safi.videosensordatasync;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;



/**
 * Created by safi on 7/25/17.
 */

public class TimedDataRecording {


    private static final Object SENSOR_SERVICE = 1;
    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();



    static double longitude=-1;
    static double latitude=-1;

    static long timeOfVid=0;

    String[] datapoints;

    GPSTracker gps;

    SensorManager sensorManager;


    SensorData SD;


    LocationListener locationListener;
    LocationManager LM;


    File file;

    Uri photoURI;

    Context context;

    FilesSyncToFirebase filesSyncToFirebase;

    static long time = 0;

    TimedDataRecording(Context con, File FileToWrite, SensorManager sm) {


        file = FileToWrite;


        filesSyncToFirebase = new FilesSyncToFirebase();

        sensorManager = sm;


        context = con;



        filesSyncToFirebase.SetDirName("test");

    }





    public void SetVidUri(Uri uri) {


        photoURI = uri;

    }

    public void stopTimer() {
        if (mTimer1 != null) {
            mTimer1.cancel();
            mTimer1.purge();

            filesSyncToFirebase.SetPhotoUri(photoURI);
            filesSyncToFirebase.StartSync();


        }
    }

    public void startTimer(final Chronometer chronometer) {
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run() {


                        gps = new GPSTracker(context);

                        if (gps.canGetLocation()) {

                            double lat = gps.getLatitude();
                            double longi = gps.getLongitude();


                            SD = new SensorData(sensorManager);


                            datapoints = new String[9];

                            datapoints[0]=chronometer.getText().toString();

                            if (latitude != -1 && longitude != -1) {

                                datapoints[1] = "" + latitude;
                                datapoints[2] = "" + longitude;

                            } else {
                                datapoints[1] = "" + lat;
                                datapoints[2] = "" + longi;
                            }


                            datapoints[3] = "" + SensorData.gyro_x;
                            datapoints[4] = "" + SensorData.gyro_y;
                            datapoints[5] = "" + SensorData.gyro_z;

                            datapoints[6] = "" + SensorData.linear_acc_x;
                            datapoints[7] = "" + SensorData.linear_acc_y;
                            datapoints[8] = "" + SensorData.linear_acc_z;


                            boolean UpdateFile = false;


                            int count=datapoints.length;

                            for (int i = 0; i < datapoints.length; i++) {

                                if (datapoints[i] == "-1") {

                                    count--;


                                }


                            }



                            if(count==0){
                                UpdateFile = true;
                                count=datapoints.length;

                            }


                            UpdateFile = true;

                            if(UpdateFile) {

                                try {
                                    filesSyncToFirebase.StoreData(datapoints, file);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                Toast.makeText(context, "Your Location is - \nLat: " + latitude + "\nLong: " + longitude
                                        + "\nGyro:" + SensorData.linear_acc_x+ " " + SensorData.linear_acc_y + " " + SensorData.linear_acc_z, Toast.LENGTH_LONG).show();




                            }
                            else{

                                Toast.makeText(context, "Working",Toast.LENGTH_LONG);
                            }

                            UpdateFile=false;



                        }





                        else{

                            gps.showSettingsAlert();
                        }



                    }
                });


            }

        };

        mTimer1.schedule(mTt1, 1, 1000);


    }
}






