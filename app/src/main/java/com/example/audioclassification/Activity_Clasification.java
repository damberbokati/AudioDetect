package com.example.audioclassification;

import android.content.Intent;
import android.media.AudioRecord;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.TimerTask;

public class Activity_Clasification extends AudioHelper {
    String modelPath = "my_birds_model.tflite";
    FirebaseAuth auth;
    FirebaseUser user;
    float probabilityThreshold = 0.3f;
    AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private TimerTask timerTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    public void onStartRecording(View view) {
        super.onStartRecording(view);

        // Loading the model from the assets folder
        try {
            classifier = AudioClassifier.createFromFile(this, modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating an audio recorder
        tensor = classifier.createInputTensorAudio();

        // Showing the audio recorder specification
        TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n"
                + "Sample Rate: " + format.getSampleRate();
        //specsTextView.setText(specs);

        // Creating and starting recording
        record = classifier.createAudioRecord();
        record.startRecording();
    }

    public void onStopRecording(View view) {
        super.onStopRecording(view);

        // Stopping the recording
        record.stop();

        // Load the recorded audio and classify
        int numberOfSamples = tensor.load(record);
        Log.d("MyApp", "Number of samples loaded: " + numberOfSamples);
        List<Classifications> output = classifier.classify(tensor);

        // Flatten and filter out classifications with low probability
        List<Category> finalOutput = new ArrayList<>();
        for (Classifications classifications : output) {
            for (Category category : classifications.getCategories()) {
                Log.d("MyApp", "Label: " + category.getLabel() + ", Score: " + category.getScore());
                if (category.getScore() > probabilityThreshold) {
                    finalOutput.add(category);
                }
            }
        }

        // Sort the results by score in descending order
        Collections.sort(finalOutput, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));

        // Take the top 3 classifications
        List<Category> top3 = finalOutput.subList(0, Math.min(1, finalOutput.size()));

        // Creating a multiline string with the filtered results
        StringBuilder outputStr = new StringBuilder();
        for (Category category : top3) {
            outputStr.append(category.getLabel())
                    .append(": ").append(category.getScore())
                    .append(", ").append(category.getDisplayName()).append("\n");
        }

        // Updating the UI
        runOnUiThread(() -> {
            if (top3.isEmpty()) {
                outputTextView.setText("Could not identify the bird");
            } else {
                outputTextView.setText(outputStr.toString());
            }
        });
    }
}
