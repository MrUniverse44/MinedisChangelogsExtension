package me.blueslime.minedis.extensions.changelogs.utils.version;

import java.util.Locale;

public class VersionConverter {
    public static String convert(Conversion conversion, String version) {
        String[] split = version.replace(" ", "").replace("v", "").split("\\.");

        int big = 0;
        int minor = 0;
        int fix = 0;

        if (split.length >= 1) {
            big = isNumber(split[0]) ? Integer.parseInt(split[0]) : 0;
        }

        if (split.length >= 2) {
            minor = isNumber(split[1]) ? Integer.parseInt(split[1]) : 0;
        }

        if (split.length >= 3) {
            minor = isNumber(split[2]) ? Integer.parseInt(split[2]) : 0;
        }

        if (conversion == Conversion.BIG) {
            big++;
            return big + ".0.0";
        }
        if (conversion == Conversion.MINOR) {
            minor++;
            return big + "." + minor + ".0";
        }
        fix++;
        return big + "." + minor + "." + fix;
    }

    public enum Conversion {
        BIG,
        MINOR,
        FIX;

        public String lower() {
            return toString().toLowerCase(Locale.ENGLISH);
        }

        public static boolean isConversion(String text) {
            for (Conversion comp : values()) {
                if (comp.lower().equals(text.toLowerCase(Locale.ENGLISH))) {
                    return true;
                }
            }
            return false;
        }

        public static Conversion fromString(String text) {
            for (Conversion comp : values()) {
                if (comp.lower().equals(text.toLowerCase(Locale.ENGLISH))) {
                    return comp;
                }
            }
            return FIX;
        }
    }

    private static boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
