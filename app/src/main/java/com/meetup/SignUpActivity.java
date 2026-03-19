package com.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;

    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.signUpButton).setOnClickListener(v -> attemptSignUp());
        findViewById(R.id.loginButton).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignUp() {
        clearErrors();

        String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "";

        boolean valid = true;
        if (username.isEmpty()) {
            usernameLayout.setError(getString(R.string.error_empty_username));
            valid = false;
        }
        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.error_empty_email));
            valid = false;
        }
        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_empty_password));
            valid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordLayout.setError(getString(R.string.error_password_too_short));
            valid = false;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError(getString(R.string.error_empty_confirm_password));
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!valid) return;

        View signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setEnabled(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    signUpButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.sign_up_success, Snackbar.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.sign_up_failed, message), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }
}
