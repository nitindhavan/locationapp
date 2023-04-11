package com.exambullet.locationapp;

import android.location.Location;

public class LocationData {
    Location location;
    String otherInfo;
    String vehicleData;
    int tripNumber;
    double odometerStart;
    double odometerEnd;

    public LocationData(Location location, String otherInfo, String vehicleData, int tripNumber, double odometerStart, double odometerEnd) {
        this.location = location;
        this.otherInfo = otherInfo;
        this.vehicleData = vehicleData;
        this.tripNumber = tripNumber;
        this.odometerStart = odometerStart;
        this.odometerEnd = odometerEnd;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public String getVehicleData() {
        return vehicleData;
    }

    public void setVehicleData(String vehicleData) {
        this.vehicleData = vehicleData;
    }

    public int getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(int tripNumber) {
        this.tripNumber = tripNumber;
    }

    public double getOdometerStart() {
        return odometerStart;
    }

    public void setOdometerStart(double odometerStart) {
        this.odometerStart = odometerStart;
    }

    public double getOdometerEnd() {
        return odometerEnd;
    }

    public void setOdometerEnd(double odometerEnd) {
        this.odometerEnd = odometerEnd;
    }
}
