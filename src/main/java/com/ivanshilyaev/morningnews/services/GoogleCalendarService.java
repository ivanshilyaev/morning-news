package com.ivanshilyaev.morningnews.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.ivanshilyaev.morningnews.dtos.CalendarEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final DateTimeFormatter PARSE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static final DateTime START_OF_THE_DAY = new DateTime(Date.from(LocalDate.now().atStartOfDay()
            .atZone(ZoneId.systemDefault()).toInstant()));
    private static final DateTime END_OF_THE_DAY = new DateTime(Date.from(LocalDate.now().atStartOfDay().plusDays(1)
            .atZone(ZoneId.systemDefault()).toInstant()));

    private final Multimap<LocalDateTime, CalendarEvent> events = TreeMultimap.create();
    private final List<String> reminders = new ArrayList<>();

    private PrivateKey parsePrivateKeyContent() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyContent = System.getenv("GOOGLE_SERVICE_ACCOUNT_PRIVATE_KEY");

        privateKeyContent = privateKeyContent
                .replaceAll("\\\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent.getBytes()));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    private HttpRequestInitializer authorize() throws NoSuchAlgorithmException, InvalidKeySpecException {
        ServiceAccountCredentials credentials = ServiceAccountCredentials.newBuilder()
                .setClientId(System.getenv("GOOGLE_CLIENT_ID"))
                .setClientEmail(System.getenv("GOOGLE_CLIENT_EMAIL"))
                .setPrivateKey(parsePrivateKeyContent())
                .setPrivateKeyId(System.getenv("GOOGLE_SERVICE_ACCOUNT_PRIVATE_KEY_ID"))
                .setScopes(Collections.singletonList(CalendarScopes.CALENDAR_READONLY))
                .build();

        return new HttpCredentialsAdapter(credentials);
    }

    private void extractEventsContent(Events calendarEvents, Multimap<LocalDateTime, CalendarEvent> allEvents) {
        for (Event event : calendarEvents.getItems()) {
            String summary = event.getSummary();
            /*
             * Date has the format "yyyy-mm-dd", ONLY if this is an all-day event.
             * Otherwise, start date will be null, start will only contain dateTime.
             */
            if (event.getStart().getDate() == null) {
                LocalDateTime start = LocalDateTime.parse(event.getStart().getDateTime().toStringRfc3339(), PARSE_FORMAT);
                LocalDateTime end = LocalDateTime.parse(event.getEnd().getDateTime().toStringRfc3339(), PARSE_FORMAT);
                allEvents.put(start, new CalendarEvent(start, end, summary));
            } else {
                reminders.add(summary);
            }
        }
    }

    private void getEventsFromParticularCalendar(Calendar service, String calendarId) throws IOException {
        Events calendarEvents = service
                .events()
                .list(calendarId)
                .setTimeMin(START_OF_THE_DAY)
                .setTimeMax(END_OF_THE_DAY)
                .setSingleEvents(true)
                .execute();
        extractEventsContent(calendarEvents, events);
    }

    private void processCalendars() throws IOException, GeneralSecurityException {
        Calendar calendarService = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                new GsonFactory(),
                authorize()
        ).build();

        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_EDUCATION_CALENDAR_ID"));
        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_LIFE_CALENDAR_ID"));
        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_PD_CALENDAR_ID"));
        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_SPORT_CALENDAR_ID"));
        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_TASKS_CALENDAR_ID"));
        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_UNI_CALENDAR_ID"));
        getEventsFromParticularCalendar(calendarService, System.getenv("GOOGLE_WORK_CALENDAR_ID"));
    }

    public Multimap<LocalDateTime, CalendarEvent> getCalendarEvents() throws GeneralSecurityException, IOException {
        processCalendars();

        return events;
    }

    public List<String> getCalendarReminders() {
        return reminders;
    }
}
