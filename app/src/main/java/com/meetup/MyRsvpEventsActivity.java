package com.meetup;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

import java.util.ArrayList;
import java.util.List;

public class MyRsvpEventsActivity extends AppCompatActivity {

    private TextView emptyStateText;
    private ListView rsvpEventsListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton reloadButton;

    private AppDatabase db;
    private final List<EventEntity> rsvpedEvents = new ArrayList<>();
    private ArrayAdapter<EventEntity> adapter;

    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isGuest = getIntent().getBooleanExtra(LoginActivity.EXTRA_IS_GUEST, false);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_rsvp_events);
        SystemUiHelper.applyMeetUpSystemBars(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myRsvpRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emptyStateText = findViewById(R.id.emptyStateText);
        rsvpEventsListView = findViewById(R.id.rsvpEventsListView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        reloadButton = findViewById(R.id.reloadButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        db = AppDatabase.getInstance(this);

        if (isGuest) {
            showStyledMessage(getString(R.string.guest_mode_view_rsvps));
            emptyStateText.setText(R.string.my_rsvpd_events_sign_in_required);
            emptyStateText.setVisibility(View.VISIBLE);
            rsvpEventsListView.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
            reloadButton.setVisibility(View.GONE);
            return;
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.accent_orange);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.background_dark);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRsvpedEvents();
            swipeRefreshLayout.setRefreshing(false);
        });

        reloadButton.setOnClickListener(v -> loadRsvpedEvents());

        setupAdapter();
        loadRsvpedEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGuest) {
            loadRsvpedEvents();
        }
    }

    private void setupAdapter() {
        adapter = new ArrayAdapter<EventEntity>(this, 0, rsvpedEvents) {
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
                    rsvpText.setText(R.string.status_joined);
                    rsvpText.setTextColor(ContextCompat.getColor(MyRsvpEventsActivity.this, R.color.accent_orange));

                    if (event.tags != null && !event.tags.trim().isEmpty()) {
                        tagText.setText(event.tags);
                        tagText.setTextColor(ContextCompat.getColor(MyRsvpEventsActivity.this, R.color.accent_orange));
                        tagText.setVisibility(View.VISIBLE);
                        otherTagsText.setVisibility(View.GONE);
                        tagsContainer.setVisibility(View.VISIBLE);
                    } else {
                        tagsContainer.setVisibility(View.GONE);
                    }
                }

                return view;
            }
        };

        rsvpEventsListView.setAdapter(adapter);

        rsvpEventsListView.setOnItemClickListener((parent, view, position, id) -> {
            EventEntity selectedEvent = rsvpedEvents.get(position);

            Intent intent = new Intent(MyRsvpEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event_id", selectedEvent.id);
            intent.putExtra(LoginActivity.EXTRA_IS_GUEST, false);
            startActivity(intent);
        });
    }

    private void loadRsvpedEvents() {
        rsvpedEvents.clear();

        try {
            List<EventEntity> events = db.eventDao().getRsvpedEvents();

            if (events == null || events.isEmpty()) {
                emptyStateText.setText(R.string.my_rsvpd_events_none_yet);
                emptyStateText.setVisibility(View.VISIBLE);
                rsvpEventsListView.setVisibility(View.GONE);
                return;
            }

            rsvpedEvents.addAll(events);
            adapter.notifyDataSetChanged();
            emptyStateText.setVisibility(View.GONE);
            rsvpEventsListView.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, R.string.my_rsvpd_events_load_error_toast, Toast.LENGTH_SHORT).show();
            emptyStateText.setText(R.string.my_rsvpd_events_load_error_state);
            emptyStateText.setVisibility(View.VISIBLE);
            rsvpEventsListView.setVisibility(View.GONE);
        }
    }

    private void showStyledMessage(String message) {
        View rootView = findViewById(R.id.myRsvpRoot);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.accent_orange));
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.background_dark));
        snackbar.show();
    }
}
