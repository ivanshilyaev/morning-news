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
        builder.append("\uD83D\uDCB0 Exchange rates").append(System.lineSeparator());
        builder.append("$ - ")
                .append(exchangeRateService.getUsdExchangeRate())
                .append(System.lineSeparator());
        builder.append("€ - ")
                .append(exchangeRateService.getEurExchangeRate())
                .append(System.lineSeparator());
        builder.append(System.lineSeparator());

        // Notion tasks
        List<String> tasks = notionService.getNotionTasksForToday();
        if (tasks.isEmpty()) {
            builder.append("\uD83D\uDCED No tasks for today").append(System.lineSeparator());
        } else {
            builder.append("⚡️ Tasks").append(System.lineSeparator());
            tasks.forEach(t -> builder.append("• ").append(t).append(System.lineSeparator()));
        }
        builder.append(System.lineSeparator());

        // Calendar events
        Multimap<LocalDateTime, CalendarEvent> events = calendarService.getCalendarEvents();
        if (events.isEmpty()) {
            builder.append("\uD83D\uDCA4 No events for today").append(System.lineSeparator());
        } else {
            builder.append("\uD83D\uDDD3 Events").append(System.lineSeparator());
            for (CalendarEvent event : events.values()) {
                builder.append("• ")
                        .append(event.getStart().format(DISPLAY_TIME_FORMAT))
                        .append("-")
                        .append(event.getEnd().format(DISPLAY_TIME_FORMAT))
                        .append(" ")
                        .append(event.getSummary())
                        .append(System.lineSeparator());
            }
        }
        builder.append(System.lineSeparator());

        // Calendar reminders
        List<String> reminders = calendarService.getCalendarReminders();
        if (!reminders.isEmpty()) {
            builder.append("❗ Reminders").append(System.lineSeparator());
            for (String reminder : reminders) {
                builder.append("• ")
                        .append(reminder)
                        .append(System.lineSeparator());
            }
        }
        builder.append(System.lineSeparator());

        return builder.toString();
    }
}
