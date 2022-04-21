package com.ivanshilyaev.morningnews.services;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    public void sendMessage(String text) throws Exception {
        String url = "https://api.telegram.org/bot"
                + System.getenv("BOT_TOKEN")
                + "/sendMessage";
        var client = new OkHttpClient();
        var formBody = new FormBody.Builder()
                .add("text", text)
                .add("chat_id", System.getenv("BOT_CHAT_ID"))
                .build();
        var request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).execute();
    }
}
