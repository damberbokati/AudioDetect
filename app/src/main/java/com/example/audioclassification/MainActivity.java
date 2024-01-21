package com.example.audioclassification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button Audio_button,profile_button,modelinf_button;
    TextView textView, firstNameTextView, lastNameTextView;
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.main_activity);  // Correct the layout file name to "activity_main"
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Audio_button = findViewById(R.id.classify_audio);
        profile_button = findViewById(R.id.profile);
        modelinf_button = findViewById(R.id.appinfo);
        textView = findViewById(R.id.welcomemessage);
        firstNameTextView = findViewById(R.id.first_name);

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        Button classifyAudioButton = findViewById(R.id.classify_audio); // Assuming the button's ID is button1
        classifyAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity_classification activity when the button is clicked
                Intent intent = new Intent(MainActivity.this, Activity_Clasification.class);
                startActivity(intent);
            }
        });

        Button profileButton = findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity_profile activity when the "Profile" button is clicked
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        Button classificationButton = findViewById(R.id.appinfo);
        classificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity_profile activity when the "Profile" button is clicked
                Intent intent = new Intent(MainActivity.this, ModelInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchAndDisplayUserDetails(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        // Display the first name and last name
                        firstNameTextView.setText("First Name: " + firstName);
                    } else {
                        // Handle the case where user data doesn't exist in Firestore
                        Toast.makeText(MainActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors in fetching user data
                    Toast.makeText(MainActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                });
    }
}
