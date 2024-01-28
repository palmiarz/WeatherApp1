package org.weatherapp;

import com.google.gson.annotations.SerializedName;

public class WeatherData {
    private MainData main;
    private WindData wind;

    //informuje Gson, że pole weather w klasie WeatherData odpowiada
    // kluczowi "weather" w danych JSON. Dzięki tej adnotacji,
    // Gson jest w stanie dopasować odpowiednie wartości JSON do pola weather podczas deserializacji.
    @SerializedName("weather")
    private WeatherInfo[] weather;

    public MainData getMain() {
        return main;
    }

    public void setMain(MainData main) {
        this.main = main;
    }

    public WindData getWind() {
        return wind;
    }

    public void setWind(WindData wind) {
        this.wind = wind;
    }

    public WeatherInfo[] getWeather() {
        return weather;
    }

    public void setWeather(WeatherInfo[] weather) {
        this.weather = weather;
    }

    public static class MainData {
        private double temp;
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }

    public static class WindData {
        private double speed;

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }
    }

    public static class WeatherInfo {
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}