package com.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;
import com.meetup.db.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String RSVP_DEBUG = "RSVP_DEBUG";

    private AppDatabase db;
    private EventEntity event;
    private String userInterests = "";

    private TextView titleText;
    private TextView descriptionText;
    private TextView cityText;
    private TextView dateText;
    private TextView timeText;
    private TextView locationText;
    private TextView capacityText;
    private TextView matchingTagsText;
    private TextView otherTagsText;
    private TextView createdByText;
    private TextView rsvpStatusText;

    private Button rsvpButton;
    private Button addToCalendarButton;

    private MaterialCardView tagsCard;
    private LinearLayout locationRow;
    private LinearLayout capacityRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isGuest = getIntent().getBooleanExtra(LoginActivity.EXTRA_IS_GUEST, false);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        SystemUiHelper.applyMeetUpSystemBars(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eventDetailsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        db = AppDatabase.getInstance(this);

        UserEntity currentUser = db.userDao().getCurrentUser();
        if (currentUser != null && currentUser.interests != null) {
            userInterests = currentUser.interests;
        }

        int eventId = getIntent().getIntExtra("event_id", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(RSVP_DEBUG, "Opening EventDetailsActivity for event ID: " + eventId);

        event = db.eventDao().getEventById(eventId);

        if (event == null) {
            Log.e(RSVP_DEBUG, "Event not found for event ID: " + eventId);
            showEventNotFound();
            return;
        }

        populateEventDetails();
        updateRsvpUi();

        addToCalendarButton.setOnClickListener(v -> addEventToCalendar());

        if (isGuest) {
            rsvpButton.setEnabled(false);
            rsvpStatusText.setText(R.string.sign_in_to_rsvp);
        } else {
            rsvpButton.setOnClickListener(v -> handleRsvpClick());
        }
    }

    private void initViews() {
        titleText = findViewById(R.id.detailsTitleText);
        descriptionText = findViewById(R.id.detailsDescriptionText);
        cityText = findViewById(R.id.detailsCityText);
        dateText = findViewById(R.id.detailsDateText);
        timeText = findViewById(R.id.detailsTimeText);
        locationText = findViewById(R.id.detailsLocationText);
        capacityText = findViewById(R.id.detailsCapacityText);
        matchingTagsText = findViewById(R.id.detailsMatchingTagsText);
        otherTagsText = findViewById(R.id.detailsOtherTagsText);
        createdByText = findViewById(R.id.detailsCreatedByText);
        rsvpStatusText = findViewById(R.id.rsvpStatusText);
        rsvpButton = findViewById(R.id.rsvpButton);
        addToCalendarButton = findViewById(R.id.addToCalendarButton);

        tagsCard = findViewById(R.id.tagsCard);
        locationRow = findViewById(R.id.locationRow);
        capacityRow = findViewById(R.id.capacityRow);
    }

    private void handleRsvpClick() {
        if (event == null) {
            Toast.makeText(this, "Event unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newStatus = !event.isRsvped;

        try {
            db.eventDao().updateRsvpStatus(event.id, newStatus);
            event.isRsvped = newStatus;
            updateRsvpUi();

            Toast.makeText(
                    this,
                    newStatus ? R.string.rsvp_joined_toast : R.string.rsvp_cancelled_toast,
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {
            Log.e(RSVP_DEBUG, "Error updating RSVP", e);
            Toast.makeText(this, "Failed to update RSVP", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateEventDetails() {
        titleText.setText(event.title);
        descriptionText.setText(event.description);
        cityText.setText(event.city);
        dateText.setText(event.date);

        if (event.time != null && !event.time.trim().isEmpty()) {
            timeText.setText(event.time);
        } else {
            timeText.setText(R.string.time_tbd);
        }

        if (event.location != null && !event.location.trim().isEmpty()) {
            locationText.setText(event.location);
            locationRow.setVisibility(View.VISIBLE);
        } else {
            locationRow.setVisibility(View.GONE);
        }

        if (event.maxAttendees > 0) {
            capacityText.setText(getResources().getQuantityString(
                    R.plurals.spots_count,
                    event.maxAttendees,
                    event.maxAttendees
            ));
        } else {
            capacityText.setText(R.string.unlimited_spots);
        }

        capacityRow.setVisibility(View.VISIBLE);

        populateTags();
        createdByText.setText(getString(R.string.created_by_format, "Event Organizer"));
    }

    private void populateTags() {
        if (event.tags == null || event.tags.trim().isEmpty()) {
            tagsCard.setVisibility(View.GONE);
            return;
        }

        tagsCard.setVisibility(View.VISIBLE);

        String matchingTags = getMatchingTags(event, userInterests);
        String nonMatchingTags = getNonMatchingTags(event, userInterests);

        if (!matchingTags.isEmpty()) {
            matchingTagsText.setText(getString(R.string.tags_matching_prefix, matchingTags));
            matchingTagsText.setTextColor(ContextCompat.getColor(this, R.color.accent_orange));
            matchingTagsText.setVisibility(View.VISIBLE);

            if (!nonMatchingTags.isEmpty()) {
                otherTagsText.setText(getString(R.string.tags_other_prefix, nonMatchingTags));
                otherTagsText.setVisibility(View.VISIBLE);
            } else {
                otherTagsText.setVisibility(View.GONE);
            }
        } else {
            matchingTagsText.setVisibility(View.GONE);
            otherTagsText.setText(event.tags);
            otherTagsText.setVisibility(View.VISIBLE);
        }
    }

    private String getMatchingTags(EventEntity event, String userInterests) {
        if (event == null || event.tags == null || event.tags.trim().isEmpty()) {
            return "";
        }

        if (userInterests == null || userInterests.trim().isEmpty()) {
            return "";
        }

        String[] eventTags = event.tags.split(",");
        String[] interests = userInterests.split(",");
        List<String> matches = new ArrayList<>();

        for (String tag : eventTags) {
            String cleanTag = tag.trim();
            for (String interest : interests) {
                if (cleanTag.equalsIgnoreCase(interest.trim())) {
                    matches.add(cleanTag);
                    break;
                }
            }
        }

        return String.join(", ", matches);
    }

    private String getNonMatchingTags(EventEntity event, String userInterests) {
        if (event == null || event.tags == null || event.tags.trim().isEmpty()) {
            return "";
        }

        if (userInterests == null || userInterests.trim().isEmpty()) {
            return event.tags;
        }

        String[] eventTags = event.tags.split(",");
        String[] interests = userInterests.split(",");
        List<String> nonMatches = new ArrayList<>();

        for (String tag : eventTags) {
            String cleanTag = tag.trim();
            boolean matchFound = false;

            for (String interest : interests) {
                if (cleanTag.equalsIgnoreCase(interest.trim())) {
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound) {
                nonMatches.add(cleanTag);
            }
        }

        return String.join(", ", nonMatches);
    }

    private void addEventToCalendar() {
        if (event == null) {
            Toast.makeText(this, "Event unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, event.title)
                    .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                    .putExtra(
                            CalendarContract.Events.EVENT_LOCATION,
                            (event.location != null && !event.location.trim().isEmpty())
                                    ? event.location + ", " + event.city
                                    : event.city
                    );

            startActivity(intent);

        } catch (Exception e) {
            Log.e(RSVP_DEBUG, "No calendar app available", e);
            Toast.makeText(this, "No calendar app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEventNotFound() {
        titleText.setText(R.string.event_not_found);
        descriptionText.setText("");
        cityText.setText("");
        dateText.setText("");
        timeText.setText("");
        locationRow.setVisibility(View.GONE);
        capacityRow.setVisibility(View.GONE);
        tagsCard.setVisibility(View.GONE);
        createdByText.setVisibility(View.GONE);
        rsvpStatusText.setText(R.string.event_load_error);
        rsvpButton.setText(R.string.unavailable);
        rsvpButton.setEnabled(false);
        addToCalendarButton.setVisibility(View.GONE);
    }

    private void updateRsvpUi() {
        if (event != null && event.isRsvped) {
            rsvpStatusText.setText(R.string.status_rsvpd);
            rsvpButton.setText(R.string.cancel_rsvp);
        } else {
            rsvpStatusText.setText(R.string.status_not_rsvpd);
            rsvpButton.setText(R.string.rsvp_now);
        }
    }
}
