package org.example.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.components.FishComponent;
import org.example.components.FishermanComponent;
import org.example.events.CatchFishEvent;
import org.example.events.StopFishingEvent;
import org.joml.Vector3d;

import java.util.Random;

public class FishingSystem extends EntityTickingSystem<EntityStore> {

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery(){
        return Query.and(FishComponent.getComponentType());
    }

    private final Random random = new Random();

    boolean fishRested = true;
    float currentPlayerStrenght;
    float currentFishStrenght;
    float fishStrenght = 2f;
    float playerStrenght = 5f;
    float timeTilSideChange = 0f;
    float targetAngle = 0.3f;
    float timeSameSide = 0f;
    float totalTime = 0f;
    float timeAtMaxTension = 0f;
    final float MIN_TIME = 2f;
    final float MAX_TIME = 5f;
    final float MAX_ANGLE = 0.7f;
    final float SPEED_MODIFIER = 0.4f;
    float tension = 0f;
    final float MAX_TENSION = 10f;

    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        var fishComponent = archetypeChunk.getComponent(index, FishComponent.getComponentType());
        var player = store.getExternalData().getRefFromUUID(fishComponent.getPlayerId());
        var playerRef = store.getComponent(player, PlayerRef.getComponentType());
        var world  = store.getExternalData().getWorld();

        var bobberTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        var playerTransform = store.getComponent(player, TransformComponent.getComponentType());

        var fishermanComponent = store.getComponent(player, FishermanComponent.getComponentType());


        Vector3d playerPos = new Vector3d(playerTransform.getPosition());
        Vector3d bobberPos = new Vector3d(bobberTransform.getPosition());

        int fluidId = world.getFluidId((int)bobberPos.x, (int)Math.floor(bobberPos.y), (int)bobberPos.z);
        boolean inWater = (fluidId == 7||fluidId == 8||fluidId == 12);
        double distanceXZ = new Vector3d(playerPos.x, 0, playerPos.z)
                .distance(new Vector3d(bobberPos.x, 0, bobberPos.z));








        if(timeSameSide >= timeTilSideChange){
            fishStrenght *= -1;
            targetAngle = newAngle();
            timeTilSideChange = newTimeTilSideChange();
            timeSameSide = 0f;
        }

        fishComponent.orbitAngle += (fishStrenght * dt) * 0.4f;
        fishComponent.orbitAngle = Math.clamp(fishComponent.orbitAngle, fishComponent.initialAngle - targetAngle, fishComponent.initialAngle + targetAngle);

        if(!fishRested){
            currentFishStrenght = fishStrenght * 0.5f;
        }else {
            if (Math.abs(fishComponent.orbitAngle) >= targetAngle * 0.9f) {
                currentFishStrenght = fishStrenght;
            } else {
                currentFishStrenght = fishStrenght * 0.8f;
            }
        }

        currentFishStrenght = Math.abs(currentFishStrenght);
        currentPlayerStrenght = (float)getForceExerted(fishermanComponent.getHeight()) * playerStrenght;

        tension = currentFishStrenght + currentPlayerStrenght;

        if (fishermanComponent.getSide() * fishStrenght > 0 && (fishermanComponent.getSide() >= 0.50f || fishermanComponent.getSide() <= -0.50f)) { // 1.57 is approx 90 degrees
            if(fishermanComponent.getHeight() >= 0f) {
                fishComponent.currentDistance += Math.clamp((((currentFishStrenght * dt) - (currentPlayerStrenght * dt)) * SPEED_MODIFIER), -10f, 0f); // Success!
            }else{
                fishComponent.currentDistance += Math.clamp((((currentFishStrenght * dt) - (currentPlayerStrenght * dt)) * SPEED_MODIFIER), 0f, currentFishStrenght); // Success!
            }
        } else {
            fishComponent.currentDistance += (currentFishStrenght * dt) * SPEED_MODIFIER; // Fail!
        }

        double targetX = playerPos.x + Math.cos(fishComponent.orbitAngle) * fishComponent.currentDistance;
        double targetZ = playerPos.z + Math.sin(fishComponent.orbitAngle) * fishComponent.currentDistance;
        bobberTransform.setPosition(new Vector3d(targetX, bobberPos.y, targetZ));


        if(distanceXZ >= 20){
            StopFishingEvent.dispatch(player);
        } else if (distanceXZ <= 2) {
            CatchFishEvent.dispatch(player);
            StopFishingEvent.dispatch(player);
        }

        if (tension >= MAX_TENSION * 2){
            playerRef.sendMessage(Message.raw("2X TENSION REACHED: %f".formatted(tension)));
            StopFishingEvent.dispatch(player);
        }

        if(tension >= MAX_TENSION){
            timeAtMaxTension += dt;
        }else{
            timeAtMaxTension = 0f;
        }

        if(timeAtMaxTension >= 2){
            playerRef.sendMessage(Message.raw("LINHA QUEBROU! "));
            StopFishingEvent.dispatch(player);
        }


        if(fishComponent.currentStamina <= 0f){
            fishRested = false;
        }

        if(fishComponent.currentStamina >= fishComponent.maxStamina && !fishRested){
            fishComponent.currentStamina = fishComponent.maxStamina;
            fishRested = true;
        }


        if(fishRested){
            fishComponent.currentStamina -= dt;
        }else{
            fishComponent.currentStamina += (dt * fishComponent.staminaRegen);
        }

        //playerRef.sendMessage(Message.raw("TEN: %f (%fs)".formatted(tension,timeAtMaxTension)));
        playerRef.sendMessage(Message.raw("%b".formatted(fishRested)));




        fishermanComponent.setSide(0);
        fishermanComponent.setHeight(0);
        timeSameSide += dt;
        totalTime += dt;
    }

    private float newTimeTilSideChange(){
        return MIN_TIME + random.nextFloat() * (MAX_TIME - MIN_TIME);
    }

    private float newAngle(){
        return random.nextFloat() * (MAX_ANGLE);
    }

    private double getForceExerted(double height){
        return (Math.clamp(height, -0.20f, 0.20f))/0.20f;
    }


}
