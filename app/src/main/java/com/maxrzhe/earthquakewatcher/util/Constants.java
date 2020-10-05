package com.maxrzhe.earthquakewatcher.util;

import android.net.Uri;

public class Constants {
    public static final String URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.geojson";
    private static final String URL_PLACE_DETAILS = "https://earthquake.usgs.gov/ws/geoserve/places.json";
    private static final String URL_REGION_TECTONIC_DETAILS = "https://earthquake.usgs.gov/ws/geoserve/regions.json";

    private static final String LAT_KEY = "latitude";
    private static final String LON_KEY = "longitude";
    private static final String MAX_RADIUS_KM = "maxradiuskm";

    public static final String FEATURES_KEY = "features";
    public static final String PROPERTIES_KEY = "properties";
    public static final String GEOMETRY_KEY = "geometry";
    public static final String COORDINATES_KEY = "coordinates";
    public static final String EVENT_KEY = "event";
    public static final String GEONAMES_KEY = "geonames";
    public static final String TYPE_KEY = "type";
    public static final String TECTONIC_KEY = "tectonic";
    public static final String LIMIT_KEY = "limit";
    public static final String MIN_POPULATION_KEY = "minpopulation";
    public static final String SUMMARY_KEY = "summary";


    public static final int LIMIT = 100;

    public static String buildUrlPlaceDetails(double lat, double lon, int maxRadiusKm, int limit, int minPopulation) {
        return Uri.parse(URL_PLACE_DETAILS).buildUpon()
                .appendQueryParameter(LAT_KEY,String.valueOf(lat))
                .appendQueryParameter(LON_KEY,String.valueOf(lon))
                .appendQueryParameter(MAX_RADIUS_KM,String.valueOf(maxRadiusKm))
                .appendQueryParameter(LIMIT_KEY,String.valueOf(limit))
                .appendQueryParameter(MIN_POPULATION_KEY,String.valueOf(minPopulation))
                .build().toString();

    }

    public static String buildUrlRegionTectonicDetails(double lat, double lon) {
        return Uri.parse(URL_REGION_TECTONIC_DETAILS).buildUpon()
                .appendQueryParameter(LAT_KEY,String.valueOf(lat))
                .appendQueryParameter(LON_KEY,String.valueOf(lon))
                .appendQueryParameter(TYPE_KEY, TECTONIC_KEY)
                .build().toString();

    }
}
