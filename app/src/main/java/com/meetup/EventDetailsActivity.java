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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        db = AppDatabase.getInstance(this);

        UserEntity currentUser = db.userDao().getCurrentUser();
        if (currentUser != null && currentUser.interests != null) {
            userInterests = currentUser.interests;
        }

        int eventId = getIntent().getIntExtra("event_id", -1);
        Log.d(RSVP_DEBUG, "Opening EventDetailsActivity for event ID: " + eventId);

        event = db.eventDao().getEventById(eventId);

        if (event != null) {
            Log.d(RSVP_DEBUG, "Loaded event: " + event.title);

            populateEventDetails();
            updateRsvpUi();

            addToCalendarButton.setOnClickListener(v -> addEventToCalendar());

        } else {
            Log.e(RSVP_DEBUG, "Event not found for event ID: " + eventId);
            showEventNotFound();
        }

        if (isGuest) {
            rsvpButton.setEnabled(false);
            rsvpStatusText.setText(R.string.sign_in_to_rsvp);
        } else {
            updateRsvpUi();

            rsvpButton.setOnClickListener(v -> {
                boolean newStatus = !event.isRsvped;
                db.eventDao().updateRsvpStatus(event.id, newStatus);
                event.isRsvped = newStatus;
                updateRsvpUi();

                Toast.makeText(
                        EventDetailsActivity.this,
                        newStatus ? R.string.rsvp_joined_toast : R.string.rsvp_cancelled_toast,
                        Toast.LENGTH_SHORT
                ).show();
            });
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
                    R.plurals.spots_count, event.maxAttendees, event.maxAttendees));
            capacityRow.setVisibility(View.VISIBLE);
        } else {
            capacityText.setText(R.string.unlimited_spots);
            capacityRow.setVisibility(View.VISIBLE);
        }

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
        List<String> matchingTags = new ArrayList<>();

        for (String eventTag : eventTags) {
            String cleanEventTag = eventTag.trim();
            for (String interest : interests) {
                if (cleanEventTag.equalsIgnoreCase(interest.trim())) {
                    matchingTags.add(cleanEventTag);
                    break;
                }
            }
        }

        return String.join(", ", matchingTags);
    }

    private String getNonMatchingTags(EventEntity event, String userInterests) {
        if (event == null || event.tags == null || event.tags.trim().isEmpty()) {
            return "";
        }

        String[] eventTags = event.tags.split(",");
        List<String> nonMatchingTags = new ArrayList<>();

        if (userInterests == null || userInterests.trim().isEmpty()) {
            for (String eventTag : eventTags) {
                nonMatchingTags.add(eventTag.trim());
            }
            return String.join(", ", nonMatchingTags);
        }

        String[] interests = userInterests.split(",");

        for (String eventTag : eventTags) {
            String cleanEventTag = eventTag.trim();
            boolean isMatch = false;
            for (String interest : interests) {
                if (cleanEventTag.equalsIgnoreCase(interest.trim())) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                nonMatchingTags.add(cleanEventTag);
            }
        }

        return String.join(", ", nonMatchingTags);
    }

    private void addEventToCalendar() {
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, event.title)
                .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, 
                        (event.location != null ? event.location + ", " : "") + event.city);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date eventDate = dateFormat.parse(event.date);

            if (eventDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(eventDate);

                if (event.time != null && !event.time.trim().isEmpty()) {
                    String[] timeParts = event.time.replace(" AM", "").replace(" PM", "").split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;

                    if (event.time.contains("PM") && hour != 12) {
                        hour += 12;
                    } else if (event.time.contains("AM") && hour == 12) {
                        hour = 0;
                    }

                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                }

                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());

                cal.add(Calendar.HOUR, 2);
                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis());
            }
        } catch (ParseException | NumberFormatException e) {
            Log.e(RSVP_DEBUG, "Error parsing date/time for calendar", e);
        }

        try {
            startActivity(calendarIntent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.no_calendar_app, Toast.LENGTH_SHORT).show();
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
