package com.aem.poc.pcdf.internal.topology;

import com.aem.poc.pcdf.internal.locale.LocaleFolder;
import java.util.regex.Pattern;

/**
 * Promotions live at {@code /content/dam/aem-poc/pcdf/{region}/{country}/{locale}}.
 * Region and country are lowercase folder names (for example {@code americas}, {@code us}).
 */
public final class TopologyPath {

    public static final String DAM_ROOT = "/content/dam/aem-poc/pcdf";

    private static final Pattern REGION = Pattern.compile("[a-z]{2,16}");
    private static final Pattern COUNTRY = Pattern.compile("[a-z]{2,3}");
    private static final Pattern FRAGMENT_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,127}");

    private TopologyPath() {
    }

    public static boolean isSafeRegion(String region) {
        return region != null && REGION.matcher(region).matches();
    }

    public static boolean isSafeCountry(String country) {
        return country != null && COUNTRY.matcher(country).matches();
    }

    public static boolean isSafeFragmentName(String name) {
        return name != null && FRAGMENT_NAME.matcher(name).matches();
    }

    public static boolean isSafe(String region, String country, String locale) {
        return isSafeRegion(region) && isSafeCountry(country) && LocaleFolder.isSafe(locale);
    }

    public static String folderPath(String region, String country, String locale) {
        return DAM_ROOT + "/" + region + "/" + country + "/" + locale;
    }
}
