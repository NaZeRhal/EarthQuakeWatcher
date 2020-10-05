package com.maxrzhe.earthquakewatcher;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maxrzhe.earthquakewatcher.model.City;
import com.maxrzhe.earthquakewatcher.model.EarthQuake;
import com.maxrzhe.earthquakewatcher.ui.CustomInfoWindow;
import com.maxrzhe.earthquakewatcher.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final String TAG = "MAIN_LOG";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;

    private RequestQueue requestQueue;
    private LatLng currentInfoPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestQueue = Volley.newRequestQueue(this);
        getEarthQuakes();
    }

    private void getEarthQuakes() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Constants.URL,
                null,
                response -> {
                    try {
                        JSONArray features = response.getJSONArray(Constants.FEATURES_KEY);
                        for (int i = 0; i < features.length(); i++) {
                            JSONObject earthQuakeInfoJSONObject = features.getJSONObject(i);
                            JSONObject properties = earthQuakeInfoJSONObject.getJSONObject(Constants.PROPERTIES_KEY);
                            JSONArray coordinates = earthQuakeInfoJSONObject.getJSONObject(Constants.GEOMETRY_KEY).getJSONArray(Constants.COORDINATES_KEY);

                            String place = properties.getString("place");
                            double magnitude = properties.getDouble("mag");
                            long time = properties.getLong("time");
                            String detailLink = properties.getString("detail");
                            String type = properties.getString("type");

                            double lon = coordinates.getDouble(0);
                            double lat = coordinates.getDouble(1);

                            EarthQuake earthQuake = new EarthQuake(place, magnitude, time, detailLink, type, lat, lon);
                            Log.d(TAG, "Earthquake " + i + " -> " + earthQuake);

                            MarkerOptions markerOptions = getMarkerOptions(earthQuake);

                            if (magnitude > 5) {
                                CircleOptions circleOptions = getCircleOptions(earthQuake);
                                mMap.addCircle(circleOptions);
                            }

                            mMap.addMarker(markerOptions);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    Log.e(TAG, "getEarthQuakes: ", error);
                });
        requestQueue.add(jsonObjectRequest);
    }

    private CircleOptions getCircleOptions(EarthQuake earthQuake) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(earthQuake.getLat(), earthQuake.getLon()));
        circleOptions.radius(30000);
        circleOptions.strokeWidth(3.6f);
        circleOptions.strokeColor(Color.RED);
//        circleOptions.fillColor(Color.alpha(Color.RED));
        return circleOptions;
    }

    private MarkerOptions getMarkerOptions(EarthQuake earthQuake) {
        MarkerOptions markerOptions = new MarkerOptions();
        float markerColor;
        double magnitude = earthQuake.getMagnitude();
        if (magnitude <= 4) {
            markerColor = BitmapDescriptorFactory.HUE_GREEN;
        } else if (magnitude <= 5) {
            markerColor = BitmapDescriptorFactory.HUE_ORANGE;
        } else {
            markerColor = BitmapDescriptorFactory.HUE_RED;
        }

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(markerColor));
        markerOptions.title(earthQuake.getPlace());
        markerOptions.position(new LatLng(earthQuake.getLat(), earthQuake.getLon()));

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String formattedDate = dateFormat.format(new Date(earthQuake.getTime()));
        Log.d(TAG, "onResponse: date " + formattedDate);


        markerOptions.snippet("Magnitude: " + magnitude + "\n" +
                "Date: " + formattedDate);
        return markerOptions;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        } else {
            //we have permission
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng lastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(lastLocationLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("I'm here!"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng, 8));
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        currentInfoPosition = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        String urlDetails = Constants.buildUrlPlaceDetails(marker.getPosition().latitude, marker.getPosition().longitude, 100, 10, 10000);
        getQuakeDetails(urlDetails);
        Log.d(TAG, "onInfoWindowClick: " + urlDetails);
    }

    private void getQuakeDetails(String url) {
        if (url != null) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        List<City> cityList = new ArrayList<>();
                        try {
                            JSONArray featuresArray = response.getJSONObject(Constants.GEONAMES_KEY).getJSONArray(Constants.FEATURES_KEY);
                            for (int i = 0; i < featuresArray.length(); i++) {
                                JSONObject cityProperties = featuresArray.getJSONObject(i).getJSONObject(Constants.PROPERTIES_KEY);
                                JSONArray cityCoordinates = featuresArray.getJSONObject(i).getJSONObject(Constants.GEOMETRY_KEY).getJSONArray(Constants.COORDINATES_KEY);
                                City city = new City();
                                city.setName(cityProperties.getString("name"));
                                city.setCountryName(cityProperties.getString("country_name"));
                                city.setDistanceFromDisaster(cityProperties.getDouble("distance"));
                                city.setPopulation(cityProperties.getLong("population"));
                                city.setLon(cityCoordinates.getDouble(0));
                                city.setLat(cityCoordinates.getDouble(1));

                                cityList.add(city);
                                Log.d(TAG, "onResponse: " + city.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        getPopUpDialog(cityList);
                    },
                    error -> {
                        Log.d(TAG, "getQuakeDetails: ", error);
                    });
            requestQueue.add(jsonObjectRequest);
        }
    }

    private void setTectonicPlaceInfo(double lat, double lon, WebView webView) {
        String url = Constants.buildUrlRegionTectonicDetails(lat, lon);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray tectonicInfoArray = response.getJSONObject(Constants.TECTONIC_KEY).getJSONArray(Constants.FEATURES_KEY);
                        if (tectonicInfoArray.length() > 0 && tectonicInfoArray.get(0) != null) {
                            JSONObject tectonicProperties = ((JSONObject) tectonicInfoArray.get(0)).getJSONObject(Constants.PROPERTIES_KEY);
                            if (tectonicProperties.has(Constants.SUMMARY_KEY)) {
                                String tectonicTextInfo = tectonicProperties.getString(Constants.SUMMARY_KEY);
                                webView.loadDataWithBaseURL(null, tectonicTextInfo, "text/html", "UTF-8", null);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e(TAG, "getTectonicPlaceInfo: ", error);
                });
        requestQueue.add(jsonObjectRequest);
    }

    public void getPopUpDialog(final List<City> cities) {
        alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.popup_cities_nearby, null);

        TextView popupList = view.findViewById(R.id.popupList);
        Button dismissButton = view.findViewById(R.id.btn_dismissPopUp);
        Button closeButton = view.findViewById(R.id.btn_close);
        WebView htmlWebView = view.findViewById(R.id.htmlWebView);

        setTectonicPlaceInfo(currentInfoPosition.latitude, currentInfoPosition.longitude, htmlWebView);

        String citiesString = cities.stream().map(City::toString).collect(Collectors.joining("\n\n"));
        popupList.setText(citiesString);

        dismissButton.setOnClickListener(v -> alertDialog.dismiss());
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

}