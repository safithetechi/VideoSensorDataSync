package com.example.safi.videosensordatasync;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

/**
 * Created by safi on 7/21/17.
 */

public class FilesSyncToFirebase {

    String [] DataPoints;
    String DirName;

    Uri photoUri;

    Uri File;


    boolean StoreFile;

    boolean StoreImage;

    private StorageReference mStorageRef;




    FilesSyncToFirebase(){




        mStorageRef = FirebaseStorage.getInstance().getReference();





    }


    public void StartSync(){





            StorageReference Data = mStorageRef.child(DirName+"/file.csv");


            DataSync(Data,File);

            DataSync(mStorageRef.child(DirName+"/vid.mp4"),photoUri);






    }



    private void  DataSync(StorageReference Data,Uri file) {

        Data.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.v("********************","*****************THEN WHY");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...

                            Log.v("********************","*****************THIS SUCKS");

                        }
                    });


        }

    public void  SetDirName(String name){

        DirName=name;


    }

    public void SetStoreFile(boolean t){

        StoreFile=t;
    }



    public void SetStoreImage(boolean t){

        StoreFile=t;
    }






    public void SetPhotoUri(Uri uri){
        photoUri=uri;
    }

    public Uri StoreData(String[] dataPoints,File file) throws IOException {


        String filePath=file.getAbsolutePath();


        FileOutputStream fileinput = new FileOutputStream(file,true);

        PrintStream printstream = new PrintStream(fileinput);



        for(int i=0;i<dataPoints.length;i++) {


            printstream.print(dataPoints[i]+",");


        }

        printstream.print("\n");




        

        fileinput.close();




        return (File=Uri.fromFile(new File(filePath)));


    }





}
