package org.example.components;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Component;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.utils.FishType;
import org.joml.Vector3d;

import java.util.UUID;


public class FishComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, FishComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, FishComponent> type) {TYPE = type;}

    public static ComponentType<EntityStore, FishComponent> getComponentType(){ return TYPE;}

    @NullableDecl
    @Override
    public Component<EntityStore> clone(){
        return new FishComponent();
    }

    public FishComponent(){}

    public FishComponent(UUID playerId, float initialDistance, double initialAngle, FishType fishType){
        this.playerId = playerId;
        this.currentDistance = initialDistance;
        this.initialAngle = initialAngle;
        this.orbitAngle = initialAngle;


        //default values:
        this.maxStamina = fishType.maxStamina;
        this.currentStamina = fishType.maxStamina;
        this.staminaRegen = fishType.staminaRegen;
        this.minTimeToChangeSides  = fishType.minTimeToChangeSides;
        this.maxTimeToChangeSides = fishType.maxTimeToChangeSides;
        this.maxAngle = fishType.maxAngle;
        this.tiredFishStrenghtModifier = fishType.tiredFishStrenghtModifier;
        this.strength = fishType.strength;
        this.speed = fishType.speed;
        this.size = fishType.size;
        this.itemId = fishType.getItemId();
        this.type = fishType;

        //setup fish sounds
        this.swimFastAudio = SoundEvent.getAssetMap().getIndex(SWIM_FAST_SFX);
        this.swimSlowAudio = SoundEvent.getAssetMap().getIndex(SWIM_SLOW_SFX);
        this.waterMoveOutAudio = SoundEvent.getAssetMap().getIndex(WATER_MOVE_OUT_SFX);
    }

    public FishComponent(UUID playerId, float initialDistance, double initialAngle){ //default fish
        this.playerId = playerId;
        this.currentDistance = initialDistance;
        this.initialAngle = initialAngle;
        this.orbitAngle = initialAngle;


        //default values:
        this.maxStamina = 5f;
        this.currentStamina = this.maxStamina;
        this.staminaRegen = 1f;
        this.minTimeToChangeSides  = 2f;
        this.maxTimeToChangeSides = 5f;
        this.maxAngle = 0.7f;
        this.tiredFishStrenghtModifier = 0.3f;
        this.strength = 2f;
        this.speed = 1f;
        this.size = 0.2f;
        this.itemId = "Fish_Bluegill_Item";

        //setup fish sounds
        this.swimFastAudio = SoundEvent.getAssetMap().getIndex(SWIM_FAST_SFX);
        this.swimSlowAudio = SoundEvent.getAssetMap().getIndex(SWIM_SLOW_SFX);
        this.waterMoveOutAudio = SoundEvent.getAssetMap().getIndex(WATER_MOVE_OUT_SFX);
    }

    public float getCurrentStamina() {
        return currentStamina;
    }

    public void setCurrentStamina(float currentStamina) {
        this.currentStamina = currentStamina;
    }

    public float getStaminaRegen() {
        return staminaRegen;
    }

    public void setStaminaRegen(float staminaRegen) {
        this.staminaRegen = staminaRegen;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }


    //initial variables
    public double initialAngle = 0f;
    public UUID playerId;

    //spatial variables:
    public float orbitVelocity = 0f;
    public float distanceVelocity = 0f;
    public float currentDistance;
    public double orbitAngle = 0f;

    //movement variables
    public float currentStamina;
    public float maxStamina;
    public float staminaRegen;
    public float minTimeToChangeSides;
    public float maxTimeToChangeSides;
    public float maxAngle;
    public float tiredFishStrenghtModifier;

    //physical stats
    public float strength;
    public float speed;
    public float size; //meters

    //item ID
    public String itemId;

    public FishType type;



    public final String SWIM_FAST_SFX = "SFX_Fish_Fast";
    public final String SWIM_SLOW_SFX = "SFX_Fish_Slow";
    public final String WATER_MOVE_OUT_SFX = "SFX_Water_MoveOut";

    public int getSwimFastAudio() {
        return swimFastAudio;
    }

    public int getSwimSlowAudio() {
        return swimSlowAudio;
    }

    public int getWaterMoveOutAudio() {
        return waterMoveOutAudio;
    }

    private int waterMoveOutAudio;
    private int swimFastAudio;
    private int swimSlowAudio;










}
