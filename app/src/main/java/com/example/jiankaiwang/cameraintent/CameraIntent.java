package com.example.jiankaiwang.cameraintent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraIntent extends AppCompatActivity {

    // the image view for taken photo
    protected ImageView imgCapture;
    protected TextView result;

    protected static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;

    protected static int switchModel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_intent);

        // initialize
        result = (TextView) findViewById(R.id.result);

        // get the image view object
        imgCapture = (ImageView) findViewById(R.id.photoview);
        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(switchModel) {
                    case 0:
                        // open the default camera
                        open_camera();
                        break;
                    case 1:
                        // open the external camera app and
                        // save the image taken from the app to another directory
                        open_camera_and_save_image();
                        break;
                }
            }
        });
    }

    protected void open_camera() {
        // use default camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there is a camera activity to handle the intent.
        if(intent.resolveActivity(getPackageManager()) != null) {
            // pass to another external activity
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected void load_thumbnail(int requestCode, int resultCode, Intent data) {
        try {
            if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                /* Here we show the thumbnail on the screen. */
                Bundle extra = data.getExtras();
                Bitmap photo = (Bitmap) extra.get("data");
                imgCapture.setImageBitmap(photo);
                result.setText(R.string.success_result);
                Log.d("thumbnail width", String.valueOf(photo.getWidth()));
                Log.d("thumbnail height", String.valueOf(photo.getHeight()));
            } else {
                result.setText(R.string.fail_request_result);
            }
        } catch (Exception e) {
            result.setText(R.string.fail_result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(switchModel) {
            case 0:
                // show the thumbnail
                load_thumbnail(requestCode, resultCode, data);
                break;
            case 1:
                // show full size image and add to the gallery
                setPicFromLocalFile(resultCode);
                galleryAddPic(resultCode);
                break;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // save a file, path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    protected void open_camera_and_save_image() {
        /* The example opening the camera and save the taken photo.
        * */

        // use default camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there is a camera activity to handle the intent.
        if(intent.resolveActivity(getPackageManager()) != null) {
            // create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // error occurred while creating the file
                result.setText(R.string.fail_in_creating_photo);
            }

            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.jiankaiwang.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // pass to another external activity
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void galleryAddPic(int resultCode) {
        if(resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    private void setPicFromLocalFile(int resultCode) {
        if(resultCode == RESULT_OK) {
            // Get the dimensions of the View
            int targetW = imgCapture.getWidth();
            int targetH = imgCapture.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            imgCapture.setImageBitmap(bitmap);

            result.setText(R.string.success_result);
        } else {
            result.setText(R.string.fail_request_result);
        }
    }

}
