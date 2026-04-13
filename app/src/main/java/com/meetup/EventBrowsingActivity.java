package com.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

import java.util.ArrayList;
import java.util.List;

public class EventBrowsingActivity extends AppCompatActivity {

    private static final String EVENT_DEBUG = "EVENT_DEBUG";

    private TextView loadingText;
    private TextView errorText;
    private TextView emptyStateText;
    private ListView eventsListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private AppDatabase db;
    private String selectedCity;
    private final List<EventEntity> filteredEvents = new ArrayList<>();
    private ArrayAdapter<EventEntity> adapter;

    private boolean isGuest = false;
    private String userInterests = "";

    private boolean filterByInterestOnly = false;
    private TextView filterInterestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_browsing);
        SystemUiHelper.applyMeetUpSystemBars(this);

        isGuest = getIntent().getBooleanExtra(LoginActivity.EXTRA_IS_GUEST, false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eventBrowsingMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView cityTitleText = findViewById(R.id.cityTitleText);
        loadingText = findViewById(R.id.loadingText);
        errorText = findViewById(R.id.errorText);
        emptyStateText = findViewById(R.id.emptyStateText);
        eventsListView = findViewById(R.id.eventsListView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        filterInterestButton = findViewById(R.id.filterInterestButton);

        swipeRefreshLayout.setColorSchemeResources(R.color.accent_orange);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.background_dark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(EVENT_DEBUG, "Swipe refresh triggered for city: " + selectedCity);
            refreshEvents();
        });

        db = AppDatabase.getInstance(this);

        if (db.userDao().getCurrentUser() != null && db.userDao().getCurrentUser().interests != null) {
            userInterests = db.userDao().getCurrentUser().interests;
        }

        selectedCity = getIntent().getStringExtra("selected_city");
        if (selectedCity == null || selectedCity.trim().isEmpty()) {
            Toast.makeText(this, "City not selected. Returning to home.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cityTitleText.setText(getString(R.string.events_in_city, selectedCity));

        setupFilterButton();
        setupAdapter();
        setupClickListeners();

        seedSampleDataIfNeeded();
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(EVENT_DEBUG, "onResume triggered, reloading events for city: " + selectedCity);
        loadEvents();
    }

    private void setupFilterButton() {
        if (isGuest) {
            filterInterestButton.setVisibility(View.GONE);
            return;
        }

        filterInterestButton.setOnClickListener(v -> {
            if (userInterests == null || userInterests.trim().isEmpty()) {
                Toast.makeText(this, "No interests set in your profile.", Toast.LENGTH_SHORT).show();
                return;
            }

            filterByInterestOnly = !filterByInterestOnly;

            filterInterestButton.setText(
                    filterByInterestOnly
                            ? "Show All Events"
                            : "Show Matching Interests Only"
            );

            loadEvents();
        });
    }

    private void setupAdapter() {
        adapter = new ArrayAdapter<EventEntity>(this, 0, filteredEvents) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_event_browsing, parent, false);
                }

                EventEntity event = getItem(position);

                TextView titleText = view.findViewById(R.id.eventTitleItem);
                TextView metaText = view.findViewById(R.id.eventMetaItem);
                TextView rsvpText = view.findViewById(R.id.eventRsvpItem);
                TextView tagText = view.findViewById(R.id.eventTagItem);
                TextView otherTagsText = view.findViewById(R.id.eventOtherTagsItem);
                View tagsContainer = view.findViewById(R.id.tagsContainer);

                if (event != null) {
                    titleText.setText(event.title);
                    metaText.setText(getString(R.string.event_date_city, event.date, event.city));

                    if (event.isRsvped) {
                        rsvpText.setText(R.string.status_joined);
                        rsvpText.setTextColor(ContextCompat.getColor(EventBrowsingActivity.this, R.color.accent_orange));
                    } else {
                        rsvpText.setText(R.string.status_not_joined);
                        rsvpText.setTextColor(ContextCompat.getColor(EventBrowsingActivity.this, R.color.text_on_dark));
                    }

                    if (event.tags != null && !event.tags.trim().isEmpty()) {
                        String matchingTags = getMatchingTags(event, userInterests);
                        String nonMatchingTags = getNonMatchingTags(event, userInterests);

                        if (!matchingTags.isEmpty()) {
                            tagText.setText(getString(R.string.tags_matching_prefix, matchingTags));
                            tagText.setTextColor(ContextCompat.getColor(EventBrowsingActivity.this, R.color.accent_orange));
                            tagText.setVisibility(View.VISIBLE);

                            if (!nonMatchingTags.isEmpty()) {
                                otherTagsText.setText(getString(R.string.tags_other_prefix, nonMatchingTags));
                                otherTagsText.setVisibility(View.VISIBLE);
                            } else {
                                otherTagsText.setVisibility(View.GONE);
                            }
                        } else {
                            tagText.setVisibility(View.GONE);
                            otherTagsText.setText(event.tags);
                            otherTagsText.setVisibility(View.VISIBLE);
                        }

                        tagsContainer.setVisibility(View.VISIBLE);
                    } else {
                        tagsContainer.setVisibility(View.GONE);
                    }
                }

                return view;
            }
        };

        eventsListView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        eventsListView.setOnItemClickListener((parent, view, position, id) -> {
            EventEntity selectedEvent = filteredEvents.get(position);

            Intent intent = new Intent(EventBrowsingActivity.this, EventDetailsActivity.class);
            intent.putExtra("event_id", selectedEvent.id);
            intent.putExtra(LoginActivity.EXTRA_IS_GUEST, isGuest);
            startActivity(intent);
        });

        findViewById(R.id.reloadButton).setOnClickListener(v -> {
            Log.d(EVENT_DEBUG, "Manual reload triggered for city: " + selectedCity);
            swipeRefreshLayout.setRefreshing(true);
            refreshEvents();
        });

        View createButton = findViewById(R.id.createEventButton);
        createButton.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(this, R.string.guest_cannot_create_events, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(EventBrowsingActivity.this, CreateEventActivity.class);
            intent.putExtra("selected_city", selectedCity);
            intent.putExtra(LoginActivity.EXTRA_IS_GUEST, false);
            startActivity(intent);
        });

        if (isGuest) {
            createButton.setVisibility(View.GONE);
        }
    }

    private void seedSampleDataIfNeeded() {
        if (db.eventDao().getAll().isEmpty()) {
            Log.d(EVENT_DEBUG, "Database empty, inserting sample events");

            db.eventDao().insert(new EventEntity(
                    "Tech Meetup",
                    "A meetup for developers",
                    "Ottawa",
                    "2026-03-25",
                    "06:00 PM",
                    "Downtown Hub",
                    50,
                    false,
                    "Tech, Networking"
            ));

            db.eventDao().insert(new EventEntity(
                    "Startup Pitch Night",
                    "Pitch your startup idea",
                    "Ottawa",
                    "2026-03-28",
                    "07:30 PM",
                    "Innovation Centre",
                    80,
                    false,
                    "Tech, Networking"
            ));

            db.eventDao().insert(new EventEntity(
                    "Music Festival Meetup",
                    "Meet before the festival",
                    "Toronto",
                    "2026-04-02",
                    "05:00 PM",
                    "Harbourfront",
                    120,
                    false,
                    "Music, Art"
            ));

            db.eventDao().insert(new EventEntity(
                    "Art Walk",
                    "Explore local galleries",
                    "Toronto",
                    "2026-04-05",
                    "01:00 PM",
                    "Queen Street West",
                    40,
                    false,
                    "Art, Culture"
            ));

            db.eventDao().insert(new EventEntity(
                    "Language Exchange",
                    "Practice languages together",
                    "Montreal",
                    "2026-04-10",
                    "06:30 PM",
                    "Old Port Café",
                    35,
                    false,
                    "Networking, Culture"
            ));
        }
    }

    private void loadEvents() {
        showLoadingState();
        Log.d(EVENT_DEBUG, "Loading events for city: " + selectedCity);

        try {
            filteredEvents.clear();

            List<EventEntity> events = db.eventDao().getEventsByCity(selectedCity);

            if (events == null || events.isEmpty()) {
                Log.d(EVENT_DEBUG, "No events found for city: " + selectedCity);
                adapter.notifyDataSetChanged();
                showEmptyState("No events found in " + selectedCity);
                Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
                return;
            }

            events.sort((e1, e2) -> {
                boolean e1Matches = matchesUserInterests(e1, userInterests);
                boolean e2Matches = matchesUserInterests(e2, userInterests);

                if (e1Matches == e2Matches) {
                    return 0;
                }
                return e1Matches ? -1 : 1;
            });

            if (filterByInterestOnly) {
                for (EventEntity event : events) {
                    if (matchesUserInterests(event, userInterests)) {
                        filteredEvents.add(event);
                    }
                }

                if (filteredEvents.isEmpty()) {
                    adapter.notifyDataSetChanged();
                    showEmptyState("No events match your interests in this city.");
                    return;
                }
            } else {
                filteredEvents.addAll(events);
            }

            Log.d(EVENT_DEBUG, "Events shown: " + filteredEvents.size());
            adapter.notifyDataSetChanged();
            showListState();

        } catch (Exception e) {
            Log.e(EVENT_DEBUG, "Error loading events", e);
            Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
            showErrorState();
        }
    }

    private void refreshEvents() {
        loadEvents();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showLoadingState() {
        loadingText.setText(R.string.loading_events);
        loadingText.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void showErrorState() {
        loadingText.setVisibility(View.GONE);
        errorText.setText(R.string.something_went_wrong);
        errorText.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setText(message);
        emptyStateText.setVisibility(View.VISIBLE);
        eventsListView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void showListState() {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    private boolean matchesUserInterests(EventEntity event, String userInterests) {
        if (event == null || event.tags == null || event.tags.trim().isEmpty()) {
            return false;
        }

        if (userInterests == null || userInterests.trim().isEmpty()) {
            return false;
        }

        String[] eventTags = event.tags.split(",");
        String[] interests = userInterests.split(",");

        for (String eventTag : eventTags) {
            String cleanEventTag = eventTag.trim();
            for (String interest : interests) {
                if (cleanEventTag.equalsIgnoreCase(interest.trim())) {
                    return true;
                }
            }
        }

        return false;
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
}
