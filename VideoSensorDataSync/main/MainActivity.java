package com.example.safi.videosensordatasync;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;




public class MainActivity extends AppCompatActivity {


    private static final String TAG ="Acha" ;
    private Camera mCamera;
    private CameraPreview mPreview;

    private SensorManager sensorManager;

    TimedDataRecording timedDataRecording;

    GPSTracker gps;

    Camera.Parameters params;

    private MediaRecorder mMediaRecorder;


    File DataFile;

    private boolean isRecording = false;


    Chronometer chrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MarshMelloPermission marshMelloPermission =new MarshMelloPermission(this);

        if(!marshMelloPermission.checkPermissionForRecord()){

            marshMelloPermission.checkPermissionForRecord();

        }

        if(!marshMelloPermission.checkPermissionForCamera()){

            marshMelloPermission.checkPermissionForCamera();

        }

        if(!marshMelloPermission.checkPermissionForExternalStorage()){

            marshMelloPermission.checkPermissionForExternalStorage();

        }

        if(!marshMelloPermission.checkPermissionForFineLocation()){

            marshMelloPermission.checkPermissionForFineLocation();

        }





        chrono = (Chronometer) this.findViewById(R.id.chrono);



        final long clock=SystemClock.elapsedRealtime();

        final int[] i = {0};


        try {
          //  FrameLayout preview = (FrameLayout) findViewById(R.id.CameraPreview);

        //    preview.addView(mPreview);
            mCamera=getCameraInstance();


            mPreview= new CameraPreview(MainActivity.this,mCamera);






        } catch (Exception e) {
            e.printStackTrace();
        }



        FrameLayout preview = (FrameLayout) findViewById(R.id.CameraPreview);

        preview.addView(mPreview);


        sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);








        preview.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if (isRecording) {
                            // stop recording and release camera
                            mMediaRecorder.stop();  // stop the recording
                           releaseMediaRecorder(); // release the MediaRecorder object
                            mCamera.lock();         // take camera access back from MediaRecorder


                            chrono.stop();

                             timedDataRecording.stopTimer();

                            // inform the user that recording has stopped

                            isRecording = false;
                        } else {
                            // initialize video camera
                            if (prepareVideoRecorder()) {
                                // Camera is available and unlocked, MediaRecorder is prepared,
                                // now you can start recording
                                mMediaRecorder.start();

//                                  chrono.setBase(clock);
                                  chrono.start();


                                File file=null;

                                try {
                                    file=createFile();
                                } catch (IOException e) {
                                    e.printStackTrace();

                                }


                               timedDataRecording= new TimedDataRecording(MainActivity.this,file,sensorManager);


                                timedDataRecording.SetVidUri(getOutputMediaFileUri(MEDIA_TYPE_VIDEO));

                                timedDataRecording.startTimer(chrono);




                                // inform the user that recording has started

                                isRecording = true;
                            } else {
                                // prepare didn't work, release the camera
                                releaseMediaRecorder();
                                // inform user


                            }



                        }




                    }
                }
        );








    }




    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first


        // Get the Camera instance as the activity achieves full user focus
        if (mCamera == null) {

            mCamera=getCameraInstance();

        }

        if(mMediaRecorder==null){

            prepareVideoRecorder();

        }

    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)

            Toast.makeText(getApplicationContext(),"Camera is not available (in use or does not exist)",Toast.LENGTH_SHORT);
        }
        return c; // returns null if camera is unavailable
    }




    private File createFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String FileName = "CSV_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.getExternalStorageState());



        File file = File.createTempFile(
               FileName,  /* prefix */
                ".csv",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return file;
    }




    private boolean prepareVideoRecorder(){

           mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
          mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
          mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
          mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }





    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }



    private  Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = getExternalFilesDir(Environment.getExternalStorageState());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.



        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


}

