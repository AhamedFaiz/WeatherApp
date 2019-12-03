package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    TextView tempTextView;
    TextView humidityTextView;
    TextView weatherTextView;
    TextView cityTextView;
    ImageView iconImageView;
    EditText editText;
    String weather = null;
    double temperature;
    int humidity;
    String icon = null;
    Bitmap iconImage;
    String location = null;

    public void setButton(View view) {
        /*Setting what happens when user enters the place and the button is clicked */


        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);//To hide the keyboard after typing


        location = editText.getText().toString();// Getting the content user has entered
        editText.getText().clear();

        //Calling the function to get the JASON data from API
        GetWebContent webContent = new GetWebContent();
        webContent.execute
                ("https://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=0d66efb8c3a74fbb8ac7f913c313ca24");
    }

    public class GetWebContent extends AsyncTask<String, Void, String> {

        /* Class to download the Data from any link passed to it that does the work in Background */

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Getting the Data from Website

                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                for (String line; (line = bufferedReader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }
                return builder.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            // Inbuilt Method in AsyncTask to work on the data without going to UI Thread

            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    // Getting Weather Description from JSON data

                    String weatherData = jsonObject.getString("weather");
                    JSONArray arr = new JSONArray(weatherData);
                    JSONObject jsonPart = arr.getJSONObject(0);
                    weather = jsonPart.getString("description");
                    icon = jsonPart.getString("icon");


                    //Getting Temperature and Humidity From JSON data

                    JSONObject jsonObject1 = new JSONObject(result);
                    jsonObject1 = jsonObject1.getJSONObject("main");
                    temperature = jsonObject1.getDouble("temp");
                    humidity = jsonObject1.getInt("humidity");

                    //Downloading Icon to be displayed with weather data

                    DownloadImage downloadImage = new DownloadImage();
                    iconImage = downloadImage.execute("http://openweathermap.org/img/w/" + icon + ".png").get();

                    // Displaying all the Weather data using ImageView and TextView

                    iconImageView.setImageBitmap(iconImage);
                    weatherTextView.setText(weather);
                    tempTextView.setText(String.format("%.0f", (temperature - 273.15)) + "\u2103");
                    humidityTextView.setText("Humidity : " + humidity);
                    cityTextView.setText(location.toUpperCase());


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Invalid City", Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Invalid City", Toast.LENGTH_LONG).show();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Invalid City", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Invalid City", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing all the UI elements on startup
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        humidityTextView = findViewById(R.id.humidityTextView);
        weatherTextView = findViewById(R.id.weatherTextView);
        tempTextView = findViewById(R.id.tempTextView);
        iconImageView = findViewById(R.id.iconImageView);
        cityTextView = findViewById(R.id.cityTextView);

    }

    protected class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        // Another Async Task to Download the Image

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
