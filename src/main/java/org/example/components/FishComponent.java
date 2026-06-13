package org.example.components;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Component;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
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

    public FishComponent(UUID playerId, float initialDistance, double initialAngle){
        this.playerId = playerId;
        this.currentDistance = initialDistance;
        this.initialAngle = initialAngle;
        this.orbitAngle = initialAngle;
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

    public boolean isSwimmingLeft() {
        return swimmingLeft;
    }

    public void setSwimmingLeft(boolean swimmingLeft) {
        this.swimmingLeft = swimmingLeft;
    }


    //initial variables
    public double initialAngle = 0f;
    public Vector3d initialPlayerPos;
    public UUID playerId;

    //spatial variables:
    public float orbitVelocity = 0f;
    public float distanceVelocity = 0f;
    public boolean swimmingLeft = true;
    public float currentDistance;
    public double orbitAngle = 0f;

    //stamina variables
    public float currentStamina = 5f;
    public float maxStamina = 5f;
    public float staminaRegen = 1f;
    public float timeToRecover;

    //physical stats
    public float strength;
    public float speed;
    public float size; //meters













}
