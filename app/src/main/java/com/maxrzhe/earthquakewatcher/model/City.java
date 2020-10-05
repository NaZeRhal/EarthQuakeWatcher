package com.maxrzhe.earthquakewatcher.model;

import java.util.Locale;

public class City {
    private String name;
    private String countryName;
    private double distanceFromDisaster;
    private long population;
    private double lat;
    private double lon;

    public City() {
    }

    public City(String name, String countryName, double distanceFromDisaster, long population, double lat, double lon) {
        this.name = name;
        this.countryName = countryName;
        this.distanceFromDisaster = distanceFromDisaster;
        this.population = population;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public double getDistanceFromDisaster() {
        return distanceFromDisaster;
    }

    public void setDistanceFromDisaster(double distanceFromDisaster) {
        this.distanceFromDisaster = distanceFromDisaster;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "City: %s\nCountry: %s\nDistance, km: %f\nPopulation: %d\nCoordinates: %f, %f",
                name, countryName, distanceFromDisaster, population, lat, lon);
    }
}
