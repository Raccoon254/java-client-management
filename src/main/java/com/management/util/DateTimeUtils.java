package com.management.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtils {

    // Common date formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    public static final DateTimeFormatter ICS_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Format a LocalDate to string using the default formatter
     * @param date The date to format
     * @return The formatted date string, or empty string if date is null
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Format a LocalDate to string using a custom formatter
     * @param date The date to format
     * @param formatter The formatter to use
     * @return The formatted date string, or empty string if date is null
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "";
    }

    /**
     * Format a LocalTime to string using the default formatter
     * @param time The time to format
     * @return The formatted time string, or empty string if time is null
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }

    /**
     * Format a LocalTime to string using a custom formatter
     * @param time The time to format
     * @param formatter The formatter to use
     * @return The formatted time string, or empty string if time is null
     */
    public static String formatTime(LocalTime time, DateTimeFormatter formatter) {
        return time != null ? time.format(formatter) : "";
    }

    /**
     * Format a LocalDateTime to string using the default formatter
     * @param dateTime The datetime to format
     * @return The formatted datetime string, or empty string if datetime is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    /**
     * Format a LocalDateTime to string using a custom formatter
     * @param dateTime The datetime to format
     * @param formatter The formatter to use
     * @return The formatted datetime string, or empty string if datetime is null
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? dateTime.format(formatter) : "";
    }

    /**
     * Parse a date string using the default formatter
     * @param dateStr The date string to parse
     * @return The parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a date string using a custom formatter
     * @param dateStr The date string to parse
     * @param formatter The formatter to use
     * @return The parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a time string using the default formatter
     * @param timeStr The time string to parse
     * @return The parsed LocalTime, or null if parsing fails
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a time string using a custom formatter
     * @param timeStr The time string to parse
     * @param formatter The formatter to use
     * @return The parsed LocalTime, or null if parsing fails
     */
    public static LocalTime parseTime(String timeStr, DateTimeFormatter formatter) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a datetime string using the default formatter
     * @param dateTimeStr The datetime string to parse
     * @return The parsed LocalDateTime, or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a datetime string using a custom formatter
     * @param dateTimeStr The datetime string to parse
     * @param formatter The formatter to use
     * @return The parsed LocalDateTime, or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Generate a list of dates between two dates (inclusive)
     * @param startDate The start date
     * @param endDate The end date
     * @return List of dates between start and end
     */
    public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }

        if (startDate.isAfter(endDate)) {
            // Swap dates if start is after end
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * Generate a list of dates for a month
     * @param year The year
     * @param month The month (1-12)
     * @return List of dates in the month
     */
    public static List<LocalDate> getDatesInMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return getDatesBetween(startDate, endDate);
    }

    /**
     * Calculate the number of days between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return The number of days between start and end
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Generate a list of time slots
     * @param startHour The start hour (0-23)
     * @param endHour The end hour (0-23)
     * @param intervalMinutes The interval in minutes
     * @return List of time slots
     */
    public static List<LocalTime> generateTimeSlots(int startHour, int endHour, int intervalMinutes) {
        if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
            throw new IllegalArgumentException("Hours must be between 0 and 23");
        }

        if (intervalMinutes <= 0 || intervalMinutes > 60 || 60 % intervalMinutes != 0) {
            throw new IllegalArgumentException("Interval minutes must be a divisor of 60");
        }

        List<LocalTime> timeSlots = new ArrayList<>();

        if (startHour > endHour) {
            // Handle overnight (e.g., 22:00 to 2:00)
            for (int hour = startHour; hour <= 23; hour++) {
                for (int minute = 0; minute < 60; minute += intervalMinutes) {
                    timeSlots.add(LocalTime.of(hour, minute));
                }
            }

            for (int hour = 0; hour <= endHour; hour++) {
                for (int minute = 0; minute < 60; minute += intervalMinutes) {
                    timeSlots.add(LocalTime.of(hour, minute));
                }
            }
        } else {
            // Normal case (e.g., 9:00 to 17:00)
            for (int hour = startHour; hour <= endHour; hour++) {
                for (int minute = 0; minute < 60; minute += intervalMinutes) {
                    timeSlots.add(LocalTime.of(hour, minute));
                }
            }
        }

        return timeSlots;
    }

    /**
     * Format a date for use in an ICS file
     * @param date The date
     * @return The formatted date string for ICS
     */
    public static String formatDateTimeForICS(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ICS_DATETIME_FORMATTER) : "";
    }

    /**
     * Get the current date and time as an ICS formatted string
     * @return The current date and time formatted for ICS
     */
    public static String getCurrentDateTimeForICS() {
        return LocalDateTime.now().format(ICS_DATETIME_FORMATTER);
    }

    /**
     * Get a human-readable description of time elapsed since a given date/time
     * @param dateTime The past date/time
     * @return A human-readable string describing the elapsed time
     */
    public static String getTimeElapsedDescription(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (days > 365) {
            long years = days / 365;
            return years + (years == 1 ? " year ago" : " years ago");
        } else if (days > 30) {
            long months = days / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        } else if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            if (hours > 0) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else {
                long minutes = ChronoUnit.MINUTES.between(dateTime, now);
                if (minutes > 0) {
                    return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
                } else {
                    return "just now";
                }
            }
        }
    }
}