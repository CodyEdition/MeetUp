package com.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.meetup.db.AppDatabase;
import com.meetup.db.CityEntity;
import com.meetup.db.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private final List<String> cityDisplayNames = new ArrayList<>();
    private CityWheelAdapter adapter;
    private TextView profileAvatarInitials;

    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        isGuest = getIntent().getBooleanExtra(LoginActivity.EXTRA_IS_GUEST, false);
        SystemUiHelper.applyMeetUpSystemBars(this);

        db = AppDatabase.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileAvatarInitials = findViewById(R.id.profileAvatarInitials);
        TextView welcomeText = findViewById(R.id.welcomeText);

        if (isGuest) {
            welcomeText.setText("Welcome, Guest");
            findViewById(R.id.profileAvatarButton).setVisibility(View.GONE);
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            welcomeText.setText(
                    getString(R.string.welcome, email != null ? email : "")
            );
            updateProfileAvatar();
        }

        findViewById(R.id.profileAvatarButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        seedCitiesIfNeeded();
        loadCities();

        RecyclerView recyclerView = findViewById(R.id.cityWheelRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CityWheelUiHelper.SelectionBandDecoration(this));

        adapter = new CityWheelAdapter(cityDisplayNames, this::openEventBrowsing);
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

            if (!isGuest) {
                FirebaseAuth.getInstance().signOut();
                db.userDao().clearUser();
            }

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.addCityButton).setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(this, "Guest users cannot add cities. Please sign in.", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddCityDialog();
        });
    }

    private void seedCitiesIfNeeded() {
        List<CityEntity> existing = db.cityDao().getAllCities();
        if (existing.isEmpty()) {
            for (CityHub hub : CityHub.values()) {
                db.cityDao().insertCity(new CityEntity(hub.getDisplayName(), hub.getProvince()));
            }
        }
    }

    private void loadCities() {
        cityDisplayNames.clear();
        List<CityEntity> cities = db.cityDao().getAllCities();
        for (CityEntity city : cities) {
            cityDisplayNames.add(city.getFullDisplayName());
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddCityDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_city, null);
        EditText cityInput = dialogView.findViewById(R.id.editCityName);
        EditText provinceInput = dialogView.findViewById(R.id.editProvinceName);

        new AlertDialog.Builder(this, R.style.ThemeOverlay_MeetUp_AlertDialog)
                .setTitle("Add New City Hub")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String cityName = cityInput.getText().toString().trim();
                    String province = provinceInput.getText().toString().trim();

                    if (!cityName.isEmpty() && !province.isEmpty()) {
                        db.cityDao().insertCity(new CityEntity(cityName, province));
                        loadCities();
                        Toast.makeText(this, "City added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openEventBrowsing(String cityFullDisplayName) {
        // cityFullDisplayName is "City, Province"
        // We only want the City name for filtering events for now, or we can use the full name
        String cityName = cityFullDisplayName.split(",")[0].trim();

        UserEntity user = db.userDao().getCurrentUser();
        if (user != null) {
            user.lastSelectedCity = cityName;
            db.userDao().insertOrUpdateUser(user);
        }

        Intent intent = new Intent(MainActivity.this, EventBrowsingActivity.class);
        intent.putExtra("selected_city", cityName);
        intent.putExtra(LoginActivity.EXTRA_IS_GUEST, isGuest);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGuest) {
            updateProfileAvatar();
        }
    }

    private void updateProfileAvatar() {
        UserEntity user = db.userDao().getCurrentUser();
        String email = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

        String initials;
        if (user != null && user.displayName != null && !user.displayName.isEmpty()) {
            String displayName = user.displayName;
            String[] parts = displayName.split("\\s+");
            if (parts.length >= 2) {
                initials = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            } else {
                initials = displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
            }
        } else if (email != null && !email.isEmpty()) {
            initials = email.substring(0, Math.min(2, email.length())).toUpperCase();
        } else {
            initials = "?";
        }

        profileAvatarInitials.setText(initials);
    }
}