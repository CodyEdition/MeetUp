package com.meetup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    public static final String EXTRA_FORWARD_IS_GUEST = "welcome_forward_is_guest";
    public static final String PREFS_NAME = "meetup_welcome";
    public static final String KEY_WELCOME_SEEN_PREFIX = "welcome_seen_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        SystemUiHelper.applyMeetUpSystemBars(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomeRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top + 24, v.getPaddingRight(), bars.bottom + 24);
            return insets;
        });

        boolean isGuest = getIntent().getBooleanExtra(EXTRA_FORWARD_IS_GUEST, false);

        View logo = findViewById(R.id.welcomeLogo);
        View title = findViewById(R.id.welcomeTitle);
        View subtitle = findViewById(R.id.welcomeSubtitle);
        View features = findViewById(R.id.featuresContainer);
        View button = findViewById(R.id.getStartedButton);

        logo.setAlpha(0f);
        logo.setScaleX(0.9f);
        logo.setScaleY(0.9f);
        title.setAlpha(0f);
        subtitle.setAlpha(0f);
        features.setAlpha(0f);
        features.setTranslationY(40f);
        button.setAlpha(0f);

        logo.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        title.animate().alpha(1f).setStartDelay(200).setDuration(400).start();
        subtitle.animate().alpha(1f).setStartDelay(300).setDuration(400).start();
        features.animate().alpha(1f).translationY(0f).setStartDelay(400).setDuration(500).start();
        button.animate().alpha(1f).setStartDelay(700).setDuration(400).start();

        button.setOnClickListener(v -> {
            if (!isGuest) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putBoolean(KEY_WELCOME_SEEN_PREFIX + user.getUid(), true).apply();
                }
            }

            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.putExtra(LoginActivity.EXTRA_IS_GUEST, isGuest);
            intent.putExtra(MeetUpApplication.EXTRA_SKIP_WELCOME, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
