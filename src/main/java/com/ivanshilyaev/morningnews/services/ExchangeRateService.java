package com.ivanshilyaev.morningnews.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ExchangeRateService {

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final String BASE_CONVERSION_URL = "https://api.exchangerate.host/convert";

    public String getUsdExchangeRate() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("from", "USD");
        params.put("to", "BYN");

        return getExchangeRate(BASE_CONVERSION_URL, params);
    }

    public String getEurExchangeRate() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("from", "EUR");
        params.put("to", "BYN");

        return getExchangeRate(BASE_CONVERSION_URL, params);
    }

    public String getBtcExchangeRate() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("from", "BTC");
        params.put("to", "USD");

        String rate = getExchangeRate(BASE_CONVERSION_URL, params);
        return rate.substring(0, rate.indexOf('.'));
    }

    public String getExchangeRate(String url, Map<String, String> params) throws IOException {
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        Request request = new Request.Builder().url(httpBuilder.build()).build();
        var response = client.newCall(request).execute();
        JsonObject root = JsonParser.parseString(Objects.requireNonNull(response.body()).string()).getAsJsonObject();

        return root.get("result").getAsString();
    }
}
