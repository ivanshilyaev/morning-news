package com.ivanshilyaev.morningnews.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class NotionService {

    private JsonObject createNotionPostJsonObject() {
        var root = new JsonObject();
        var and = new JsonObject();
        var array = new JsonArray();
        var firstFilterOption = new JsonObject();
        var secondFilterOption = new JsonObject();
        var thirdFilterOption = new JsonObject();

        firstFilterOption.addProperty("property", "Day");
        var select = new JsonObject();
        String dayOfWeek = Instant.now()
                .atOffset(ZoneOffset.UTC)
                .getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.US);
        select.addProperty("equals", dayOfWeek);
        firstFilterOption.add("select", select);

        secondFilterOption.addProperty("property", "Week");
        var date1 = new JsonObject();
        LocalDate firstDayOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        date1.addProperty("on_or_after", firstDayOfWeek.toString());
        secondFilterOption.add("date", date1);

        thirdFilterOption.addProperty("property", "Week");
        var date2 = new JsonObject();
        LocalDate lastDayOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        date2.addProperty("on_or_before", lastDayOfWeek.toString());
        thirdFilterOption.add("date", date2);

        array.add(firstFilterOption);
        array.add(secondFilterOption);
        array.add(thirdFilterOption);

        and.add("and", array);

        root.add("filter", and);

        return root;
    }

    public List<String> getNotionTasksForToday() throws IOException {
        String url = "https://api.notion.com/v1/databases/"
                + System.getenv("NOTION_DATABASE_ID")
                + "/query";

        var client = new OkHttpClient();
        var jsonObject = createNotionPostJsonObject();
        var requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
        var request = new Request.Builder()
                .url(url)
                .header("Notion-Version", "2021-08-16")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "
                        + System.getenv("NOTION_INTEGRATION_TOKEN")
                )
                .post(requestBody)
                .build();

        var response = client.newCall(request).execute();

        JsonObject root = JsonParser.parseString(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
        JsonArray array = root.getAsJsonArray("results");
        List<String> tasks = new ArrayList<>();
        array.forEach(p -> tasks.add(p.getAsJsonObject()
                .get("properties")
                .getAsJsonObject()
                .get("Task Name")
                .getAsJsonObject()
                .get("title")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("text")
                .getAsJsonObject()
                .get("content")
                .getAsString()
        ));

        return tasks;
    }
}
