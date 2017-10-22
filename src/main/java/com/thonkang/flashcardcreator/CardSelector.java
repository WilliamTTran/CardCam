package com.thonkang.flashcardcreator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.graphics.Bitmap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import highlighter.Highlighter;
import highlighter.ImageDetector;
import highlighter.PackageManagerUtils;
import highlighter.TextDetector;

public class CardSelector extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB9j8N2OWfLDMYuaqNcEzCy7poaFNEahhk";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private final static int CAMERA_RQ = 6969;
    TextView sideText;
    ImageButton nextButton;
    ImageButton frontButton;
    ImageButton doneButton;
    ImageButton addMoreButton;

    ProgressBar loadingCircle;

    // Create global camera reference in an activity or fragment
    Camera camera;

    Bitmap image;

    boolean isFront = true;

    EditText frontCardText;
    EditText backCardText;

    public static List<List<String>> frontCameraOutput;
    public static List<List<String>> backCameraOutput;
    ListView cardOptions;

    private class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex){
            Log.e("ERROR", "uncaught_exception_handler: uncaught exception in thread " + thread.getName(), ex);

            //hack to rethrow unchecked exceptions
            if(ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            if(ex instanceof Error)
                throw (Error)ex;

            //this should really never happen
            Log.e("ERROR", "uncaught_exception handler: unable to rethrow checked exception");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selector);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        sideText = (TextView) findViewById(R.id.sideText);

        nextButton = (ImageButton) findViewById(R.id.nextButton);
        doneButton = (ImageButton) findViewById(R.id.doneButton);
        frontButton = (ImageButton) findViewById(R.id.frontButton);
        addMoreButton = (ImageButton) findViewById(R.id.addMoreButton);

        frontCardText = (EditText) findViewById(R.id.frontCardText);
        backCardText = (EditText) findViewById(R.id.backCardText);

        loadingCircle = (ProgressBar) findViewById(R.id.progressBar);

        nextButton.setVisibility(View.INVISIBLE);
        doneButton.setVisibility(View.INVISIBLE);
        frontButton.setVisibility(View.INVISIBLE);
        addMoreButton.setVisibility(View.INVISIBLE);

        // Go back to home screen if no camera enabled
        if(!hasCamera()) {
            finish();
        }

        loadingCircle.setVisibility(View.VISIBLE);

        launchCamera();

        cardOptions = (ListView) findViewById(R.id.cardOptions);

        cardOptions.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);
                if(isFront) frontCardText.setText(frontCardText.getText() + "\n" + value);
                else backCardText.setText(backCardText.getText() + "\n" + value);
            }
        });
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void launchCamera() {/*
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.thonkang.flashcardcreator.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // To get the image taken from the camera -> onActivityResult
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }*/
        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int check2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        Log.i("ASD", ""+check + " " + PackageManager.PERMISSION_GRANTED);
        if (check == PackageManager.PERMISSION_GRANTED) {
            //Do something
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
        }
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1231313);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1231311);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1025);

        }
        Log.i("asd", Environment.getExternalStorageDirectory().toString());
        File saveFolder = new File(Environment.getExternalStorageDirectory(), "MaterialCamera Sample");
        if (!saveFolder.mkdirs()){Log.i("help", "cant make dir");}
            //throw new RuntimeException("Unable to create save directory, make sure WRITE_EXTERNAL_STORAGE permission is granted.");

        new MaterialCamera(this)                               // Constructor takes an Activity
                .allowRetry(true)                                  // Whether or not 'Retry' is visible during playback
                .autoSubmit(false)                                 // Whether or not user is allowed to playback videos after recording. This can affect other things, discussed in the next section.
                .saveDir(saveFolder)                               // The folder recorded videos are saved to
                .primaryColorAttr(R.attr.colorPrimary)             // The theme color used for the camera, defaults to colorPrimary of Activity in the constructor
                .showPortraitWarning(true)                         // Whether or not a warning is displayed if the user presses record in portrait orientation
                .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera                 // Allows the user to change cameras.
                .retryExits(false)                                 // If true, the 'Retry' button in the playback screen will exit the camera instead of going back to the recorder
                .restartTimerOnRetry(false)                        // If true, the countdown timer is reset to 0 when the user taps 'Retry' in playback
                .continueTimerInPlayback(false)                    // If true, the countdown timer will continue to go down during playback, rather than pausing.
                .qualityProfile(MaterialCamera.QUALITY_LOW)       // Sets a quality profile, manually setting bit rates or frame rates with other settings will overwrite individual quality profile settings// Sets a preferred aspect ratio for the recorded video output.
                .maxAllowedFileSize(1024 * 1024 * 3)               // Sets a max file size of 5MB, recording will stop if file reaches this limit. Keep in mind, the FAT file system has a file size limit of 4GB.
                .iconRecord(R.drawable.mcam_action_capture)        // Sets a custom icon for the button used to start recording
                .iconStop(R.drawable.mcam_action_stop)             // Sets a custom icon for the button used to stop recording
                .iconFrontCamera(R.drawable.mcam_camera_front)     // Sets a custom icon for the button used to switch to the front camera
                .iconRearCamera(R.drawable.mcam_camera_rear)       // Sets a custom icon for the button used to switch to the rear camera
                .iconPlay(R.drawable.evp_action_play)              // Sets a custom icon used to start playback
                .iconPause(R.drawable.evp_action_pause)            // Sets a custom icon used to pause playback
                .iconRestart(R.drawable.evp_action_restart)        // Sets a custom icon used to restart playback
                .labelRetry(R.string.mcam_retry)                   // Sets a custom button label for the button used to retry recording, when available
                .labelConfirm(R.string.mcam_use_stillshot)             // Sets a custom button label for the button used to confirm/submit a recording
                .audioDisabled(true).stillShot()// Set to true to record video without any audio.
                .start(CAMERA_RQ);// Starts the camera activity, the result will be sent back to the current Activity
        //Looper.loop();
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("asdf", ""+data.toString());

        // Check that an image was actually taken
        // Received recording or error from MaterialCamera
        /*
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.i("asdgg", "hi");
            Bundle extras = data.getExtras();
            image = (Bitmap) extras.get("data");
*/
            //HashMap<String, String> blah = new HashMap<String, String>();
            Log.i("something", "HERE");
            //blah.put("GOOGLE_APPLICATION_CREDENTIALS", "gcred.json");
        /*
            try {
                TextDetector.setEnv(blah);
            } catch(Exception e) {
                e.printStackTrace();
            }
            */
        Log.i("asdASDad", ""+data.getData());


        Uri imageUri = data.getData();
        try {
            Log.i("asdASDad2", ""+data.getData());
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("asdASDad3", ""+image.getWidth());
        image = image.copy(Bitmap.Config.ARGB_8888, true);
        image = scaleDown(image, 800, false);
        FileOutputStream out = null;
        try {
            File saveFolder = new File(Environment.getExternalStorageDirectory(), "MaterialCamera Sample/test.png");
            out = new FileOutputStream(saveFolder);
            image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*
            ImageDetector id = new ImageDetector();
            try {
                Log.i("asdASDad4", ""+data.getData());
                System.out.println(id.detectImages(image, Highlighter.PINK));
                //Log.i("BITMAP THING", id.detectImages(image, Highlighter.PINK).get(0).toString());
            } catch(IOException e) {
                e.printStackTrace();
            }
            */
            //finish();
            /*
        } else {
            if(isFront == true) {
                finish();
            } else {
                goBackToFront();
            }
        }*/
        try {
            Log.i("asdASDad5", ""+data.getData());
            callCloudVision(image, this);
            Log.i("asdASDad6", ""+data.getData());
        } catch(Exception e) {
            e.printStackTrace();
        }

        //finish();
    }

    public void goBackToFront()
    {
        sideText.setText("Front Side");
        doneButton.setVisibility(View.INVISIBLE);
        frontButton.setVisibility(View.INVISIBLE);
        addMoreButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.VISIBLE);

        frontCardText.setVisibility(View.VISIBLE);
        backCardText.setVisibility(View.INVISIBLE);

        isFront = true;
    }

    public void onNextClick(View view) {
        sideText.setText("Back Side");
        doneButton.setVisibility(View.VISIBLE);
        frontButton.setVisibility(View.VISIBLE);
        addMoreButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.INVISIBLE);

        frontCardText.setVisibility(View.INVISIBLE);
        backCardText.setVisibility(View.VISIBLE);

        isFront = false;

        onPostCamera();
    }

    public void onFrontClick(View view) {
        goBackToFront();

        onPostCamera();
    }

    public void onAddMoreClick(View view) {
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(frontCardText.getText().toString());
        temp.add(backCardText.getText().toString());

        globals.deck.add(temp);

        frontCardText.setText("");
        backCardText.setText("");

        loadingCircle.setVisibility(View.VISIBLE);

        goBackToFront();

        launchCamera();
    }

    public void onDoneClick(View view) {
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(frontCardText.getText().toString());
        temp.add(backCardText.getText().toString());

        globals.deck.add(temp);

        Intent intent = new Intent(CardSelector.this, ViewActivity.class);
        startActivity(intent);
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void callCloudVision(final Bitmap image, final Activity activity) throws IOException {
        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = activity.getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(activity.getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<com.google.api.services.vision.v1.model.Feature>() {{
                            com.google.api.services.vision.v1.model.Feature labelDetection = new com.google.api.services.vision.v1.model.Feature();
                            labelDetection.setType("TEXT_DETECTION");
                            //labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d("LMAO", "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    Log.i("RESPONSE", response.toString());
                    List<com.google.api.services.vision.v1.model.AnnotateImageResponse> responses = response.getResponses();
                    Log.i("RESPONSES", responses.toString());

                    for (com.google.api.services.vision.v1.model.AnnotateImageResponse res : responses) {
                        if (res.getError() != null) {
                            Log.i("ERROR", res.getError().getMessage());
                            return null;
                        }
                        Log.i("ANNOTATIONS", res.getTextAnnotations().toString());
                        frontCameraOutput = TextDetector.getHighlightedText(image, res.getTextAnnotations(), Highlighter.PINK);
                        backCameraOutput = TextDetector.getHighlightedText(image, res.getTextAnnotations(), Highlighter.BLUE);

                        return "XD HACKS";
                    }
                } catch (GoogleJsonResponseException e) {
                    Log.d("XD", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("hi", "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                Log.i("PINK", frontCameraOutput.toString());
                Log.i("BLUE", backCameraOutput.toString());

                onPostCamera();
            }
        }.execute();
    }

    public void onPostCamera() {
        List<String> cardList = new ArrayList<String>();

        ArrayAdapter adapter = new ArrayAdapter<String>(CardSelector.this,
                android.R.layout.simple_list_item_1, cardList);

        List<List<String>> output;
        if(isFront) {
            output = frontCameraOutput;
        }
        else {
            output = backCameraOutput;
        }

        for(List<String> chunk : output)
        {
            String toAdd = "";
            for(String line : chunk)
            {
                toAdd += line;
            }
            cardList.add(toAdd);
        }

        cardOptions.setAdapter(adapter);

        loadingCircle.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }


}
