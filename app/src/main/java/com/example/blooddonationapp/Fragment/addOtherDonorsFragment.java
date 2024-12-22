package com.example.blooddonationapp.Fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blooddonationapp.Adapter.DonorListAdapter;
import com.example.blooddonationapp.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class addOtherDonorsFragment extends DialogFragment {

    private EditText donorEmailEditText;
    private Button addDonorButton;
    private RecyclerView donorListRecyclerView;
    private String siteId;

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_other_donors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        donorEmailEditText = view.findViewById(R.id.donorEmailEditText);
        addDonorButton = view.findViewById(R.id.addDonorButton);
        donorListRecyclerView = view.findViewById(R.id.donorListRecyclerView);

        firestore = FirebaseFirestore.getInstance();

        // Get site ID from arguments
        siteId = requireArguments().getString("siteId");

        // Initialize RecyclerView
        donorListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDonorList();

        addDonorButton.setOnClickListener(v -> {
            String email = donorEmailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                addDonorToSite(email);
            } else {
                Toast.makeText(getContext(), "Please enter an email address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set dialog width and height
        Dialog dialog = getDialog();
        if (dialog != null) {
            // You can adjust these values as per your design requirements
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void loadDonorList() {
        firestore.collection("donation_sites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> donorEmails = (List<String>) documentSnapshot.get("donorEmails");
                    if (donorEmails == null) {
                        donorEmails = new ArrayList<>(); // Avoid null issues
                    }
                    DonorListAdapter adapter = new DonorListAdapter(donorEmails);
                    donorListRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load donors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addDonorToSite(String email) {
        firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String donorId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // Add the donor ID to the donor list of the donation_sites document
                        firestore.collection("donation_sites").document(siteId)
                                .update("donors", FieldValue.arrayUnion(donorId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Donor added successfully!", Toast.LENGTH_SHORT).show();
                                    donorEmailEditText.setText("");
                                    loadDonorList();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to add donor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "Donor with this email does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching donor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
