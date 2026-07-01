package org.example.utils;

public record ZoneInfo(
        int zone,
        int tier) {
    @Override
    public String toString() {
        return String.format("Zone: %d | Tier: %d", zone, tier);
    }
}
