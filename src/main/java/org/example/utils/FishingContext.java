package org.example.utils;

public record FishingContext(
        //TimePeriod time,
        String zone,
        int tier,
        String region,
        String biome,
        double yPos,
        int waterDepth
) { }