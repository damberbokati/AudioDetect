package com.example.audioclassification;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ModelInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Set the content view to your XML layout
        setContentView(R.layout.app_information);

        // Additional code for your activity can be added here
    }
}
