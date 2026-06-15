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

        this.type = fishType;

        currentStamina = type.maxStamina;

        //setup fish sounds
        this.swimFastAudio = SoundEvent.getAssetMap().getIndex(SWIM_FAST_SFX);
        this.swimSlowAudio = SoundEvent.getAssetMap().getIndex(SWIM_SLOW_SFX);
        this.waterMoveOutAudio = SoundEvent.getAssetMap().getIndex(WATER_MOVE_OUT_SFX);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    //initial variables
    public double initialAngle = 0f;
    public UUID playerId;

    //spatial variables:
    public float orbitVelocity = 0f;
    public float distanceVelocity = 0f;
    public float currentDistance;
    public double orbitAngle = 0f;
    public float currentStamina;

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
