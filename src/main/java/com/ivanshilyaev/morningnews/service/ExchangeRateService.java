package com.ivanshilyaev.morningnews.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class ExchangeRateService {

    private static final String USD_TO_BYN_URL = "https://v6.exchangerate-api.com/v6/"
            + System.getenv("API_TOKEN")
            + "/pair/USD/BYN";
    private static final String EUR_TO_BYN_URL = "https://v6.exchangerate-api.com/v6/"
            + System.getenv("API_TOKEN")
            + "/pair/EUR/BYN";

    public String getUsdExchangeRate() throws IOException {
        return getExchangeRate(USD_TO_BYN_URL);
    }

    public String getEurExchangeRate() throws IOException {
        return getExchangeRate(EUR_TO_BYN_URL);
    }

    private String getExchangeRate(String url) throws IOException {
        var client = new OkHttpClient();
        var request = new Request.Builder().url(url).build();
        var response = client.newCall(request).execute();

        JsonObject root = JsonParser.parseString(Objects.requireNonNull(response.body()).string()).getAsJsonObject();

        return root.get("conversion_rate").getAsString();
    }
}
