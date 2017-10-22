package com.thonkang.flashcardcreator;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CardSelector extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    TextView sideText;
    ImageButton nextButton;
    ImageButton frontButton;
    ImageButton doneButton;

    boolean isFront = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selector);

        sideText = (TextView) findViewById(R.id.sideText);

        nextButton = (ImageButton) findViewById(R.id.nextButton);
        doneButton = (ImageButton) findViewById(R.id.doneButton);
        frontButton = (ImageButton) findViewById(R.id.frontButton);

        doneButton.setVisibility(View.INVISIBLE);
        frontButton.setVisibility(View.INVISIBLE);

        // Go back to home screen if no camera enabled
        if(!hasCamera()) {
            finish();
        }

        launchCamera();

        ListView cardOptions = (ListView) findViewById(R.id.cardOptions);
        List cardList = new ArrayList<String>();

        cardList.add("test1");
        cardList.add("test2");

        ArrayAdapter adapter = new ArrayAdapter<String>(CardSelector.this,
                android.R.layout.simple_list_item_1, cardList);

        cardOptions.setAdapter(adapter);
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // To get the image taken from the camera -> onActivityResult
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check that an image was actually taken
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            //capturedImage.setImageBitmap(image);
        } else {
            if(isFront == true) {
                finish();
            } else {
                goBackToFront();
            }
        }
    }

    public void goBackToFront()
    {
        sideText.setText("Front Side");
        doneButton.setVisibility(View.INVISIBLE);
        frontButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.VISIBLE);

        isFront = true;
    }

    public void onNextClick(View view) {
        sideText.setText("Back Side");
        doneButton.setVisibility(View.VISIBLE);
        frontButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.INVISIBLE);

        isFront = false;

        launchCamera();
    }

    public void onFrontClick(View view) {
        goBackToFront();
    }

    public void onAddMoreClick(View view) {

    }

    public void onDoneClick(View view) {

    }
}
