package com.example.blooddonationapp.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FirestoreUtils {
    private static final String TAG = "FirestoreUtils";

    /**
     *
     *
     * @param context
     * @param callback
     */
    public static void loadDonorBloodType(Context context, Consumer<String> callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String donorBloodType = documentSnapshot.getString("bloodType");
                        if (donorBloodType != null) {
                            callback.accept(donorBloodType);
                        } else {
                            showToast(context, "Blood type not found. Please update your profile.");
                        }
                    } else {
                        showToast(context, "User document not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast(context, "Failed to retrieve blood type: " + e.getMessage());
                    Log.e(TAG, "Error fetching donor blood type", e);
                });
    }

    /**
     *
     *
     * @param context
     * @param callback
     */
    public static void loadDonationSites(Context context, BiConsumer<List<DonationSite>, List<String>> callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("donation_sites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<DonationSite> siteList = new ArrayList<>();
                        List<String> siteIds = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            DonationSite site = doc.toObject(DonationSite.class);
                            if (site != null) {
                                siteIds.add(doc.getId());
                                siteList.add(site);
                            }
                        }

                        callback.accept(siteList, siteIds);
                    } else {
                        showToast(context, "No donation sites found.");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast(context, "Failed to load sites: " + e.getMessage());
                    Log.e(TAG, "Error loading sites", e);
                });
    }

    /**
     *
     *
     * @param context
     * @param message
     */
    private static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public interface ManagerDetailsCallback {
        void onSuccess(User manager);
        void onFailure(String errorMessage);
    }

    public static void fetchManagerDetails(String managerId, ManagerDetailsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(managerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            User manager = document.toObject(User.class);
                            if (manager != null) {
                                callback.onSuccess(manager);
                            } else {
                                callback.onFailure("Manager data is null");
                            }
                        } else {
                            callback.onFailure("Manager document does not exist");
                        }
                    } else {
                        callback.onFailure("Error fetching manager details: " + (task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error"));
                    }
                });
    }

}

