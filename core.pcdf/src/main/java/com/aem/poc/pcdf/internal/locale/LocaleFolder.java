package com.aem.poc.pcdf.internal.locale;

import java.util.regex.Pattern;

/**
 * Locale query values are the third DAM folder segment under region/country:
 * lowercase BCP-47-style tokens such as {@code en-us}, {@code fr}, {@code hi}.
 * Underscore forms ({@code en_US}) are not folders and yield no-match.
 */
public final class LocaleFolder {

    private static final Pattern SAFE = Pattern.compile("[a-z]{2,3}(-[a-z]{2,8})*");

    private LocaleFolder() {
    }

    public static boolean isSafe(String locale) {
        return locale != null && SAFE.matcher(locale).matches();
    }
}
