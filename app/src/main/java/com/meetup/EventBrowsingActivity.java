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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

import java.util.ArrayList;
import java.util.List;

public class EventBrowsingActivity extends AppCompatActivity {

    private static final String EVENT_DEBUG = "EVENT_DEBUG";

    private TextView cityTitleText;
    private TextView loadingText;
    private TextView errorText;
    private TextView emptyStateText;
    private ListView eventsListView;

    private AppDatabase db;
    private String selectedCity;
    private final List<EventEntity> filteredEvents = new ArrayList<>();
    private ArrayAdapter<EventEntity> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_browsing);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eventBrowsingMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cityTitleText = findViewById(R.id.cityTitleText);
        loadingText = findViewById(R.id.loadingText);
        errorText = findViewById(R.id.errorText);
        emptyStateText = findViewById(R.id.emptyStateText);
        eventsListView = findViewById(R.id.eventsListView);

        db = AppDatabase.getInstance(this);

        selectedCity = getIntent().getStringExtra("selected_city");
        if (selectedCity == null || selectedCity.trim().isEmpty()) {
            selectedCity = "Unknown City";
        }

        cityTitleText.setText("Events in " + selectedCity);

        adapter = new ArrayAdapter<EventEntity>(this, 0, filteredEvents) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_event_browsing, parent, false);
                }

                EventEntity event = getItem(position);

                TextView titleText = view.findViewById(R.id.eventTitleItem);
                TextView metaText = view.findViewById(R.id.eventMetaItem);
                TextView rsvpText = view.findViewById(R.id.eventRsvpItem);

                if (event != null) {
                    titleText.setText(event.title);
                    metaText.setText(event.date + " • " + event.city);

                    // isRsvped represents whether the current user has joined this event
                    if (event.isRsvped) {
                        rsvpText.setText("Status: Joined");
                        rsvpText.setTextColor(getResources().getColor(R.color.accent_orange));
                    } else {
                        rsvpText.setText("Status: Not Joined");
                        rsvpText.setTextColor(getResources().getColor(R.color.text_on_dark));
                    }
                }

                return view;
            }
        };

        eventsListView.setAdapter(adapter);

        eventsListView.setOnItemClickListener((parent, view, position, id) -> {
            EventEntity selectedEvent = filteredEvents.get(position);

            Intent intent = new Intent(EventBrowsingActivity.this, EventDetailsActivity.class);
            intent.putExtra("event_id", selectedEvent.id);
            intent.putExtra("event_title", selectedEvent.title);
            intent.putExtra("event_description", selectedEvent.description);
            intent.putExtra("event_city", selectedEvent.city);
            intent.putExtra("event_date", selectedEvent.date);
            startActivity(intent);
        });

        findViewById(R.id.reloadButton).setOnClickListener(v -> {
            loadingText.setText("Refreshing...");
            Log.d(EVENT_DEBUG, "Manual reload triggered for city: " + selectedCity);
            loadEvents();
            Toast.makeText(EventBrowsingActivity.this, "Events refreshed", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        findViewById(R.id.createEventButton).setOnClickListener(v -> {
            Intent intent = new Intent(EventBrowsingActivity.this, CreateEventActivity.class);
            intent.putExtra("selected_city", selectedCity);
            startActivity(intent);
        });

        seedSampleDataIfNeeded();
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(EVENT_DEBUG, "onResume triggered, reloading events for city: " + selectedCity);
        loadEvents();
    }

    private void seedSampleDataIfNeeded() {
        if (db.eventDao().getAll().isEmpty()) {
            Log.d(EVENT_DEBUG, "Database empty, inserting sample events");

            db.eventDao().insert(new EventEntity("Tech Meetup", "A meetup for developers", "Ottawa", "2026-03-25"));
            db.eventDao().insert(new EventEntity("Startup Pitch Night", "Pitch your startup idea", "Ottawa", "2026-03-28"));
            db.eventDao().insert(new EventEntity("Music Festival Meetup", "Meet before the festival", "Toronto", "2026-04-02"));
            db.eventDao().insert(new EventEntity("Art Walk", "Explore local galleries", "Toronto", "2026-04-05"));
            db.eventDao().insert(new EventEntity("Language Exchange", "Practice languages together", "Montreal", "2026-04-10"));
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
                showEmptyState();
                return;
            }

            filteredEvents.addAll(events);
            Log.d(EVENT_DEBUG, "Events found: " + filteredEvents.size());

            adapter.notifyDataSetChanged();
            showListState();

        } catch (Exception e) {
            Log.e(EVENT_DEBUG, "Error loading events", e);
            showErrorState();
        }
    }

    private void showLoadingState() {
        loadingText.setText("Loading events...");
        loadingText.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
    }

    private void showErrorState() {
        loadingText.setVisibility(View.GONE);
        errorText.setText("Something went wrong while loading events.");
        errorText.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setText("No events yet. Tap 'Create Event' to add one.");
        emptyStateText.setVisibility(View.VISIBLE);
        eventsListView.setVisibility(View.GONE);
    }

    private void showListState() {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.VISIBLE);
    }
}
