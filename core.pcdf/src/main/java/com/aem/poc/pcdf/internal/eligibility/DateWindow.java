package com.aem.poc.pcdf.internal.eligibility;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public final class DateWindow {

    private DateWindow() {
    }

    public static LocalDate todayOnInstance() {
        return LocalDate.now(ZoneId.systemDefault());
    }

    public static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof Calendar) {
            Calendar calendar = (Calendar) value;
            return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId()).toLocalDate();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String text = value.toString().trim();
        try {
            if (text.length() >= 10) {
                return LocalDate.parse(text.substring(0, 10));
            }
            return LocalDate.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static boolean includes(LocalDate start, LocalDate end, LocalDate evaluation) {
        if (start == null || end == null || evaluation == null) {
            return false;
        }
        return !evaluation.isBefore(start) && !evaluation.isAfter(end);
    }
}
