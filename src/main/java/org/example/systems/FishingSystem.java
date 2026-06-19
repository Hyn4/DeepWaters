package org.example.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.components.FishComponent;
import org.example.components.FishermanComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.CameraControllerEvent;
import org.example.events.CatchFishEvent;
import org.example.events.StopFishingEvent;
import org.example.utils.CameraState;
import org.joml.Vector3d;

import java.util.Random;

public class FishingSystem extends EntityTickingSystem<EntityStore> {

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(FishComponent.getComponentType());
    }

    private final Random random = new Random();

    PlayerRef playerRef;


    //local
    boolean fishRested = true;
    float totalTime = 0f;
    float timeTilReelSound = 0f;
    float timeTilSwimSound = 0f;
    float timeAtMaxTension = 0f;
    float timeSameSide = 0f;
    float tension = 0f;
    float currentPlayerStrenght;
    float currentFishStrenght;
    float timeTilSideChange = 0f;
    float targetAngle = 0.3f;
    final float SPEED_MODIFIER = 0.2f;
    final float LERP_MODIFIER = 1.0f;
    float targetOrbitVelocity;


    //external
    float fishStrengthVector;
    float maxTension;

    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        var fishComponent = archetypeChunk.getComponent(index, FishComponent.getComponentType());
        var player = store.getExternalData().getRefFromUUID(fishComponent.getPlayerId());
        playerRef = store.getComponent(player, PlayerRef.getComponentType());
        var world = store.getExternalData().getWorld();

        var bobberTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        var playerTransform = store.getComponent(player, TransformComponent.getComponentType());

        var fishermanComponent = store.getComponent(player, FishermanComponent.getComponentType());
        var playerRPGComponent = store.getComponent(player, PlayerRPGComponent.getComponentType());

        Vector3d playerPos = new Vector3d(playerTransform.getPosition());
        Vector3d bobberPos = new Vector3d(bobberTransform.getPosition());

        int fluidId = world.getFluidId((int) bobberPos.x, (int) Math.floor(bobberPos.y), (int) bobberPos.z);
        boolean inWater = (fluidId == 7 || fluidId == 8 || fluidId == 12 || fluidId == 2);
        double distanceXZ = new Vector3d(playerPos.x, 0, playerPos.z)
                .distance(new Vector3d(bobberPos.x, 0, bobberPos.z));

        maxTension = (fishComponent.type.strength + playerRPGComponent.getFishermanStrenght()) * fishermanComponent.getMaxTension();

        //if (!inWater) playerRef.sendMessage(Message.raw("NOT IN WATER!!!"));

        if(fishermanComponent.isReeling()){
            GET_REEL_PLAYER_INPUT(commandBuffer, bobberPos, playerPos, player, fishermanComponent);
        }


        float maxFishSpeed = (fishComponent.type.speed / 100.0f) * 0.2f;
        fishStrengthVector = fishComponent.type.strength * fishComponent.side;

        if (timeSameSide >= timeTilSideChange) {
            ChangeSides(fishComponent);
        }

        SET_CURRENT_FISH_STRENGTH(store, fishComponent, player, bobberPos);

        // Horizontal Counter-Steering Math
        double playerSide = fishermanComponent.getSide();
        float fishDirection = Math.signum(fishComponent.orbitVelocity);
        boolean isCounterSteering = false;
        float counterSteerTension = 0f;

        if (fishermanComponent.isReeling() && Math.signum(playerSide) != fishDirection && Math.abs(playerSide) > 0.1f)
            isCounterSteering = true;


        SET_ORBIT_ANGLE(dt, fishComponent, playerSide, fishermanComponent.isReeling(), maxFishSpeed, playerRPGComponent.getFishermanStrenght());

        // --- B. SMOOTHED TENSION & DISTANCE ---
        float targetTension = 0f;
        float targetDistanceChange = 0f;
 
        if (!fishermanComponent.isReeling()) {
            // Player let go of the wire
            targetDistanceChange = currentFishStrenght * dt;
            targetTension = 0f;
            currentPlayerStrenght = 0f;
        } else {
            double height = fishermanComponent.getHeight();
            float forceRatio = (float) getForceExerted(height); // -1.0 to 1.0
            float pullEffectiveness = (float) Math.clamp(1.0 - Math.abs(playerSide), 0.0, 1.0);
 
            if (height >= 0.05f) {
                // Looking Up (Reeling In)
                currentPlayerStrenght = forceRatio * playerRPGComponent.getFishermanStrenght();
                float effectivePlayerStrength = currentPlayerStrenght * pullEffectiveness;
                targetDistanceChange = Math.min(0f, (currentFishStrenght - effectivePlayerStrength) * dt * SPEED_MODIFIER);
                targetTension = currentFishStrenght + currentPlayerStrenght;
            } else if (height <= -0.05f) {
                // Looking Down (Yielding line)
                currentPlayerStrenght = 0f;
                float yieldPercentage = (float) Math.abs(forceRatio);
                targetDistanceChange = currentFishStrenght * yieldPercentage * dt * SPEED_MODIFIER;
                targetTension = currentFishStrenght * (1.0f - yieldPercentage);
            } else {
                // Looking Center (close to zero)
                currentPlayerStrenght = 0f;
                float maxPlayerStrength = playerRPGComponent.getFishermanStrenght() * pullEffectiveness;
                if (currentFishStrenght > maxPlayerStrength) {
                    targetDistanceChange = (currentFishStrenght - maxPlayerStrength) * dt * SPEED_MODIFIER;
                } else {
                    targetDistanceChange = 0f;
                }
                targetTension = currentFishStrenght;
            }
        }


        targetDistanceChange = Math.clamp(targetDistanceChange, -0.1f, maxFishSpeed);

        if (distanceXZ >= 7) {
            if(targetDistanceChange > 0) {
                targetDistanceChange = 0f;
                targetTension = currentFishStrenght;
            }
        }
        else if (distanceXZ <= 2) {
            SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getWaterMoveOutAudio(), SoundCategory.SFX, playerPos, store);
            CatchFishEvent.dispatch(player, fishComponent.type.getItemId());
            StopFishingEvent.dispatch(player);
        }

        tension = lerp(tension, targetTension, dt * LERP_MODIFIER);

        //apply smoothing to the distance velocity
        fishComponent.distanceVelocity = lerp(fishComponent.distanceVelocity, targetDistanceChange, dt * LERP_MODIFIER);
        fishComponent.currentDistance += fishComponent.distanceVelocity;

        //playerRef.sendMessage(Message.raw("side: %f".formatted(playerSide)));

        //set position based on orbit angle and distance
        double targetX = playerPos.x + Math.cos(fishComponent.orbitAngle) * fishComponent.currentDistance;
        double targetZ = playerPos.z + Math.sin(fishComponent.orbitAngle) * fishComponent.currentDistance;
        bobberTransform.setPosition(new Vector3d(targetX, bobberPos.y, targetZ));


        MANAGE_TENSION(dt, store, fishermanComponent, playerRef, player, playerPos);

        MANAGE_FISH_STAMINA(dt, fishComponent, isCounterSteering, playerSide);

        //Spawn water sprint particle on bobber
        float splashScale = fishComponent.type.getSizeClass().particleScale;
        ParticleUtil.spawnParticleEffect("Water_Sprint", bobberPos, 0f, 0f, 0f, splashScale,0.2f , commandBuffer);

        PLAY_ROD_SFX(store, fishermanComponent, fishComponent, player, playerPos);



        INCREASE_TIMERS_RESET_INPUT(dt, fishermanComponent);
    }

    private void INCREASE_TIMERS_RESET_INPUT(float dt, FishermanComponent fishermanComponent) {
        fishermanComponent.setSide(0);
        fishermanComponent.setHeight(0);
        timeSameSide += dt;
        totalTime += dt;
        timeTilSwimSound += dt;
        timeTilReelSound += dt;
    }

    private void PLAY_ROD_SFX(@NonNullDecl Store<EntityStore> store, FishermanComponent fishermanComponent, FishComponent fishComponent, Ref<EntityStore> player, Vector3d playerPos) {
        //play reel in / reel out / max tension SFX
        if(timeTilReelSound >= 0.3f) {
            if(tension < maxTension) {
                if (fishComponent.distanceVelocity > 0) {
                    SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getReelOutAudio(), SoundCategory.SFX, playerPos, store);
                } else if (fishComponent.distanceVelocity < 0) {
                    SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getReelInAudio(), SoundCategory.SFX, playerPos, store);
                }
            }else{
                SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getMaxTensionAudio(), SoundCategory.SFX, playerPos, store);
            }
            timeTilReelSound = 0f;
        }
    }

    private void MANAGE_FISH_STAMINA(float dt, FishComponent fishComponent, boolean isCounterSteering, double playerSide) {
        //fish resting and stamina toggles and manager
        if (fishComponent.currentStamina <= 0f) {
            fishRested = false;
        }
        if (fishComponent.currentStamina >= fishComponent.type.maxStamina && !fishRested) {
            fishComponent.currentStamina = fishComponent.type.maxStamina;
            fishRested = true;
        }
        if (fishRested) {
            float tireDrain = dt;
            if (isCounterSteering) {
                tireDrain += dt * 2.0f * (float) Math.abs(playerSide);
            }
            fishComponent.currentStamina -= tireDrain;
        }
        else {
            fishComponent.currentStamina += (dt * fishComponent.type.staminaRegen);
        }
    }

    private void MANAGE_TENSION(float dt, @NonNullDecl Store<EntityStore> store, FishermanComponent fishermanComponent, PlayerRef playerRef, Ref<EntityStore> player, Vector3d playerPos) {
        //Rod tension managers
        if (tension >= maxTension * 2) {
            playerRef.sendMessage(Message.raw("2X TENSION REACHED: %f".formatted(tension)));
            StopFishingEvent.dispatch(player);
        }
        if (tension >= maxTension) {
            CameraControllerEvent.dispatch(player, CameraState.STRUGGLE, timeAtMaxTension/40f); // esse 40f é pra transformar o intervalo de 0-2 em 0-0.05 (intensidade do camera shake é mais sensível)
            timeAtMaxTension += dt;
        } else {
            timeAtMaxTension = 0f;
        }
        if (timeAtMaxTension >= 2) {
            SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getLineBreakAduio(), SoundCategory.SFX, playerPos, store);
            //playerRef.sendMessage(Message.raw("LINHA QUEBROU! "));
            StopFishingEvent.dispatch(player);
            timeAtMaxTension = 0f;
        }
    }

    private void SET_CURRENT_FISH_STRENGTH(@NonNullDecl Store<EntityStore> store, FishComponent fishComponent, Ref<EntityStore> player, Vector3d bobberPos) {
        if (!fishRested) {
            currentFishStrenght = fishStrengthVector * fishComponent.type.tiredFishStrenghtModifier;
            if(timeTilSwimSound >= 1.1f) {
                timeTilSwimSound = 0f;
                if (fishComponent.getSwimSlowAudio() != 0)
                    SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getSwimSlowAudio(), SoundCategory.SFX, bobberPos, store);
            }

        } else {
            if(timeTilSwimSound >= 1.1f){
                timeTilSwimSound = 0f;
                if(fishComponent.getSwimFastAudio() != 0)
                    SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getSwimFastAudio(), SoundCategory.SFX, bobberPos, store);
            }

            if (Math.abs(fishComponent.orbitAngle) >= targetAngle * 0.9f) {
                currentFishStrenght = fishStrengthVector;
            } else {
                currentFishStrenght = fishStrengthVector * 0.8f;
            }
        }
        currentFishStrenght = Math.abs(currentFishStrenght);
    }

    private void SET_ORBIT_ANGLE(float dt, @NonNullDecl FishComponent fishComponent, double playerSide, boolean isReeling, float maxFishSpeed, float playerStrength) {
        // --- A. SMOOTHED ANGLE MOVEMENT ---
        targetOrbitVelocity = (maxFishSpeed * 10f) * fishComponent.side * dt * 5;

        if (isReeling) {
            var aux = (float) (playerSide * dt * (-1) * 5);
            playerRef.sendMessage(Message.raw("Difference: %f".formatted(targetOrbitVelocity-aux) ));
            targetOrbitVelocity += aux;
        }

        // Smoothing the velocity change so the fish doesn't snap instantly when
        // changing direction
        fishComponent.orbitVelocity = lerp(fishComponent.orbitVelocity, targetOrbitVelocity, dt * LERP_MODIFIER);

        fishComponent.orbitAngle += fishComponent.orbitVelocity * dt;
 
        fishComponent.orbitAngle = Math.clamp(fishComponent.orbitAngle, fishComponent.initialAngle - targetAngle,
                fishComponent.initialAngle + targetAngle);
    }

    private void ChangeSides(@NonNullDecl FishComponent fishComponent) {
        fishComponent.side *= -1;
        targetAngle = newAngle(fishComponent.type.maxAngle);
        timeTilSideChange = newTimeTilSideChange(fishComponent.type.minTimeToChangeSides, fishComponent.type.maxTimeToChangeSides) * 3f;
        timeSameSide = 0f;
    }

    private static void GET_REEL_PLAYER_INPUT(@NonNullDecl CommandBuffer<EntityStore> commandBuffer, Vector3d bobberPos, Vector3d playerPos, Ref<EntityStore> player, @NonNullDecl FishermanComponent fishermanComponent) {
        var playerToBobber = new Vector3d(bobberPos).sub(playerPos).normalize();
        var playerHeadRotation = commandBuffer.getComponent(player, HeadRotation.getComponentType());
        var playerHeadDirection = playerHeadRotation.getDirection().normalize();

        double side = (playerHeadDirection.x * playerToBobber.z) - (playerHeadDirection.z * playerToBobber.x);
        double height = playerHeadRotation.getRotation().pitch() / 1.56f;

        fishermanComponent.setHeight(height);
        fishermanComponent.setSide(side);
    }

    private float newTimeTilSideChange(float MIN_TIME, float MAX_TIME) {
        return MIN_TIME + random.nextFloat() * (MAX_TIME - MIN_TIME);
    }

    private float newAngle(float MAX_ANGLE) {
        var MIN_ANGLE = 0.2f;
        return MIN_ANGLE + random.nextFloat() * (MAX_ANGLE - MIN_ANGLE);
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    private double getForceExerted(double height) {
        return (Math.clamp(height, -0.15f, 0.15f)) / 0.15f;
    }


}
