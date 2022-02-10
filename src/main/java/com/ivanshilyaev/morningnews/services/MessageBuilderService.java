package com.ivanshilyaev.morningnews.services;

import com.google.common.collect.Multimap;
import com.ivanshilyaev.morningnews.dtos.CalendarEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.ivanshilyaev.morningnews.utils.AppConstants.TELEGRAM_NEW_LINE;

@Service
@RequiredArgsConstructor
public class MessageBuilderService {

    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ExchangeRateService exchangeRateService;
    private final NotionService notionService;
    private final GoogleCalendarService calendarService;

    public String buildMessage() throws IOException, GeneralSecurityException {
        StringBuilder builder = new StringBuilder();

        // Exchange rates
        builder.append("\uD83D\uDCB0 Exchange rates").append(TELEGRAM_NEW_LINE);
        builder.append("$ - ")
                .append(exchangeRateService.getUsdExchangeRate())
                .append(TELEGRAM_NEW_LINE);
        builder.append("€ - ")
                .append(exchangeRateService.getEurExchangeRate())
                .append(TELEGRAM_NEW_LINE);
        builder.append(TELEGRAM_NEW_LINE);

        // Notion tasks
        List<String> tasks = notionService.getNotionTasksForToday();
        if (tasks.isEmpty()) {
            builder.append("\uD83D\uDCED No tasks for today").append(TELEGRAM_NEW_LINE);
        } else {
            builder.append("⚡️ Tasks").append(TELEGRAM_NEW_LINE);
            tasks.forEach(t -> builder.append("• ").append(t).append(TELEGRAM_NEW_LINE));
        }
        builder.append(TELEGRAM_NEW_LINE);

        // Calendar events
        Multimap<LocalDateTime, CalendarEvent> events = calendarService.getCalendarEvents();
        if (events.isEmpty()) {
            builder.append("\uD83D\uDCA4 No events for today").append(TELEGRAM_NEW_LINE);
        } else {
            builder.append("\uD83D\uDDD3 Events").append(TELEGRAM_NEW_LINE);
            for (CalendarEvent event : events.values()) {
                builder.append("• ")
                        .append(event.getStart().format(DISPLAY_TIME_FORMAT))
                        .append("-")
                        .append(event.getEnd().format(DISPLAY_TIME_FORMAT))
                        .append(" ")
                        .append(event.getSummary())
                        .append(TELEGRAM_NEW_LINE);
            }
        }
        builder.append(TELEGRAM_NEW_LINE);

        // Calendar reminders
        List<String> reminders = calendarService.getCalendarReminders();
        if (!reminders.isEmpty()) {
            builder.append("❗ Reminders").append(TELEGRAM_NEW_LINE);
            for (String reminder : reminders) {
                builder.append("• ")
                        .append(reminder)
                        .append(TELEGRAM_NEW_LINE);
            }
        }
        builder.append(TELEGRAM_NEW_LINE);

        return builder.toString();
    }
}
