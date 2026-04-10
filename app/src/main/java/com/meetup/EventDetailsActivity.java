package com.meetup;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String RSVP_DEBUG = "RSVP_DEBUG";

    private AppDatabase db;
    private EventEntity event;

    private TextView titleText;
    private TextView descriptionText;
    private TextView cityText;
    private TextView dateText;
    private TextView rsvpStatusText;
    private Button rsvpButton;
    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isGuest = getIntent().getBooleanExtra(LoginActivity.EXTRA_IS_GUEST, false);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        SystemUiHelper.applyMeetUpSystemBars(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eventDetailsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleText = findViewById(R.id.detailsTitleText);
        descriptionText = findViewById(R.id.detailsDescriptionText);
        cityText = findViewById(R.id.detailsCityText);
        dateText = findViewById(R.id.detailsDateText);
        rsvpStatusText = findViewById(R.id.rsvpStatusText);
        rsvpButton = findViewById(R.id.rsvpButton);

        db = AppDatabase.getInstance(this);

        int eventId = getIntent().getIntExtra("event_id", -1);
        Log.d(RSVP_DEBUG, "Opening EventDetailsActivity for event ID: " + eventId);

        event = db.eventDao().getEventById(eventId);

        if (event != null) {
            Log.d(RSVP_DEBUG, "Loaded event: " + event.title);

            populateEventDetails();
            updateRsvpUi();

            rsvpButton.setOnClickListener(v -> {
                boolean newStatus = !event.isRsvped;

                // isRsvped represents whether the current user has joined this event
                // Toggle RSVP status for the current user and persist it in the database
                db.eventDao().updateRsvpStatus(event.id, newStatus);
                event.isRsvped = newStatus;

                Log.d(RSVP_DEBUG, "Event ID " + event.id + " RSVP set to: " + newStatus);

                updateRsvpUi();

                Toast.makeText(
                        EventDetailsActivity.this,
                        newStatus ? "You have joined this event." : "You have cancelled your RSVP.",
                        Toast.LENGTH_SHORT
                ).show();
            });

        } else {
            Log.e(RSVP_DEBUG, "Event not found for event ID: " + eventId);
            showEventNotFound();
        }
        if (isGuest) {
            rsvpButton.setEnabled(false);
            rsvpStatusText.setText("Sign in to RSVP for this event.");
        } else {
            updateRsvpUi();

            rsvpButton.setOnClickListener(v -> {
                boolean newStatus = !event.isRsvped;
                db.eventDao().updateRsvpStatus(event.id, newStatus);
                event.isRsvped = newStatus;
                updateRsvpUi();
            });
        }
    }

    private void populateEventDetails() {
        titleText.setText(event.title);
        descriptionText.setText(event.description);
        cityText.setText("City: " + event.city);
        dateText.setText("Date: " + event.date);
    }

    private void showEventNotFound() {
        titleText.setText("Event not found");
        descriptionText.setText("");
        cityText.setText("");
        dateText.setText("");
        rsvpStatusText.setText("This event could not be loaded.");
        rsvpButton.setText("Unavailable");
        rsvpButton.setEnabled(false);
    }

    private void updateRsvpUi() {
        if (event != null && event.isRsvped) {
            rsvpStatusText.setText("Status: You have RSVP’d for this event.");
            rsvpButton.setText("Cancel RSVP");
        } else {
            rsvpStatusText.setText("Status: You have not RSVP’d for this event yet.");
            rsvpButton.setText("RSVP Now");
        }
    }
}
