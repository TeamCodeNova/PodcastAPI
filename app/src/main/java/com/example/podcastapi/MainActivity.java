package com.example.podcastapi;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "69a27af18f0848d6968210073133948e";
    private static final String API_URL = "https://listen-api.listennotes.com/api/v2/search?q=SEARCH_QUERY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            String response = fetchPodcasts("Technology");
            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    StringBuilder podcastTitles = new StringBuilder();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject podcastObject = jsonArray.getJSONObject(i);
                        String title = podcastObject.getString("title_original");
                        podcastTitles.append(title).append("\n");
                    }

                    TextView textView = findViewById(R.id.textView);
                    textView.setText(podcastTitles.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }


    private String fetchPodcasts(String searchQuery) {
        String response = "";

        try {
            URL url = new URL(API_URL.replace("SEARCH_QUERY", searchQuery));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-ListenAPI-Key", API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}