package com.thonkang.flashcardcreator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.ImageButton;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton createButton = (ImageButton) findViewById(R.id.createButton);
        ImageButton viewButton = (ImageButton) findViewById(R.id.viewButton);
        ImageButton helpButton = (ImageButton) findViewById(R.id.helpButton);

        createButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(HomeActivity.this, CardSelector.class);
                        startActivity(intent);
                    }
                }
        );

        viewButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v)
                    {

                    }
                }
        );

        helpButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(HomeActivity.this, HelpActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }
}
