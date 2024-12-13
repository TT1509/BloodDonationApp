package com.example.blooddonationapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blooddonationapp.Model.Donor;
import com.example.blooddonationapp.Model.SiteManager;
import com.example.blooddonationapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private Spinner bloodTypeSpinner;
    private EditText nameInput, emailInput, passwordInput, phoneNumberInput;
    private RadioGroup roleGroup;
    private RadioButton donorRadio, siteManagerRadio;
    private Button signupButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize fields
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneNumberInput = findViewById(R.id.phoneNumberInput); // Added phoneNumber
        bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner);
        roleGroup = findViewById(R.id.roleGroup);
        donorRadio = findViewById(R.id.donorRadio);
        siteManagerRadio = findViewById(R.id.siteManagerRadio);
        signupButton = findViewById(R.id.signupButton);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Listen for role selection changes
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.donorRadio) {
                bloodTypeSpinner.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.siteManagerRadio) {
                bloodTypeSpinner.setVisibility(View.GONE);
            }
        });

        // Handle signup button click
        signupButton.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phoneNumberStr = phoneNumberInput.getText().toString().trim(); // Phone number input as string
        String role = donorRadio.isChecked() ? "donor" : siteManagerRadio.isChecked() ? "site_manager" : null;

        // Validate common fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phoneNumberStr) || role == null) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse phone number
        Integer phoneNumber;
        try {
            phoneNumber = Integer.parseInt(phoneNumberStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle specialized attributes
        if ("donor".equals(role)) {
            Spinner bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner);
            String bloodType = bloodTypeSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(bloodType)) {
                Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
                return;
            }

            createUserInFirebase(new Donor(name, email, phoneNumber, role, bloodType, 0), email, password);
        } else if ("site_manager".equals(role)) {
            createUserInFirebase(new SiteManager(name, email, phoneNumber, role), email, password);
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
