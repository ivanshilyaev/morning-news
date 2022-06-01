package com.ivanshilyaev.morningnews.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;

@Service
public class WeatherService {

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    public String getDayForecast() throws IOException {
        String city = "Minsk"; // TODO: retrieve city (or coordinates) from Telegram current location

        String url = "https://api.weatherapi.com/v1/forecast.json?key=" +
            System.getenv("WEATHER_API_TOKEN") +
            "&q=" + city + "&aqi=no&alerts=no";
        var request = new Request.Builder()
            .url(url)
            .get()
            .build();
        var response = client.newCall(request).execute();

        JsonObject root = JsonParser.parseString(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
        double high = root.get("forecast").getAsJsonObject()
            .get("forecastday").getAsJsonArray()
            .get(0).getAsJsonObject()
            .get("day").getAsJsonObject()
            .get("maxtemp_c").getAsDouble();
        double low = root.get("forecast").getAsJsonObject()
            .get("forecastday").getAsJsonArray()
            .get(0).getAsJsonObject()
            .get("day").getAsJsonObject()
            .get("mintemp_c").getAsDouble();
        String condition = root.get("forecast").getAsJsonObject()
            .get("forecastday").getAsJsonArray()
            .get(0).getAsJsonObject()
            .get("day").getAsJsonObject()
            .get("condition").getAsJsonObject()
            .get("text").getAsString();

        return city + ": " + condition.toLowerCase() + System.lineSeparator() +
            "H:" + round(high) + "°" + " L:" + round(low) + "°";
    }
}
