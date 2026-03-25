package com.meetup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

public class EventDetailsActivity extends AppCompatActivity {

    private AppDatabase db;
    private EventEntity event;
    private Button rsvpButton;
    private TextView rsvpStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        TextView titleText = findViewById(R.id.detailsTitleText);
        TextView descriptionText = findViewById(R.id.detailsDescriptionText);
        TextView cityText = findViewById(R.id.detailsCityText);
        TextView dateText = findViewById(R.id.detailsDateText);
        rsvpStatusText = findViewById(R.id.rsvpStatusText);
        rsvpButton = findViewById(R.id.rsvpButton);

        db = AppDatabase.getInstance(this);

        int eventId = getIntent().getIntExtra("event_id", -1);
        event = db.eventDao().getEventById(eventId);

        if (event != null) {
            titleText.setText(event.title);
            descriptionText.setText(event.description);
            cityText.setText("City" + event.city);
            dateText.setText("Date" + event.date);

            updateRsvpUi();

            rsvpButton.setOnClickListener(v -> {
                boolean newStatus = !event.isRsvped;
                db.eventDao().updateRsvpStatus(event.id, newStatus);
                event.isRsvped = newStatus;
                updateRsvpUi();
            });
        }else {
            titleText.setText("Event not found!");
            descriptionText.setText("");
            cityText.setText("");
            dateText.setText("");
            rsvpStatusText.setText("");
            rsvpButton.setEnabled(false);
        }
    }

        private void updateRsvpUi() {
        if(event != null && event.isRsvped) {
            rsvpStatusText.setText("you have RSVP'd for this event.");
            rsvpButton.setText("cancel RSVP");
        }else {
            rsvpStatusText.setText("you have not yet RSVP'd for this event");
            rsvpButton.setText("RSVP");
        }
    }
}