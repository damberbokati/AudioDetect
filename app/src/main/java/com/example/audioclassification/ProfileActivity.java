package com.example.audioclassification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.provider.MediaStore;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;


public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button logoutButton, updateInfoButton;
    TextView textView, firstNameTextView, lastNameTextView,UTAidTextView;
    FirebaseUser user;
    FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ImageView profilePictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        logoutButton = findViewById(R.id.logout);
        updateInfoButton = findViewById(R.id.updateUserInfo);
        textView = findViewById(R.id.Email);
        firstNameTextView = findViewById(R.id.first_name);
        lastNameTextView = findViewById(R.id.last_name);
        UTAidTextView = findViewById(R.id.UTAid);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        profilePictureImageView = findViewById(R.id.profile_picture);
        Button updateProfilePictureButton = findViewById(R.id.updateProfilePictureButton);
        // Set a click listener for the update profile picture button
        updateProfilePictureButton.setOnClickListener(v -> {
            // Open image gallery for image selection
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText("Email: " + user.getEmail());
            fetchAndDisplayUserDetails(user.getUid());
            loadProfilePicture(user.getUid());
        }

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        updateInfoButton.setOnClickListener(v -> {
            showUpdateInfoDialog();
        });
    }

    private void fetchAndDisplayUserDetails(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String utaId = documentSnapshot.getString("UTAId");

                        firstNameTextView.setText("First Name: " + firstName);
                        lastNameTextView.setText("Last Name: " + lastName);
                        UTAidTextView.setText("UTAID: " + utaId);

                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showUpdateInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update User Info");

        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.update_info, null);
        builder.setView(dialogView);

        EditText firstNameInput = dialogView.findViewById(R.id.editTextFirstName);
        EditText lastNameInput = dialogView.findViewById(R.id.editTextLastName);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFirstName = firstNameInput.getText().toString().trim();
                String newLastName = lastNameInput.getText().toString().trim();

                // Check if names are not empty before updating
                if (!newFirstName.isEmpty() && !newLastName.isEmpty()) {
                    updateUserInfo(user.getUid(), newFirstName, newLastName);
                } else {
                    Toast.makeText(ProfileActivity.this, "Please enter both first and last names", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateUserInfo(String userId, String newFirstName, String newLastName) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("firstName", newFirstName, "lastName", newLastName)
                .addOnSuccessListener(aVoid -> {
                    // User information updated successfully
                    Toast.makeText(ProfileActivity.this, "User information updated", Toast.LENGTH_SHORT).show();
                    // After updating, you may want to refresh the displayed information
                    fetchAndDisplayUserDetails(userId);
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(ProfileActivity.this, "Failed to update user information", Toast.LENGTH_SHORT).show();
                });
    }
    // Override onActivityResult to handle the result of image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            Uri selectedImageUri = data.getData();

            // Upload the selected image to Firebase Storage
            uploadProfilePicture(selectedImageUri);
        }
    }

    // Add a method to upload the selected image to Firebase Storage
// Add a method to upload the selected image to Firebase Storage
    private void uploadProfilePicture(Uri imageUri) {
        // Create a reference to the user's profile picture in Firebase Storage
        StorageReference profilePictureRef = storageReference.child("profilePictures/" + user.getUid() + ".jpg");

        // Upload the file to Firebase Storage
        profilePictureRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload successful
                    Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();

                    // After updating the profile picture, load and display it
                    loadProfilePicture(user.getUid());
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(ProfileActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                });
    }

    // Add a method to load and display the profile picture
    private void loadProfilePicture(String userId) {
        StorageReference profilePictureRef = storageReference.child("profilePictures/" + userId + ".jpg");

        profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load and display the profile picture using a library like Picasso or Glide
            // For example, using Picasso:
            Picasso.get().load(uri).into(profilePictureImageView);
        }).addOnFailureListener(e -> {
            // Handle the error
            Toast.makeText(ProfileActivity.this, "Failed to load profile picture", Toast.LENGTH_SHORT).show();
        });
    }


}
