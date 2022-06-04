package com.example.hodophile.ui.home;

public class ItineraryItem implements Comparable<ItineraryItem> {

    String location, date;
    com.example.levoyage.ui.home.TimeParcel startTime, endTime;
    int type;

    public ItineraryItem(String location, String date, com.example.levoyage.ui.home.TimeParcel startTime, com.example.levoyage.ui.home.TimeParcel endTime) {
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = 0;
    }

    public ItineraryItem(int type) {
        this.type = type;
    }

    public ItineraryItem() {}

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public com.example.levoyage.ui.home.TimeParcel getStartTime() {
        return startTime;
    }

    public void setStartTime(com.example.levoyage.ui.home.TimeParcel startTime) {
        this.startTime = startTime;
    }

    public com.example.levoyage.ui.home.TimeParcel getEndTime() {
        return endTime;
    }

    public void setEndTime(com.example.levoyage.ui.home.TimeParcel endTime) {
        this.endTime = endTime;
    }

    public int getType() { return type; }

    public void setType(int type) { this.type = type; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    @Override
    public int compareTo(ItineraryItem o) {
        return this.getStartTime().compareTo(o.getStartTime());
    }
}
