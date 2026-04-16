package com.meetup;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meetup.db.AppDatabase;
import com.meetup.db.UserEntity;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_IS_GUEST = "is_guest";
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        SystemUiHelper.applyLoginSystemBars(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Session check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            saveUserToDb(currentUser);
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        findViewById(R.id.signInButton).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.signUpButton).setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();

        });

        findViewById(R.id.guestButton).setOnClickListener(v -> continueAsGuest());

    }

    private void attemptLogin() {
        clearErrors();

        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        AuthInputValidator.LoginResult validation = AuthInputValidator.validateLogin(email, password);
        boolean valid = true;
        if (validation.emailEmpty) {
            emailLayout.setError(getString(R.string.error_empty_email));
            valid = false;
        }
        if (validation.passwordEmpty) {
            passwordLayout.setError(getString(R.string.error_empty_password));
            valid = false;
        }
        if (!valid) return;

        findViewById(R.id.signInButton).setEnabled(false);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    findViewById(R.id.signInButton).setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            saveUserToDb(user);
                        }
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : getString(R.string.unknown_error);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_failed, message), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void continueAsGuest() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_IS_GUEST, true);
        startActivity(intent);
        finish();
    }

    private void saveUserToDb(FirebaseUser firebaseUser) {
        UserEntity userEntity = new UserEntity(firebaseUser.getUid(), firebaseUser.getEmail(), true);
        AppDatabase.getInstance(this).userDao().insertOrUpdateUser(userEntity);
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }
}
