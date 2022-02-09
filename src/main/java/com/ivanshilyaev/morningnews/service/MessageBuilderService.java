package com.ivanshilyaev.morningnews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.ivanshilyaev.morningnews.utils.AppConstants.TELEGRAM_NEW_LINE;

@Service
@RequiredArgsConstructor
public class MessageBuilderService {

    private final ExchangeRateService exchangeRateService;
    private final NotionService notionService;

    public String buildMessage() throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append("Exchange rates\uD83D\uDCB0").append(TELEGRAM_NEW_LINE);
        builder.append("$ - ");
        builder.append(exchangeRateService.getUsdExchangeRate());
        builder.append(TELEGRAM_NEW_LINE);
        builder.append("€ - ");
        builder.append(exchangeRateService.getEurExchangeRate());
        builder.append(TELEGRAM_NEW_LINE).append(TELEGRAM_NEW_LINE);

        List<String> tasks = notionService.getNotionTasksForToday();
        if (tasks.isEmpty()) {
            builder.append("No tasks for today\uD83D\uDCED").append(TELEGRAM_NEW_LINE).append(TELEGRAM_NEW_LINE);
        } else {
            builder.append("Tasks for the day⚡️").append(TELEGRAM_NEW_LINE);
            tasks.forEach(t -> builder.append("• ").append(t).append(TELEGRAM_NEW_LINE));
        }

        return builder.toString();
    }
}
