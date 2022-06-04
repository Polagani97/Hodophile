package com.example.hodophile.ui.weather;

public class WeatherItem {

    private String icon, temperature, weather;
    private int condition;
    private String date;

    public WeatherItem(String icon, String temperature, String weather, int condition, String date) {
        this.icon = icon;
        this.temperature = temperature;
        this.weather = weather;
        this.condition = condition;
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
