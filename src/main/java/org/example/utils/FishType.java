package org.example.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.worldgen.zone.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum FishType {

    MINNOW("Fish_Minnow_Item", 3f, 1.5f, 0.5f, 2.0f, 0.80f, 0.50f, 1f, 10f, 0.05f, 200,
            new boolean[][]{{true,true,true},{false,false,false},{true,true,true},{false,false,false}}),

    BLUEGILL("Fish_Bluegill_Item", 5f, 1.0f, 2.0f, 5.0f, 0.70f, 0.30f, 5f, 15f, 0.20f, 180,
            new boolean[][]{{false,false,false},{true,true,true},{false,false,false},{true,true,true}});

   /* SALMON("Fish_Salmon_Item", 8f, 0.7f, 1.0f, 3.5f, 0.60f, 0.50f, 18f, 45f, 0.50f, 100),

    TROUT_RAINBOW("Fish_Trout_Rainbow_Item", 9f, 0.6f, 1.5f, 4.0f, 0.65f, 0.35f, 22f, 50f, 0.40f, 90),

    CATFISH("Fish_Catfish_Item", 12f, 0.4f, 3.0f, 8.0f, 0.50f, 0.25f, 30f, 30f, 0.80f, 70),

    PIKE("Fish_Pike_Item", 10f, 0.6f, 2.0f, 6.0f, 0.40f, 0.30f, 35f, 65f, 0.90f, 60),

    SNAPJAW("Fish_Snapjaw_Item", 7f, 0.8f, 0.8f, 2.5f, 0.70f, 0.20f, 28f, 55f, 0.60f, 80),

    CLOWNFISH("Fish_Clownfish_Item", 2f, 2.0f, 1.0f, 3.0f, 0.90f, 0.60f, 3f, 12f, 0.10f, 160),

    TANG_BLUE("Fish_Tang_Blue_Item", 6f, 0.9f, 1.0f, 3.0f, 0.70f, 0.40f, 12f, 25f, 0.25f, 120),

    TANG_CHEVRON("Fish_Tang_Chevron_Item", 6f, 0.9f, 1.0f, 3.0f, 0.70f, 0.40f, 12f, 25f, 0.25f, 120),

    TANG_LEMON_PEEL("Fish_Tang_Lemon_Peel_Item", 5.5f, 0.95f, 1.2f, 3.5f, 0.70f, 0.40f, 10f, 28f, 0.22f, 120),

    TANG_SAILFIN("Fish_Tang_Sailfin_Item", 7f, 0.8f, 1.0f, 3.5f, 0.65f, 0.35f, 14f, 30f, 0.30f, 110),

    JELLYFISH_BLUE("Fish_Jellyfish_Blue_Item", 4f, 1.2f, 3.0f, 8.0f, 0.60f, 0.50f, 6f, 5f, 0.30f, 130),

    JELLYFISH_CYAN("Fish_Jellyfish_Cyan_Item", 4f, 1.2f, 3.0f, 8.0f, 0.60f, 0.50f, 6f, 5f, 0.30f, 130),

    JELLYFISH_GREEN("Fish_Jellyfish_Green_Item", 4.5f, 1.1f, 2.5f, 7.0f, 0.60f, 0.50f, 8f, 8f, 0.35f, 120),

    JELLYFISH_RED("Fish_Jellyfish_Red_Item", 5f, 1.0f, 2.5f, 7.0f, 0.65f, 0.45f, 10f, 10f, 0.35f, 110),

    JELLYFISH_YELLOW("Fish_Jellyfish_Yellow_Item", 4f, 1.2f, 3.0f, 8.0f, 0.60f, 0.50f, 6f, 5f, 0.30f, 130),

    PUFFERFISH("Fish_Pufferfish_Item", 5f, 1.0f, 0.8f, 3.0f, 0.85f, 0.40f, 9f, 15f, 0.20f, 100),

    EEL_MORAY("Fish_Eel_Moray_Item", 10f, 0.5f, 3.0f, 7.0f, 0.30f, 0.20f, 20f, 40f, 1.20f, 60),

    FROSTGILL("Fish_Frostgill_Item", 6f, 0.9f, 0.8f, 2.5f, 0.65f, 0.30f, 25f, 35f, 0.35f, 50),

    CRAB("Fish_Crab_Item", 15f, 0.3f, 4.0f, 10.0f, 0.40f, 0.15f, 24f, 8f, 0.20f, 65),

    LOBSTER("Fish_Lobster_Item", 14f, 0.35f, 5.0f, 12.0f, 0.35f, 0.15f, 26f, 12f, 0.40f, 55),

    SHELLFISH_LAVA("Fish_Shellfish_Lava_Item", 8f, 1.2f, 1.5f, 4.0f, 0.60f, 0.30f, 32f, 20f, 0.30f, 30),

    JELLYFISH_MAN_OF_WAR("Fish_Jellyfish_Man_Of_War_Item", 8f, 0.7f, 2.0f, 5.0f, 0.75f, 0.35f, 20f, 15f, 0.50f, 20),

    PIRANHA("Fish_Piranha_Item", 6f, 1.2f, 0.5f, 1.5f, 0.80f, 0.30f, 40f, 75f, 0.30f, 25),

    PIRANHA_BLACK("Fish_Piranha_Black_Item", 8f, 1.0f, 0.5f, 1.5f, 0.80f, 0.25f, 52f, 85f, 0.35f, 15),

    SHARK("Fish_Shark_Hammerhead_Item", 20f, 0.2f, 4.0f, 9.0f, 0.75f, 0.30f, 70f, 90f, 3.50f, 10),

    TRILOBITE("Fish_Trilobite_Item", 18f, 0.2f, 6.0f, 14.0f, 0.50f, 0.10f, 75f, 15f, 0.50f, 8),

    TRILOBITE_BLACK("Fish_Trilobite_Black_Item", 20f, 0.2f, 7.0f, 15.0f, 0.50f, 0.10f, 85f, 20f, 0.55f, 5),

    WHALE_HUMPBACK("Fish_Whale_Humpback_Item", 30f, 0.15f, 8.0f, 20.0f, 0.40f, 0.05f, 100f, 40f, 15.00f, 2);
*/


    private final String itemId;
    public final float maxStamina; // how long the fish actively fights (seconds)
    public final float staminaRegen; // recovery rate per second when tired
    public final float minTimeToChangeSides; // min seconds before next direction flip
    public final float maxTimeToChangeSides; // max seconds before next direction flip
    public final float maxAngle; // width of the arc (radians) relative to cast angle
    public final float tiredFishStrenghtModifier; // strength multiplier when out of stamina
    public final float strength; // raw pull force — main difficulty driver (1=Minnow, 100=Whale)
    public final float speed; // reserved for future use
    public final float size; // average size in meters (also signals reward value)
    public final int weight; // rarity weight — higher = more common
    public final boolean[][] zoneXtier;

    FishType(String itemId, float maxStamina, float staminaRegen,
             float minTimeToChangeSides, float maxTimeToChangeSides,
             float maxAngle, float tiredFishStrenghtModifier,
             float strength, float speed, float size, int weight, boolean[][] zoneXtier) {
        this.itemId = itemId;
        this.maxStamina = maxStamina;
        this.staminaRegen = staminaRegen;
        this.minTimeToChangeSides = minTimeToChangeSides;
        this.maxTimeToChangeSides = maxTimeToChangeSides;
        this.maxAngle = maxAngle;
        this.tiredFishStrenghtModifier = tiredFishStrenghtModifier;
        this.strength = strength;
        this.speed = speed;
        this.size = size;
        this.weight = weight;
        this.zoneXtier = zoneXtier;
    }

    public static boolean zoneXtierCheck(FishType fishType, ZoneInfo zoneInfo){
        return fishType.zoneXtier[zoneInfo.zone()][zoneInfo.tier()];
    }

    public String getItemId() {
        return itemId;
    }

    public static FishType getWeightedRandom(Random random) {
        int totalWeight = 0;
        for (FishType f : values())
            totalWeight += f.weight;

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (FishType f : values()) {
            cumulative += f.weight;
            if (roll < cumulative)
                return f;
        }
        return MINNOW; // fallback, should never be reached
    }

    public static ArrayList<FishType> getFishPool(ZoneInfo zoneInfo){
        ArrayList<FishType> fishPool = new ArrayList<FishType>();

        for (FishType f : values()){
            if(zoneXtierCheck(f,zoneInfo)) fishPool.add(f);
        }

        return fishPool;
    }

    public enum SizeClass {
        TINY(0.3f),
        SMALL(0.7f),
        MEDIUM(1f),
        BIG(2f),
        COLOSSAL(3f);

        public final float particleScale;

        SizeClass(float particleScale) {
            this.particleScale = particleScale;
        }
    }

    public enum ActiveTime {
        DAY(4,20),
        NIGHT(21,3),
        AFTERNOON(12,20),
        MORNING(4,11),
        DUSK(21,24),
        DAWN(0,3),
        ALL(0,24);

        public final int start;
        public final int end;

        ActiveTime(int start, int end){this.start = start; this.end = end;}
    }

    public SizeClass getSizeClass() {
        if (size < 0.15f) return SizeClass.TINY;
        if (size < 0.40f) return SizeClass.SMALL;
        if (size < 1.00f) return SizeClass.MEDIUM;
        if (size < 4.00f) return SizeClass.BIG;
        return SizeClass.COLOSSAL;
    }

    @Override
    public String toString() {
        return String.format(
            """
            [%s]
              Item      : %s
              Strength  : %.0f  |  Size: %.2fm  |  Weight(rarity): %d
              Stamina   : %.1fs (regen %.2f/s)  |  Tired modifier: %.0f%%
              Side change: %.1f - %.1f
            """,
            name(), itemId,
            strength, size, weight,
            maxStamina, staminaRegen, tiredFishStrenghtModifier * 100f,
            minTimeToChangeSides, maxTimeToChangeSides);
        }
    }

