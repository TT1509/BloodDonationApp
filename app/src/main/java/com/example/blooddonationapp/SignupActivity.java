package com.example.blooddonationapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blooddonationapp.Model.Donor;
import com.example.blooddonationapp.Model.SiteManager;
import com.example.blooddonationapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    private Spinner bloodTypeSpinner;
    private EditText nameInput, emailInput, passwordInput, phoneNumberInput, heightInput, weightInput;
    private RadioGroup roleGroup, genderGroup;
    private RadioButton donorRadio, siteManagerRadio, maleRadio, femaleRadio;
    private Button signupButton, dobButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private Calendar calendar;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize fields
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        dobButton = findViewById(R.id.dobButton);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner);
        roleGroup = findViewById(R.id.roleGroup);
        donorRadio = findViewById(R.id.donorRadio);
        siteManagerRadio = findViewById(R.id.siteManagerRadio);
        genderGroup = findViewById(R.id.genderGroup);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);
        signupButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Set up DatePicker for date of birth
        dobButton.setOnClickListener(v -> showDatePickerDialog());

        // Listen for role selection changes
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.donorRadio) {
                bloodTypeSpinner.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.siteManagerRadio) {
                bloodTypeSpinner.setVisibility(View.GONE);
            }
        });
        signupButton.setOnClickListener(v -> signUpUser());
        loginLink.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDobInput();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void updateDobInput() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dobButton.setText(dateFormat.format(calendar.getTime()));
    }

    private void signUpUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phoneNumberStr = phoneNumberInput.getText().toString().trim(); // Phone number input as string
        String dob = dobButton.getText().toString().trim();
        String gender = maleRadio.isChecked() ? "Male" : femaleRadio.isChecked() ? "Female" : null;
        String role = donorRadio.isChecked() ? "donor" : siteManagerRadio.isChecked() ? "site_manager" : null;
        String heightStr = heightInput.getText().toString().trim();
        String weightStr = weightInput.getText().toString().trim();

        // Validate common fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phoneNumberStr) || TextUtils.isEmpty(dob) || gender == null || role == null) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate age
        Calendar today = Calendar.getInstance();
        Calendar dobCalendar = Calendar.getInstance();
        dobCalendar.setTime(calendar.getTime());

        int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

        // Adjust age if the birthday hasn't occurred this year yet
        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        if (age < 18 || age > 65) {
            Toast.makeText(this, "Your age must be higher than 18 years old and below 65 years old.", Toast.LENGTH_SHORT).show();
            return;
        }

        double height = 0;
        double weight = 0;

        if ("donor".equals(role)) {
            if (TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
                Toast.makeText(this, "Please enter your height and weight", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                height = Double.parseDouble(heightStr);
                weight = Double.parseDouble(weightStr);

                if (height <= 0 || height > 300) {
                    Toast.makeText(this, "Height must be below 300 cm", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (weight < 50) {
                    Toast.makeText(this, "Weight must be above 50 kg", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid height or weight value", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Parse phone number
        Integer phoneNumber;
        try {
            phoneNumber = Integer.parseInt(phoneNumberStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("donor".equals(role)) {
            String bloodType = bloodTypeSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(bloodType)) {
                Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
                return;
            }

            createUserInFirebase(new Donor(name, email, phoneNumber, role, calendar.getTime(), gender, bloodType, 0, height, weight), email, password);
        } else if ("site_manager".equals(role)) {
            createUserInFirebase(new SiteManager(name, email, phoneNumber, role, calendar.getTime(), gender), email, password);
        }
    }

    private void createUserInFirebase(User user, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
