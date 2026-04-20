package com.meetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meetup.db.AppDatabase;
import com.meetup.db.InterestTagEntity;
import com.meetup.db.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private AppDatabase db;
    private FirebaseAuth firebaseAuth;
    private UserEntity currentUser;

    private EditText displayNameEditText;
    private EditText emailEditText;
    private EditText bioEditText;
    private TextView avatarInitials;

    private String originalEmail;

    private final List<String> availableTags = new ArrayList<>();
    private final List<String> selectedInterests = new ArrayList<>();
    private TextView interestsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        SystemUiHelper.applyMeetUpSystemBars(this);
        findViewById(R.id.addCustomInterestButton).setOnClickListener(v -> showAddCustomInterestDialog());



        interestsText = findViewById(R.id.interestsText);
        View saveButton = findViewById(R.id.saveButton);
        final int baseSaveButtonBottomMargin =
                ((ViewGroup.MarginLayoutParams) saveButton.getLayoutParams()).bottomMargin;

        findViewById(R.id.selectInterestsButton).setOnClickListener(v -> showInterestDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int baseBottomPadding = getResources().getDimensionPixelSize(R.dimen.profile_scroll_bottom_padding);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, baseBottomPadding + systemBars.bottom);

            ViewGroup.MarginLayoutParams saveButtonLayoutParams =
                    (ViewGroup.MarginLayoutParams) saveButton.getLayoutParams();
            saveButtonLayoutParams.bottomMargin = baseSaveButtonBottomMargin + systemBars.bottom;
            saveButton.setLayoutParams(saveButtonLayoutParams);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        seedDefaultTagsIfNeeded();
        loadAvailableTagsFromDb();
        firebaseAuth = FirebaseAuth.getInstance();

        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        bioEditText = findViewById(R.id.bioEditText);
        avatarInitials = findViewById(R.id.avatarInitials);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveButton).setOnClickListener(v -> saveProfile());
        findViewById(R.id.changePasswordButton).setOnClickListener(v -> showChangePasswordDialog());

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        currentUser = db.userDao().getCurrentUser();

        if (firebaseUser != null) {
            originalEmail = firebaseUser.getEmail();
            emailEditText.setText(originalEmail);

            // Restore local Room user if missing
            if (currentUser == null) {
                UserEntity restoredUser = new UserEntity(
                        firebaseUser.getUid(),
                        firebaseUser.getEmail(),
                        true
                );

                db.userDao().insertOrUpdateUser(restoredUser);
                currentUser = db.userDao().getCurrentUser();
            }
        }

        if (currentUser != null) {
            displayNameEditText.setText(
                    currentUser.displayName != null ? currentUser.displayName : ""
            );

            bioEditText.setText(
                    currentUser.bio != null ? currentUser.bio : ""
            );

            if (currentUser.interests != null && !currentUser.interests.isEmpty()) {
                String[] saved = currentUser.interests.split(",");
                selectedInterests.clear();

                for (String interest : saved) {
                    selectedInterests.add(interest.trim());
                }

                interestsText.setText(String.join(", ", selectedInterests));
            } else {
                interestsText.setText(R.string.no_interests_selected);
            }
        } else {
            interestsText.setText(R.string.no_interests_selected);
        }

        updateAvatarInitials();
    }

    private void updateAvatarInitials() {
        String displayName = displayNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        String initials;
        if (!displayName.isEmpty()) {
            String[] parts = displayName.split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(Character.toUpperCase(parts[0].charAt(0)))
                        + Character.toUpperCase(parts[1].charAt(0));
            } else {
                initials = displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
            }
        } else if (!email.isEmpty()) {
            initials = email.substring(0, Math.min(2, email.length())).toUpperCase();
        } else {
            initials = "?";
        }

        avatarInitials.setText(initials);
    }

    private void saveProfile() {
        String displayName = displayNameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        if (newEmail.isEmpty()) {
            Toast.makeText(this, R.string.email_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, R.string.error_loading_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        db.userDao().updateProfile(currentUser.id, displayName, bio);
        String interestsString = String.join(",", selectedInterests);
        db.userDao().updateInterests(currentUser.id, interestsString);
        updateAvatarInitials();
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();

        boolean emailChanged = !newEmail.equals(originalEmail);
        if (emailChanged) {
            showReauthDialogForEmailChange(newEmail);
        }
    }

    private void showReauthDialogForEmailChange(String newEmail) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);

        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        currentPasswordLayout.setHint(getString(R.string.enter_password_to_confirm));

        dialogView.findViewById(R.id.newPasswordLayout).setVisibility(View.GONE);
        dialogView.findViewById(R.id.confirmPasswordLayout).setVisibility(View.GONE);

        EditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordEditText);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_MeetUp_AlertDialog)
                .setTitle(R.string.confirm_email_change)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = currentPasswordInput.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, R.string.password_required, Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            reauthenticateAndUpdateEmail(password, newEmail);
        }));

        dialog.show();
    }

    private void reauthenticateAndUpdateEmail(String password, String newEmail) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || originalEmail == null) {
            Toast.makeText(this, R.string.error_not_signed_in, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(originalEmail, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> user.verifyBeforeUpdateEmail(newEmail)
                        .addOnSuccessListener(aVoid2 -> {
                            emailEditText.setText(originalEmail);
                            Toast.makeText(this, R.string.verification_email_sent, Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, getString(R.string.email_update_failed, e.getMessage()),
                                        Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.incorrect_password, Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordEditText);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordEditText);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_MeetUp_AlertDialog)
                .setTitle(R.string.change_password)
                .setView(dialogView)
                .setPositiveButton(R.string.change, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = currentPasswordInput.getText().toString();
            String newPassword = newPasswordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (!validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                return;
            }

            dialog.dismiss();
            changePassword(currentPassword, newPassword);
        }));

        dialog.show();
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            Toast.makeText(this, R.string.current_password_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (newPassword.isEmpty()) {
            Toast.makeText(this, R.string.new_password_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (newPassword.length() < AuthInputValidator.MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, getResources().getQuantityString(R.plurals.password_min_characters,
                            AuthInputValidator.MIN_PASSWORD_LENGTH, AuthInputValidator.MIN_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || originalEmail == null) {
            Toast.makeText(this, R.string.error_not_signed_in, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(originalEmail, currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid2 ->
                                Toast.makeText(this, R.string.password_changed, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, getString(R.string.password_change_failed, e.getMessage()),
                                        Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.incorrect_password, Toast.LENGTH_SHORT).show());
    }


    private void showInterestDialog() {
        String[] tagsArray = availableTags.toArray(new String[0]);
        boolean[] checkedItems = new boolean[tagsArray.length];

        for (int i = 0; i < tagsArray.length; i++) {
            checkedItems[i] = selectedInterests.contains(tagsArray[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_interests_title)
                .setMultiChoiceItems(tagsArray, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedInterests.contains(tagsArray[which])) {
                            selectedInterests.add(tagsArray[which]);
                        }
                    } else {
                        selectedInterests.remove(tagsArray[which]);
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> interestsText.setText(
                        selectedInterests.isEmpty()
                                ? getString(R.string.no_interests_selected)
                                : String.join(", ", selectedInterests)
                ))
                .show();
    }
    private void seedDefaultTagsIfNeeded() {
        if (db.interestTagDao().getAllTags().isEmpty()) {
            String[] defaultTags = getResources().getStringArray(R.array.default_interest_tags);
            for (String tag : defaultTags) {
                db.interestTagDao().insertTag(new InterestTagEntity(tag));
            }
        }
    }
    private void loadAvailableTagsFromDb() {
        availableTags.clear();
        List<InterestTagEntity> tags = db.interestTagDao().getAllTags();
        for (InterestTagEntity tag : tags) {
            availableTags.add(tag.name);
        }
    }
    private void showAddCustomInterestDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.enter_new_interest_tag);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_custom_interest)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String newTag = input.getText().toString().trim();

                    if (newTag.isEmpty()) {
                        Toast.makeText(this, R.string.interest_tag_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String formattedTag = newTag.substring(0, 1).toUpperCase() + newTag.substring(1).trim();

                    InterestTagEntity existing = db.interestTagDao().findByName(formattedTag);
                    if (existing == null) {
                        db.interestTagDao().insertTag(new InterestTagEntity(formattedTag));
                    }

                    loadAvailableTagsFromDb();

                    boolean alreadySelected = false;
                    for (String tag : selectedInterests) {
                        if (tag.equalsIgnoreCase(formattedTag)) {
                            alreadySelected = true;
                            break;
                        }
                    }

                    if (!alreadySelected) {
                        selectedInterests.add(formattedTag);
                    }

                    interestsText.setText(
                            String.join(", ", selectedInterests)
                    );

                    Toast.makeText(this, R.string.custom_interest_added, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
