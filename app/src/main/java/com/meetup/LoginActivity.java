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

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        SystemUiHelper.applyMeetUpSystemBars(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Session check
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
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
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_failed, message), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }
}
