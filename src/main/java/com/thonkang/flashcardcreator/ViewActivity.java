package com.thonkang.flashcardcreator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        ListView cardsView = (ListView) findViewById(R.id.cardsView);

        List<String> cards = new ArrayList<>();

        ArrayAdapter adapter = new ArrayAdapter<String>(ViewActivity.this,
                android.R.layout.simple_list_item_1, cards);

        for(List<String> card : globals.deck) {
            cards.add("Front: \t" + card.get(0) + "\nBack: \t" + card.get(1));
        }

        cardsView.setAdapter(adapter);
    }

    public void onHomeClick(View view) {
        Intent intent = new Intent(ViewActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}
