package com.meetup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        SystemUiHelper.applyMeetUpSystemBars(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            ((TextView) findViewById(R.id.welcomeText)).setText(
                    getString(R.string.welcome, email != null ? email : "")
            );
        }

        List<String> cities = new ArrayList<>();
        for (CityHub hub : CityHub.values()) {
            cities.add(hub.getDisplayName());
        }

        RecyclerView recyclerView = findViewById(R.id.cityWheelRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CityWheelUiHelper.SelectionBandDecoration(this));

        CityWheelAdapter adapter = new CityWheelAdapter(cities, this::openEventBrowsing);
        recyclerView.setAdapter(adapter);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                CityWheelUiHelper.applyWheelTransforms(rv);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    CityWheelUiHelper.applyWheelTransforms(rv);
                }
            }
        });
        recyclerView.post(() -> CityWheelUiHelper.applyWheelTransforms(recyclerView));

        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openEventBrowsing(String city) {
        SharedPreferences prefs = getSharedPreferences("meetup_prefs", MODE_PRIVATE);
        prefs.edit().putString("selected_city_hub", city).apply();

        Intent intent = new Intent(MainActivity.this, EventBrowsingActivity.class);
        intent.putExtra("selected_city", city);
        startActivity(intent);
    }
}
