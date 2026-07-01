package org.example.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class EnvironmentParser {
    private static final Pattern ZONE_PATTERN = Pattern.compile("Zone(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIER_PATTERN = Pattern.compile("Tier(\\d+)", Pattern.CASE_INSENSITIVE);

    public static ZoneInfo parse(String input) {
        if (input == null || input.isEmpty()) return new ZoneInfo(-1, 1);

        // Zone1 -> "1"
        int zone = -1;//global
        Matcher zoneMatcher = ZONE_PATTERN.matcher(input);
        if (zoneMatcher.find()) {
            zone = Integer.parseInt(zoneMatcher.group(1));
        } else if (input.toLowerCase().contains("ocean")) {
            // Edge case for Oceans
            zone = 0;
        }

        // Tier
        // Default to 1 if not found
        int tier = 1;
        Matcher tierMatcher = TIER_PATTERN.matcher(input);
        if (tierMatcher.find()) {
            tier = Integer.parseInt(tierMatcher.group(1));
        }

        // descriptor
        // Edge case
        /*String descriptor = input
                .replaceAll("(?i)Zone\\d+_?", "")
                .replaceAll("(?i)_?Tier\\d+", "")
                .replace("_", " ")
                .trim();

        if (descriptor.isEmpty()) descriptor = "General";*/

        return new ZoneInfo(zone, tier);
    }

}

