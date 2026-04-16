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
import androidx.tracing.Trace;

import com.google.android.material.snackbar.Snackbar;
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
    private TextView filterInterestButton;

    private AppDatabase db;
    private String selectedCity;
    private final List<EventEntity> filteredEvents = new ArrayList<>();
    private ArrayAdapter<EventEntity> adapter;

    private boolean isGuest = false;
    private String userInterests = "";
    private boolean filterByInterestOnly = false;

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

        bindViews();
        setupRefresh();

        db = AppDatabase.getInstance(this);

        if (db.userDao().getCurrentUser() != null && db.userDao().getCurrentUser().interests != null) {
            userInterests = db.userDao().getCurrentUser().interests;
        }

        selectedCity = getIntent().getStringExtra("selected_city");
        if (selectedCity == null || selectedCity.trim().isEmpty()) {
            Toast.makeText(this, R.string.city_not_selected_returning_home, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView cityTitleText = findViewById(R.id.cityTitleText);
        cityTitleText.setText(getString(R.string.events_in_city, selectedCity));

        if (isGuest) {
            showStyledMessage(getString(R.string.guest_mode_rsvp_or_create));
        }

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

    private void bindViews() {
        loadingText = findViewById(R.id.loadingText);
        errorText = findViewById(R.id.errorText);
        emptyStateText = findViewById(R.id.emptyStateText);
        eventsListView = findViewById(R.id.eventsListView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        filterInterestButton = findViewById(R.id.filterInterestButton);
    }

    private void setupRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_orange);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.background_dark);
        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) -> eventListCanScrollUp());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(EVENT_DEBUG, "Swipe refresh triggered for city: " + selectedCity);
            refreshEvents();
        });
    }

    private boolean eventListCanScrollUp() {
        if (eventsListView == null) {
            return false;
        }
        if (eventsListView.getFirstVisiblePosition() > 0) {
            return true;
        }
        if (eventsListView.getChildCount() == 0) {
            return false;
        }
        View first = eventsListView.getChildAt(0);
        int paddingTop = eventsListView.getPaddingTop();
        return first.getTop() < paddingTop;
    }

    private void setupFilterButton() {
        if (isGuest) {
            filterInterestButton.setVisibility(View.GONE);
            return;
        }

        filterInterestButton.setOnClickListener(v -> {
            if (userInterests == null || userInterests.trim().isEmpty()) {
                Toast.makeText(this, R.string.no_interests_set, Toast.LENGTH_SHORT).show();
                return;
            }

            filterByInterestOnly = !filterByInterestOnly;

            filterInterestButton.setText(
                    filterByInterestOnly
                            ? getString(R.string.show_all_events)
                            : getString(R.string.show_matching_interests_only)
            );

            loadEvents();
        });
    }

    private void setupAdapter() {
        adapter = new ArrayAdapter<>(this, 0, filteredEvents) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                Trace.beginSection("EventBrowsing#getView");
                try {
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
                } finally {
                    Trace.endSection();
                }
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

        View myRsvpButton = findViewById(R.id.myRsvpButton);
        myRsvpButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventBrowsingActivity.this, MyRsvpEventsActivity.class);
            intent.putExtra(LoginActivity.EXTRA_IS_GUEST, isGuest);
            startActivity(intent);
        });

        View createButton = findViewById(R.id.createEventButton);
        createButton.setOnClickListener(v -> {
            if (isGuest) {
                showStyledMessage(getString(R.string.guest_mode_create_only));
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
                    getString(R.string.sample_event_title_1),
                    getString(R.string.sample_event_description_1),
                    getString(R.string.sample_event_city_1),
                    "2026-03-25",
                    "06:00 PM",
                    getString(R.string.sample_event_location_1),
                    50,
                    false,
                    getString(R.string.sample_event_tags_1)
            ));

            db.eventDao().insert(new EventEntity(
                    getString(R.string.sample_event_title_2),
                    getString(R.string.sample_event_description_2),
                    getString(R.string.sample_event_city_2),
                    "2026-03-28",
                    "07:30 PM",
                    getString(R.string.sample_event_location_2),
                    80,
                    false,
                    getString(R.string.sample_event_tags_1)
            ));

            db.eventDao().insert(new EventEntity(
                    getString(R.string.sample_event_title_3),
                    getString(R.string.sample_event_description_3),
                    getString(R.string.sample_event_city_3),
                    "2026-04-02",
                    "05:00 PM",
                    getString(R.string.sample_event_location_3),
                    120,
                    false,
                    getString(R.string.sample_event_tags_3)
            ));

            db.eventDao().insert(new EventEntity(
                    getString(R.string.sample_event_title_4),
                    getString(R.string.sample_event_description_4),
                    getString(R.string.sample_event_city_4),
                    "2026-04-05",
                    "01:00 PM",
                    getString(R.string.sample_event_location_4),
                    40,
                    false,
                    getString(R.string.sample_event_tags_4)
            ));

            db.eventDao().insert(new EventEntity(
                    getString(R.string.sample_event_title_5),
                    getString(R.string.sample_event_description_5),
                    getString(R.string.sample_event_city_5),
                    "2026-04-10",
                    "06:30 PM",
                    getString(R.string.sample_event_location_5),
                    35,
                    false,
                    getString(R.string.sample_event_tags_5)
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
                showEmptyState(getString(R.string.no_events_found_in_city, selectedCity));
                Toast.makeText(this, R.string.no_events_available, Toast.LENGTH_SHORT).show();
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
                    showEmptyState(getString(R.string.no_interest_matches_in_city));
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
            Toast.makeText(this, R.string.error_loading_events_toast, Toast.LENGTH_SHORT).show();
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
        filterInterestButton.setVisibility(View.GONE);
    }

    private void showErrorState() {
        loadingText.setVisibility(View.GONE);
        errorText.setText(R.string.something_went_wrong);
        errorText.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        filterInterestButton.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setText(message);
        emptyStateText.setVisibility(View.VISIBLE);
        eventsListView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        filterInterestButton.setVisibility(View.GONE);
    }

    private void showListState() {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        if (!isGuest) {
            filterInterestButton.setVisibility(View.VISIBLE);
        }
    }

    private void showStyledMessage(String message) {
        View rootView = findViewById(R.id.eventBrowsingMain);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.accent_orange));
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.background_dark));
        snackbar.show();
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
