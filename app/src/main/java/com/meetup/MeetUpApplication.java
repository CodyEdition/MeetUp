package com.meetup;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MeetUpApplication extends Application {

    public static final String EXTRA_SKIP_WELCOME = "skip_welcome";

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new WelcomeInterceptor());
    }

    private static class WelcomeInterceptor implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            if (!(activity instanceof MainActivity)) return;

            Intent original = activity.getIntent();
            if (original != null && original.getBooleanExtra(EXTRA_SKIP_WELCOME, false)) return;

            boolean isGuest = original != null && original.getBooleanExtra(LoginActivity.EXTRA_IS_GUEST, false);

            if (!isGuest) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                SharedPreferences prefs = activity.getSharedPreferences(
                        WelcomeActivity.PREFS_NAME, MODE_PRIVATE);
                boolean seen = prefs.getBoolean(
                        WelcomeActivity.KEY_WELCOME_SEEN_PREFIX + user.getUid(), false);
                if (seen) return;
            }

            Intent welcome = new Intent(activity, WelcomeActivity.class);
            welcome.putExtra(WelcomeActivity.EXTRA_FORWARD_IS_GUEST, isGuest);
            welcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(welcome);
            activity.finish();
        }

        @Override public void onActivityStarted(@NonNull Activity activity) { }
        @Override public void onActivityResumed(@NonNull Activity activity) { }
        @Override public void onActivityPaused(@NonNull Activity activity) { }
        @Override public void onActivityStopped(@NonNull Activity activity) { }
        @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) { }
        @Override public void onActivityDestroyed(@NonNull Activity activity) { }
    }
}
