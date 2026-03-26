package com.meetup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

    private TextView cityTitleText;
    private TextView loadingText;
    private TextView errorText;
    private TextView emptyStateText;
    private ListView eventsListView;

    private AppDatabase db;
    private String selectedCity;
    private List<EventEntity> filteredEvents = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<String> eventTitles = new ArrayList<>();

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
        if (selectedCity == null) {
            SharedPreferences prefs = getSharedPreferences("meetup_prefs", MODE_PRIVATE);
            selectedCity = prefs.getString("selected_city_hub", null);
        }
        if (selectedCity == null) {
            selectedCity = CityHub.values()[0].getDisplayName();
        }

        cityTitleText.setText("Events in " + selectedCity);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventTitles);
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

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.createEventButton).setOnClickListener(v -> {
            Intent intent = new Intent(EventBrowsingActivity.this, CreateEventActivity.class);
            intent.putExtra("selected_city", selectedCity);
            startActivity(intent);
        });
            // sample data to test with
        if (db.eventDao().getAll().isEmpty()) {
            db.eventDao().insert(new EventEntity("Tech Meetup", "A meetup for developers", "Ottawa", "2026-03-25"));
            db.eventDao().insert(new EventEntity("Startup Pitch Night", "Pitch your startup idea", "Ottawa", "2026-03-28"));
            db.eventDao().insert(new EventEntity("Music Festival Meetup", "Meet before the festival", "Toronto", "2026-04-02"));
            db.eventDao().insert(new EventEntity("Art Walk", "Explore local galleries", "Toronto", "2026-04-05"));
            db.eventDao().insert(new EventEntity("Language Exchange", "Practice languages together", "Montreal", "2026-04-10"));
        }

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        showLoadingState();

        try {
            filteredEvents = db.eventDao().getEventsByCity(selectedCity);

            eventTitles.clear();

            if (filteredEvents == null || filteredEvents.isEmpty()) {
                showEmptyState();
                return;
            }

            for (EventEntity event : filteredEvents) {
                eventTitles.add(event.title + " - " + event.date);
            }

            adapter.notifyDataSetChanged();
            showListState();

        } catch (Exception e) {
            showErrorState();
        }
    }

    private void showLoadingState() {
        loadingText.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
    }

    private void showErrorState() {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        eventsListView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
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