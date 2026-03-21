package com.meetup;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EventBrowsingActivity extends AppCompatActivity {

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

        String selectedCity = getIntent().getStringExtra("selected_city");
        if (selectedCity == null) {
            selectedCity = "Unknown City";
        }

        TextView cityTitleText = findViewById(R.id.cityTitleText);
        TextView eventsListText = findViewById(R.id.eventsListText);

        cityTitleText.setText("Events in " + selectedCity);
        eventsListText.setText("\n" + getSampleEvents(selectedCity));

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private String getSampleEvents(String city) {
        switch (city) {
            case "Ottawa":
                return "💻 Tech Meetup\n\n🤝 Community Networking Night\n\n🚀 Startup Pitch Event";
            case "Toronto":
                return "🎵 Music Festival Meetup\n\n💼 Business Leaders Brunch\n\n🎨 Art & Culture Walk";
            case "Montreal":
                return "🍽 Food Lovers Meetup\n\n🧠 Design Thinking Workshop\n\n🌍 Language Exchange Night";
            default:
                return "No events available";
        }
    }
}
