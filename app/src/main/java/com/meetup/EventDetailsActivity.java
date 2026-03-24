package com.meetup;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        TextView titleText = findViewById(R.id.detailsTitleText);
        TextView descriptionText = findViewById(R.id.detailsDescriptionText);
        TextView cityText = findViewById(R.id.detailsCityText);
        TextView dateText = findViewById(R.id.detailsDateText);

        String title = getIntent().getStringExtra("event_title");
        String description = getIntent().getStringExtra("event_description");
        String city = getIntent().getStringExtra("event_city");
        String date = getIntent().getStringExtra("event_date");

        titleText.setText(title);
        descriptionText.setText(description);
        cityText.setText("City: " + city);
        dateText.setText("Date: " + date);
    }
}