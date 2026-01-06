package com.college.services;

import com.college.models.CalendarEvent;
import com.college.models.CalendarEvent.EventType;
import com.college.utils.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to interact with Google Calendar API
 */
public class GoogleCalendarService {

    private static final String API_KEY = "YOUR_API_KEY_HERE"; // Placeholder - In real app, load from secure config
    private static final String CALENDAR_ID = "en.indian#holiday@group.v.calendar.google.com"; // Indian Holidays
    private static final String BASE_URL = "https://www.googleapis.com/calendar/v3/calendars/";

    private final HttpClient httpClient;

    public GoogleCalendarService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Fetch public holidays for a given year and month
     * Note: Google API often returns events for a range. We'll set a reasonable
     * range.
     */
    public List<CalendarEvent> getHolidays(int year, int month) {
        List<CalendarEvent> holidays = new ArrayList<>();

        // Construct timeMin and timeMax (RFC3339 format)
        // Month is 1-based
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        String timeMin = start.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME) + "Z";
        String timeMax = end.atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME) + "Z";

        try {
            // If API Key is placeholder, return empty or mock data to avoid errors during
            // dev without key
            if (API_KEY.equals("YOUR_API_KEY_HERE")) {
                Logger.info("Google API Key not set. Skipping API call.");
                return holidays;
            }

            String url = BASE_URL + URLEncoder.encode(CALENDAR_ID, StandardCharsets.UTF_8) + "/events" +
                    "?key=" + API_KEY +
                    "&timeMin=" + URLEncoder.encode(timeMin, StandardCharsets.UTF_8) +
                    "&timeMax=" + URLEncoder.encode(timeMax, StandardCharsets.UTF_8) +
                    "&singleEvents=true";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray items = json.getAsJsonArray("items");

                if (items != null) {
                    for (JsonElement item : items) {
                        JsonObject eventJson = item.getAsJsonObject();
                        String summary = eventJson.has("summary") ? eventJson.get("summary").getAsString() : "Holiday";

                        // Start date (Google Holidays are usually all-day events, so they have "date"
                        // field)
                        JsonObject startJson = eventJson.getAsJsonObject("start");
                        if (startJson.has("date")) {
                            String dateStr = startJson.get("date").getAsString();
                            LocalDate date = LocalDate.parse(dateStr);

                            CalendarEvent event = new CalendarEvent();
                            event.setTitle(summary);
                            event.setEventDate(date);
                            event.setEventType(EventType.HOLIDAY);
                            event.setDescription("Public Holiday fetched from Google Calendar");

                            holidays.add(event);
                        }
                    }
                }
            } else {
                Logger.error("Failed to fetch holidays: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            Logger.error("Error fetching holidays from Google Calendar", e);
        }

        return holidays;
    }
}
