import os
import numpy as np
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
import librosa
import pandas as pd
from tensorflow.keras.models import load_model
from sklearn.preprocessing import LabelEncoder

def extract_features(file_name):
    try:
        audio, sample_rate = librosa.load(file_name, res_type='kaiser_fast')
        mfccs = librosa.feature.mfcc(y=audio, sr=sample_rate, n_mfcc=40)
        mfccs_processed = np.mean(mfccs.T, axis=0)
    except Exception as e:
        print(f"Error encountered while parsing file: {file_name}, Error: {e}")
        return None
    return mfccs_processed

def classify_audio(file_path, model, label_encoder):
    features = extract_features(file_path)

    if features is None:
        print(f"Failed to extract features for {file_path}.")
        return "Unknown"
    features = np.array([features])
    predicted_vector = model.predict(features)

    # Extract top 5 predictions
    top_5_indices = predicted_vector[0].argsort()[-5:][::-1]
    top_5_predictions = label_encoder.inverse_transform(top_5_indices)
    top_5_probabilities = predicted_vector[0][top_5_indices]

    for label, prob in zip(top_5_predictions, top_5_probabilities):
        print(f"Class: {label}, Confidence: {prob*100:.2f}%")

    predicted_class = np.argmax(predicted_vector, axis=1)
    predicted_label = label_encoder.inverse_transform(predicted_class)
    return predicted_label[0]

# Set the root directory as the current working directory
ROOT_DIRECTORY = os.path.dirname(os.path.abspath(__file__))
os.chdir(ROOT_DIRECTORY)

# Load the trained model
model_path = os.path.join(ROOT_DIRECTORY, "sound_classifier_model.h5")
model = load_model(model_path)

# Extract unique labels from Excel file
EXCEL_PATH = os.path.join(ROOT_DIRECTORY, "metadata", "UrbanSound8K.csv")
df = pd.read_csv(EXCEL_PATH)
unique_labels = df['class'].unique()

# Fit the label encoder with the unique labels from Excel
label_encoder = LabelEncoder()
label_encoder.fit(unique_labels)

# Classify a new audio file
audio_file_path = os.path.join(ROOT_DIRECTORY, "sound", "81722-3-0-21.wav")
label = classify_audio(audio_file_path, model, label_encoder)
print(f"\nThe audio file {audio_file_path} is most likely a '{label}' sound.")
