package com.meetup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.meetup.db.AppDatabase;
import com.meetup.db.CityEntity;
import com.meetup.db.EventEntity;
import com.meetup.db.InterestTagEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private ScrollView scrollView;

    private TextInputLayout eventTitleLayout;
    private TextInputLayout eventDescriptionLayout;
    private TextInputLayout eventDateLayout;
    private TextInputLayout eventTimeLayout;
    private TextInputLayout eventCityLayout;
    private TextInputLayout eventLocationLayout;
    private TextInputLayout eventMaxAttendeesLayout;

    private TextInputEditText eventTitleEditText;
    private TextInputEditText eventDescriptionEditText;
    private TextInputEditText eventDateEditText;
    private TextInputEditText eventTimeEditText;
    private AutoCompleteTextView eventCityDropdown;
    private TextInputEditText eventLocationEditText;
    private TextInputEditText eventMaxAttendeesEditText;

    private AppDatabase db;
    private final List<String> selectedTags = new ArrayList<>();
    private final List<String> availableTags = new ArrayList<>();


    private TextView selectedTagsText;


    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);
        loadAvailableTagsFromDb();
        SystemUiHelper.applyMeetUpSystemBars(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createEventRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scrollView = findViewById(R.id.createEventRoot);

        eventTitleLayout = findViewById(R.id.eventTitleLayout);
        eventDescriptionLayout = findViewById(R.id.eventDescriptionLayout);
        eventDateLayout = findViewById(R.id.eventDateLayout);
        eventTimeLayout = findViewById(R.id.eventTimeLayout);
        eventCityLayout = findViewById(R.id.eventCityLayout);
        eventLocationLayout = findViewById(R.id.eventLocationLayout);
        eventMaxAttendeesLayout = findViewById(R.id.eventMaxAttendeesLayout);
        selectedTagsText = findViewById(R.id.selectedTagsText);

        findViewById(R.id.selectTagsButton).setOnClickListener(v -> showTagSelectionDialog());

        eventTitleEditText = findViewById(R.id.eventTitleEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventDateEditText = findViewById(R.id.eventDateEditText);
        eventTimeEditText = findViewById(R.id.eventTimeEditText);
        eventCityDropdown = findViewById(R.id.eventCityDropdown);
        eventLocationEditText = findViewById(R.id.eventLocationEditText);
        eventMaxAttendeesEditText = findViewById(R.id.eventMaxAttendeesEditText);

        loadCitiesIntoDropdown();

        eventTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventTitleLayout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        eventDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventDescriptionLayout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        eventLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventLocationLayout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        eventMaxAttendeesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventMaxAttendeesLayout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        eventDateEditText.setOnClickListener(v -> showDatePicker());
        eventDateLayout.setEndIconOnClickListener(v -> showDatePicker());

        eventTimeEditText.setOnClickListener(v -> showTimePicker());
        eventTimeLayout.setEndIconOnClickListener(v -> showTimePicker());

        eventCityDropdown.setOnItemClickListener((parent, view, position, id) ->
                eventCityLayout.setError(null));

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.createEventButton).setOnClickListener(v -> attemptCreateEvent());
    }

    private void loadCitiesIntoDropdown() {
        List<CityEntity> cityEntities = AppDatabase.getInstance(this).cityDao().getAllCities();
        List<String> cities = new ArrayList<>();
        for (CityEntity city : cityEntities) {
            cities.add(city.cityName);
        }

        // Use custom item_dropdown to ensure black text colour
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, cities);
        eventCityDropdown.setAdapter(cityAdapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            eventDateEditText.setText(date);
            eventDateLayout.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null);

        NumberPicker hourPicker = dialogView.findViewById(R.id.hourPicker);
        NumberPicker minutePicker = dialogView.findViewById(R.id.minutePicker);
        NumberPicker amPmPicker = dialogView.findViewById(R.id.amPmPicker);

        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));

        amPmPicker.setMinValue(0);
        amPmPicker.setMaxValue(1);
        amPmPicker.setDisplayedValues(new String[]{getString(R.string.am_label), getString(R.string.pm_label)});

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        hourPicker.setValue(currentHour % 12 == 0 ? 12 : currentHour % 12);
        minutePicker.setValue(now.get(Calendar.MINUTE));
        amPmPicker.setValue(currentHour < 12 ? 0 : 1);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.event_time))
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    int hour = hourPicker.getValue();
                    int minute = minutePicker.getValue();
                    int amPm = amPmPicker.getValue();

                    selectedHour = (hour % 12) + (amPm == 1 ? 12 : 0);
                    selectedMinute = minute;

                    String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm == 0 ? getString(R.string.am_label) : getString(R.string.pm_label));
                    eventTimeEditText.setText(time);
                    eventTimeLayout.setError(null);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void attemptCreateEvent() {
        eventTitleLayout.setError(null);
        eventDescriptionLayout.setError(null);
        eventDateLayout.setError(null);
        eventTimeLayout.setError(null);
        eventCityLayout.setError(null);
        eventLocationLayout.setError(null);
        eventMaxAttendeesLayout.setError(null);

        String title = eventTitleEditText.getText() != null ? eventTitleEditText.getText().toString().trim() : "";
        String description = eventDescriptionEditText.getText() != null ? eventDescriptionEditText.getText().toString().trim() : "";
        String date = eventDateEditText.getText() != null ? eventDateEditText.getText().toString().trim() : "";
        String time = eventTimeEditText.getText() != null ? eventTimeEditText.getText().toString().trim() : "";
        String city = eventCityDropdown.getText().toString().trim();
        String location = eventLocationEditText.getText() != null ? eventLocationEditText.getText().toString().trim() : "";
        String maxAttendeesStr = eventMaxAttendeesEditText.getText() != null ? eventMaxAttendeesEditText.getText().toString().trim() : "";

        boolean valid = true;

        if (title.isEmpty()) {
            eventTitleLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (description.isEmpty()) {
            eventDescriptionLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (date.isEmpty()) {
            eventDateLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (time.isEmpty()) {
            eventTimeLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (city.isEmpty()) {
            eventCityLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (location.isEmpty()) {
            eventLocationLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (maxAttendeesStr.isEmpty()) {
            eventMaxAttendeesLayout.setError(getString(R.string.error_required));
            valid = false;
        }

        if (!valid) {
            scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
            return;
        }

        if (isDateInPast(date)) {
            eventDateLayout.setError(getString(R.string.error_date_past));
            return;
        }

        if (isDateToday(date) && selectedHour >= 0) {
            Calendar now = Calendar.getInstance();
            if (selectedHour < now.get(Calendar.HOUR_OF_DAY) ||
                    (selectedHour == now.get(Calendar.HOUR_OF_DAY) && selectedMinute <= now.get(Calendar.MINUTE))) {
                eventTimeLayout.setError(getString(R.string.error_time_past));
                return;
            }
        }

        int maxAttendees;
        try {
            maxAttendees = Integer.parseInt(maxAttendeesStr);
        } catch (NumberFormatException e) {
            eventMaxAttendeesLayout.setError(getString(R.string.error_attendees_invalid));
            return;
        }

        if (maxAttendees < 2) {
            eventMaxAttendeesLayout.setError(getString(R.string.error_attendees_min));
            return;
        }

        if (maxAttendees > 10000) {
            eventMaxAttendeesLayout.setError(getString(R.string.error_attendees_max));
            return;
        }
        String selectedTagsString = String.join(",", selectedTags);
        EventEntity event = new EventEntity(title, description, city, date, time, location, maxAttendees, false, selectedTagsString);
        AppDatabase.getInstance(this).eventDao().insert(event);

        Toast.makeText(this, getString(R.string.event_created_success), Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isDateInPast(String date) {
        try {
            String[] parts = date.split("-");
            Calendar selected = Calendar.getInstance();
            selected.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]), 0, 0, 0);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            return selected.before(today);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isDateToday(String date) {
        try {
            String[] parts = date.split("-");
            Calendar selected = Calendar.getInstance();
            selected.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            Calendar today = Calendar.getInstance();
            return selected.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && selected.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && selected.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            return false;
        }
    }

    private void showTagSelectionDialog() {
        String[] tagsArray = availableTags.toArray(new String[0]);
        boolean[] checkedItems = new boolean[tagsArray.length];

        for (int i = 0; i < tagsArray.length; i++) {
            checkedItems[i] = selectedTags.contains(tagsArray[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_interest_tags_title)
                .setMultiChoiceItems(tagsArray, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedTags.contains(tagsArray[which])) {
                            selectedTags.add(tagsArray[which]);
                        }
                    } else {
                        selectedTags.remove(tagsArray[which]);
                    }
                })
                .setPositiveButton(R.string.ok, null)
                .show();
    }
    private void seedDefaultTagsIfNeeded() {
        if (db.interestTagDao().getAllTags().isEmpty()) {
            for (String tag : getResources().getStringArray(R.array.default_interest_tags)) {
                db.interestTagDao().insertTag(new InterestTagEntity(tag));
            }
        }
    }
    private void loadAvailableTagsFromDb() {
        availableTags.clear();
        List<InterestTagEntity> tags = AppDatabase.getInstance(this).interestTagDao().getAllTags();
        for (InterestTagEntity tag : tags) {
            availableTags.add(tag.name);
        }
    }
}
