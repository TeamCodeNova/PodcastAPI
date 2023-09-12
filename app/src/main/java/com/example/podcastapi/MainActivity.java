package com.example.podcastapi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TaddyPodcastAPI";
    private static final String API_URL = "https://api.taddy.org";
    private static final String X_USER_ID = "599";
    private static final String X_API_KEY = "bfc65097269c742516dad0f0f2da4d4e1958eda6cadabc576d44999f512012649e41897fbb21449decf977c1d4f1b14663"; // Replace with your actual API key
    private TextView podcastNameTextView;
    private EditText podcastQueryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views by their IDs
        Button searchButton = findViewById(R.id.searchButton);
        podcastNameTextView = findViewById(R.id.podcastNameTextView);
        podcastQueryEditText = findViewById(R.id.podcastQueryEditText);

        // Set the click listener for the searchButton
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchPodcastData();
            }
        });
    }

    public void onSearchClick(View view) {
        fetchPodcastData();
    }

    public void fetchPodcastData() {
        // Get the user's podcast query from the EditText field
        String podcastQuery = podcastQueryEditText.getText().toString();

        // Trigger the FetchPodcastDataTask with the user's query
        new FetchPodcastDataTask().execute(podcastQuery);
    }

    private class FetchPodcastDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length != 1) {
                return null; // Return early if no query provided
            }

            String podcastQuery = params[0];

            try {
                // Create the API URL
                URL url = new URL(API_URL);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("X-USER-ID", X_USER_ID);
                connection.setRequestProperty("X-API-KEY", X_API_KEY);
                connection.setDoOutput(true);

                // Construct the JSON request body with the user's query
                String requestBody = "{ \"query\": \"{ searchForTerm(term:\\\"" + podcastQuery + "\\\", filterForTypes:PODCASTSERIES){ searchId podcastSeries{ uuid name rssUrl } } }\" }";

                // Write the JSON request to the connection's output stream
                try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                    outputStream.writeBytes(requestBody);
                    outputStream.flush();
                }

                // Get the response from the server
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    Log.e(TAG, "API request failed with response code: " + responseCode);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error making API request: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                try {
                    // Parse the JSON response
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    // Process the data as needed (e.g., extract podcast information)
                    JSONArray podcastArray = jsonObject.getJSONObject("data")
                            .getJSONObject("searchForTerm")
                            .getJSONArray("podcastSeries");

                    List<String> podcastNames = new ArrayList<>();

                    for (int i = 0; i < podcastArray.length(); i++) {
                        JSONObject podcastObject = podcastArray.getJSONObject(i);
                        String podcastName = podcastObject.getString("name");
                        podcastNames.add(podcastName);
                    }

                    // Use the extracted data in your app
                    displayPodcasts(podcastNames);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage(), e);
                }
            } else {
                Log.e(TAG, "API response is null.");
            }
        }
    }

    private void displayPodcasts(List<String> podcastNames) {
        StringBuilder names = new StringBuilder();
        for (String name : podcastNames) {
            names.append("Podcast Name: ").append(name).append("\n");
        }
        podcastNameTextView.setText(names.toString());
    }
}
