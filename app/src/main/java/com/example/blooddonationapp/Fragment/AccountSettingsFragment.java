package com.example.blooddonationapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.blooddonationapp.LoginActivity;
import com.example.blooddonationapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountSettingsFragment extends Fragment {

    private TextView greetingText;
    private Button logoutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        greetingText = view.findViewById(R.id.greetingText);
        logoutButton = view.findViewById(R.id.logoutButton);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get user's name from Firestore
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String accountName = documentSnapshot.getString("name");
                        greetingText.setText("Hello, " + accountName + "!");
                    } else {
                        greetingText.setText("Hello, User!");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                });

        // Logout button logic
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
