package com.ivanshilyaev.morningnews.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    public void sendMessage(String text) throws Exception {
        String url = "https://api.telegram.org/bot"
                + System.getenv("BOT_TOKEN")
                + "/sendMessage?text=" + text
                + "&chat_id="
                + System.getenv("BOT_CHAT_ID");

        var client = new OkHttpClient();
        var request = new Request.Builder().url(url).build();
        client.newCall(request).execute();
    }
}
