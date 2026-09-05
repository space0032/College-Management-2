package com.college.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Canonical time-slot handling for Room Availability.
 * Normalizes the three legacy formats:
 * - Web: "09:00 - 10:00 AM"
 * - Timetable editor: "09:00 - 10:00" (24h)
 * - Desktop FX: "9:00-10:00"
 * into minute intervals so overlaps are detected, not just exact string equality.
 */
public class TimeSlotUtil {

    public static final List<String> CANONICAL_SLOTS = Arrays.asList(
            "09:00 - 10:00",
            "10:00 - 11:00",
            "11:00 - 12:00",
            "12:00 - 13:00",
            "13:00 - 14:00",
            "14:00 - 15:00",
            "15:00 - 16:00",
            "16:00 - 17:00");

    private static final Pattern TIME_PART = Pattern.compile("(\\d{1,2})\\s*:\\s*(\\d{2})\\s*([AaPp])\\.?\\s*[Mm]?\\.?");

    private TimeSlotUtil() {
    }

    public static String normalizeDay(String day) {
        if (day == null)
            return "";
        String d = day.trim().toLowerCase();
        switch (d) {
            case "mon":
            case "monday":
                return "Monday";
            case "tue":
            case "tues":
            case "tuesday":
                return "Tuesday";
            case "wed":
            case "wednesday":
                return "Wednesday";
            case "thu":
            case "thur":
            case "thurs":
            case "thursday":
                return "Thursday";
            case "fri":
            case "friday":
                return "Friday";
            case "sat":
            case "saturday":
                return "Saturday";
            case "sun":
            case "sunday":
                return "Sunday";
            default:
                if (d.isEmpty())
                    return "";
                return Character.toUpperCase(d.charAt(0)) + d.substring(1);
        }
    }

    /**
     * Parse "start - end" into [startMin, endMin]. Returns null if unparsable.
     */
    public static int[] parse(String slot) {
        if (slot == null)
            return null;
        String s = slot.trim();
        if (s.isEmpty())
            return null;
        String[] halves = s.split("\\s*-\\s*|\\s+to\\s+", 2);
        if (halves.length != 2)
            return null;
        String rangeMarker = detectMarker(s);
        Integer end = parseSingle(halves[1], rangeMarker);
        // Trailing marker ("11:00 - 12:00 PM") conventionally means 11AM-12PM,
        // not 11PM-12PM: prefer the start WITHOUT the range marker when
        // applying it to both ends yields an invalid (end <= start) interval.
        Integer startWith = parseSingle(halves[0], rangeMarker);
        Integer startWithout = rangeMarker == null ? startWith : parseSingle(halves[0], null);
        Integer start = startWith;
        if (end != null && startWith != null && startWithout != null
                && end <= startWith && startWithout < end) {
            start = startWithout;
        }
        if (start == null || end == null)
            return null;
        if (end <= start) {
            // e.g. "12:00 - 01:00 PM" where start parsed as 12 but end as 13 is fine;
            // only adjust when end <= start and end looks like a PM hour
            if (end < 12 * 60)
                end += 12 * 60;
        }
        if (end <= start)
            return null;
        return new int[] { start, end };
    }

    public static boolean overlaps(String a, String b) {
        int[] ia = parse(a);
        int[] ib = parse(b);
        if (ia == null || ib == null) {
            // Fall back to case-insensitive exact match for unparsable slots
            return a.trim().equalsIgnoreCase(b.trim());
        }
        return ia[0] < ib[1] && ib[0] < ia[1];
    }

    private static String detectMarker(String full) {
        String upper = full.toUpperCase();
        // Prefer the trailing marker ("... PM") as the range default
        if (upper.trim().endsWith("PM") || upper.contains(" P.M"))
            return "P";
        if (upper.trim().endsWith("AM") || upper.contains(" A.M"))
            return "A";
        if (upper.contains("PM"))
            return "P";
        if (upper.contains("AM"))
            return "A";
        return null;
    }

    private static Integer parseSingle(String token, String defaultMarker) {
        String t = token.trim().toUpperCase();
        String marker = defaultMarker;
        if (t.contains("P.M") || t.endsWith("PM") || t.contains(" PM")) {
            marker = "P";
        } else if (t.contains("A.M") || t.endsWith("AM") || t.contains(" AM")) {
            marker = "A";
        }
        Matcher m = TIME_PART.matcher(token);
        int hour;
        int min;
        if (m.find()) {
            hour = Integer.parseInt(m.group(1));
            min = Integer.parseInt(m.group(2));
            String inline = m.group(3);
            if (inline != null) {
                marker = inline.equalsIgnoreCase("P") ? "P" : "A";
            }
        } else {
            // Plain "9" or "9:00" without minutes?
            Matcher plain = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?").matcher(t);
            if (!plain.find())
                return null;
            hour = Integer.parseInt(plain.group(1));
            min = plain.group(2) != null ? Integer.parseInt(plain.group(2)) : 0;
        }
        if (hour < 0 || hour > 23 || min < 0 || min > 59)
            return null;
        if (marker != null) {
            if ("P".equals(marker) && hour < 12)
                hour += 12;
            if ("A".equals(marker) && hour == 12)
                hour = 0;
        } else if (hour >= 1 && hour <= 7) {
            // Bare "1:00-2:00" in college context means 13:00-14:00
            hour += 12;
        }
        return hour * 60 + min;
    }

    public static String format(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
