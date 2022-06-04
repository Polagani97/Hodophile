package com.example.hodophile.ui.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.annotations.NotNull;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherFragment extends Fragment {

    final String API = "798183b24b356e657baaff7ec8bfc4c6";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    final String WEATHER_URL_5_DAYS = "https://api.openweathermap.org/data/2.5/onecall";

    private TextView cityName, weatherState, temperature;
    private ImageView weatherIcon;
    private RecyclerView weather5Days;
    private ProgressBar progressBar;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button cityFinder;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        weatherState = view.findViewById(R.id.weather_condition);
        temperature = view.findViewById(R.id.temperature);
        weatherIcon = view.findViewById(R.id.weather_icon);
        cityFinder = view.findViewById(R.id.changeCityButton);
        cityName = view.findViewById(R.id.city_name);
        weather5Days = view.findViewById(R.id.lvDailyWeather);
        progressBar = view.findViewById(R.id.weatherProgressBar);

        getWeatherForCurrentLocation();

        cityFinder.setOnClickListener(v -> {
            dialogBuilder = new AlertDialog.Builder(getContext());
            View popupView = getLayoutInflater().inflate(R.layout.city_search_popup, null);
            dialogBuilder.setView(popupView);

            ImageButton closeButton = popupView.findViewById(R.id.cityCloseButton);
            EditText changeCity = popupView.findViewById(R.id.changed_city_name);
            Button changeButton = popupView.findViewById(R.id.changeButton);

            dialog = dialogBuilder.create();
            dialog.show();

            changeButton.setOnClickListener(v1 -> {
                String newCity = changeCity.getText().toString();
                if (newCity.isEmpty()) {
                    changeCity.setError("Please enter a city");
                    changeCity.requestFocus();
                } else {
                    getWeatherForNewCity(newCity);
                    dialog.dismiss();
                }
            });

            closeButton.setOnClickListener(t -> dialog.dismiss());
        });
    }

    private void getWeatherForNewCity(String city) {
        weatherState.setVisibility(View.INVISIBLE);
        temperature.setVisibility(View.INVISIBLE);
        weatherIcon.setVisibility(View.INVISIBLE);
        cityName.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        weather5Days.setVisibility(View.INVISIBLE);
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", API);
        doNetworking(params);
    }

    private void getWeatherForCurrentLocation() {
        weatherState.setVisibility(View.INVISIBLE);
        temperature.setVisibility(View.INVISIBLE);
        weatherIcon.setVisibility(View.INVISIBLE);
        cityName.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    result -> {
                        if(result) {
                            getCurrentLocation();
                        } else {
                            Toast.makeText(getContext(),"Permission Denied",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            mPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void  getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
         locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
                    RequestParams params = new RequestParams();
                    params.put("lat", latitude);
                    params.put("lon", longitude);
                    params.put("appid", API);
                    doNetworking(params);
                } else {
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(1000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setNumUpdates(1);

                    LocationCallback callback = new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            Location location1 = locationResult.getLastLocation();
                            String latitude = String.valueOf(location1.getLatitude());
                            String longitude = String.valueOf(location1.getLongitude());
                            RequestParams params = new RequestParams();
                            params.put("lat", latitude);
                            params.put("lon", longitude);
                            params.put("appid", API);
                            doNetworking(params);
                        }
                    };
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper());
                }
            });
         } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private void doNetworking(RequestParams params) {
        AsyncHttpClient client1 = new AsyncHttpClient();
        client1.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    String city = response.getString("name");
                    int condition = response.getJSONArray("weather").getJSONObject(0).getInt("id");
                    String weatherType = response.getJSONArray("weather").getJSONObject(0).getString("main");
                    String icon = updateWeatherIcon(condition);
                    double temp = response.getJSONObject("main").getDouble("temp") - 273.15;
                    int roundedValue = (int) Math.rint(temp);
                    String t = Integer.toString(roundedValue);
                    temperature.setText(String.format("%s°C", t));
                    cityName.setText(city);
                    weatherState.setText(weatherType);
                    int resourceID = getResources().getIdentifier(icon, "drawable", getActivity().getPackageName());
                    weatherIcon.setImageResource(resourceID);

                    RequestParams params = new RequestParams();
                    params.put("lat", response.getJSONObject("coord").getString("lat"));
                    params.put("lon", response.getJSONObject("coord").getString("lon"));
                    params.put("appid", API);
                    client1.get(WEATHER_URL_5_DAYS, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                            List<com.example.hodophile.ui.weather.WeatherItem> forecast = getForecast(response);
                            WeatherAdapter weatherAdapter = new WeatherAdapter(getContext(), forecast);
                            weather5Days.setHasFixedSize(true);
                            weather5Days.setLayoutManager(new LinearLayoutManager(getContext()));
                            weather5Days.setAdapter(weatherAdapter);
                            super.onSuccess(statusCode, headers, response);
                            temperature.setVisibility(View.VISIBLE);
                            cityFinder.setVisibility(View.VISIBLE);
                            cityName.setVisibility(View.VISIBLE);
                            weatherState.setVisibility(View.VISIBLE);
                            weatherIcon.setVisibility(View.VISIBLE);
                            weather5Days.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            Toast.makeText(getContext(), "Unable to obtain weather data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Unable to obtain weather data",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getContext(), "Unable to obtain weather data",
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private static String updateWeatherIcon(int condition) {
        if (condition >= 0 && condition <= 300 || condition >= 900 && condition <= 902 || condition >= 905 && condition <= 1000) {
            return "weather_thunder";
        } else if (condition >= 300 && condition <= 500) {
            return "weather_light_rain";
        } else if (condition >= 500 && condition <= 600) {
            return "weather_heavy_rain";
        } else if (condition >= 600 && condition <= 700) {
            return "weather_snow";
        } else if (condition >= 701 && condition <= 771) {
            return "weather_fog";
        } else if (condition >= 772 && condition < 800 || condition >= 801 && condition <= 804) {
            return "weather_cloudy";
        } else if (condition == 800 || condition == 904) {
            return "weather_sunny";
        } else if (condition == 903) {
            return "weather_snow";
        } else {
            return "No matching weather";
        }
    }

    private static String timestampToDate(long timestamp) {
        Date date = new Date(timestamp * 1000);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy");
        return formatter.format(date);
    }

    public List<com.example.hodophile.ui.weather.WeatherItem> getForecast(JSONObject jsonObject) {
        try {
            List<com.example.hodophile.ui.weather.WeatherItem> forecast = new ArrayList<>();
            Log.d("Track", "retrieving info");
            for (int i = 0; i < 5; i++) {
                JSONObject weather = jsonObject.getJSONArray("daily").getJSONObject(i);
                String date = timestampToDate(weather.getLong("dt"));
                int condition = weather.getJSONArray("weather").getJSONObject(0).getInt("id");
                String weatherType = weather.getJSONArray("weather").getJSONObject(0).getString("main");
                String icon = updateWeatherIcon(condition);
                double temp = weather.getJSONObject("temp").getDouble("day") - 273.15;
                int roundedValue = (int) Math.rint(temp);
                forecast.add(new com.example.hodophile.ui.weather.WeatherItem(icon, Integer.toString(roundedValue), weatherType, condition, date));
            }
            return forecast;
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Unable to obtain weather data",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
