package org.example.components;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Component;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

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

    public FishComponent(UUID playerId){
        this.playerId = playerId;
    }

    public float getStamina() {
        return stamina;
    }

    public void setStamina(float stamina) {
        this.stamina = stamina;
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


    private UUID playerId;
    private float stamina;
    private float staminaRegen;
    private float timeToRecover;
    private float timeToCatch;
    private float speed;
    private boolean swimDirection;






}
