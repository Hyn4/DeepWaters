package org.example.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.Particle;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
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

    int reelOutAudio = 0;
    int swimSlowAudio = 0;
    int swimFastAudio = 0;
    int reelInAudio =0;
    boolean pulling = false;
    boolean fishRested = true;
    float currentPlayerStrenght;
    float currentFishStrenght;
    float fishStrenght = 2f;
    float playerStrenght = 5f;
    float timeTilSideChange = 0f;
    float targetAngle = 0.3f;
    float timeSameSide = 0f;
    float totalTime = 0f;
    float timeTilReelSound = 0f;
    float timeTilSwimSound = 0f;
    float timeAtMaxTension = 0f;
    final float MIN_TIME = 2f;
    final float MAX_TIME = 5f;
    final float MAX_ANGLE = 0.7f;
    final float TIRED_FISH_STRENGHT_MODIFIER = 0.3f;
    final float SPEED_MODIFIER = 0.4f;
    float tension = 0f;
    final float MAX_TENSION = 6.8f;

    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        var fishComponent = archetypeChunk.getComponent(index, FishComponent.getComponentType());
        var player = store.getExternalData().getRefFromUUID(fishComponent.getPlayerId());
        var playerRef = store.getComponent(player, PlayerRef.getComponentType());
        var world = store.getExternalData().getWorld();
        var playerObj = store.getComponent(player, Player.getComponentType());
        reelInAudio = SoundEvent.getAssetMap().getIndex("SFX_Reel_In");
        swimFastAudio = SoundEvent.getAssetMap().getIndex("SFX_Fish_Fast");
        swimSlowAudio = SoundEvent.getAssetMap().getIndex("SFX_Fish_Slow");
        reelOutAudio = SoundEvent.getAssetMap().getIndex("SFX_Reel_Out");


        var bobberTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        var playerTransform = store.getComponent(player, TransformComponent.getComponentType());

        var fishermanComponent = store.getComponent(player, FishermanComponent.getComponentType());
        var playerRPGComponent = store.getComponent(player, PlayerRPGComponent.getComponentType());




        Vector3d playerPos = new Vector3d(playerTransform.getPosition());
        Vector3d bobberPos = new Vector3d(bobberTransform.getPosition());

        int fluidId = world.getFluidId((int) bobberPos.x, (int) Math.floor(bobberPos.y), (int) bobberPos.z);
        boolean inWater = (fluidId == 7 || fluidId == 8 || fluidId == 12);
        double distanceXZ = new Vector3d(playerPos.x, 0, playerPos.z)
                .distance(new Vector3d(bobberPos.x, 0, bobberPos.z));


        playerStrenght = playerRPGComponent.getFishermanStrenght();
        if (playerStrenght == 0)
            playerStrenght = PlayerRPGComponent.getDefaultStrenght();

        if (!inWater)
            playerRef.sendMessage(Message.raw("NOT IN WATER!!!"));

        if (timeSameSide >= timeTilSideChange) {
            fishStrenght *= -1;
            targetAngle = newAngle();
            timeTilSideChange = newTimeTilSideChange();
            timeSameSide = 0f;
        }

        // --- A. SMOOTHED ANGLE MOVEMENT ---
        float targetOrbitVelocity = fishStrenght * 0.4f;
        // Smoothing the velocity change so the fish doesn't snap instantly when
        // changing direction
        fishComponent.orbitVelocity = lerp(fishComponent.orbitVelocity, targetOrbitVelocity, dt * 5.0f);

        fishComponent.orbitAngle += fishComponent.orbitVelocity * dt;
        fishComponent.orbitAngle = Math.clamp(fishComponent.orbitAngle, fishComponent.initialAngle - targetAngle,
                fishComponent.initialAngle + targetAngle);

        if (!fishRested) {
            currentFishStrenght = fishStrenght * TIRED_FISH_STRENGHT_MODIFIER;
            if(timeTilSwimSound >= 1.1f) {
                timeTilSwimSound = 0f;
                if (swimSlowAudio != 0)
                    SoundUtil.playSoundEvent3dToPlayer(player, swimSlowAudio, SoundCategory.SFX, bobberPos, store);
            }

        } else {
            if(timeTilSwimSound >= 1.1f){
                timeTilSwimSound = 0f;
                if(swimFastAudio != 0)
                    SoundUtil.playSoundEvent3dToPlayer(player, swimFastAudio, SoundCategory.SFX, bobberPos, store);
            }

            if (Math.abs(fishComponent.orbitAngle) >= targetAngle * 0.9f) {
                currentFishStrenght = fishStrenght;
            } else {
                currentFishStrenght = fishStrenght * 0.8f;
            }
        }
        currentFishStrenght = Math.abs(currentFishStrenght);

        currentPlayerStrenght = (float) getForceExerted(fishermanComponent.getHeight()) * playerStrenght;

        // --- B. SMOOTHED TENSION ---
        float targetTension = currentFishStrenght + currentPlayerStrenght;
        tension = lerp(tension, targetTension, dt * 10.0f);

        // --- C. SMOOTHED DISTANCE (TUG-OF-WAR) ---
        float targetDistanceChange = 0f;
        if (fishermanComponent.getSide() * fishStrenght > 0
                && (fishermanComponent.getSide() >= 0.50f || fishermanComponent.getSide() <= -0.50f)) {
            if (fishermanComponent.getHeight() >= 0f) {
                targetDistanceChange = Math.clamp(
                        (((currentFishStrenght * dt) - (currentPlayerStrenght * dt)) * SPEED_MODIFIER), -10f, 0f);
            } else {
                targetDistanceChange = Math.clamp(
                        (((currentFishStrenght * dt) - (currentPlayerStrenght * dt)) * SPEED_MODIFIER), 0f,
                        currentFishStrenght);
            }
        } else {
            targetDistanceChange = (currentFishStrenght * dt) * SPEED_MODIFIER;
        }

        // Apply smoothing to the distance velocity
        fishComponent.distanceVelocity = lerp(fishComponent.distanceVelocity, targetDistanceChange, dt * 5.0f);
        fishComponent.currentDistance += fishComponent.distanceVelocity;

        double targetX = playerPos.x + Math.cos(fishComponent.orbitAngle) * fishComponent.currentDistance;
        double targetZ = playerPos.z + Math.sin(fishComponent.orbitAngle) * fishComponent.currentDistance;
        bobberTransform.setPosition(new Vector3d(targetX, bobberPos.y, targetZ));

        if (distanceXZ >= 20) {
            StopFishingEvent.dispatch(player);
        } else if (distanceXZ <= 2) {
            CatchFishEvent.dispatch(player);
            StopFishingEvent.dispatch(player);
        }



        if (tension >= MAX_TENSION * 2) {
            playerRef.sendMessage(Message.raw("2X TENSION REACHED: %f".formatted(tension)));
            StopFishingEvent.dispatch(player);
        }

        if (tension >= MAX_TENSION) {
            CameraControllerEvent.dispatch(player, CameraState.STRUGGLE);
            timeAtMaxTension += dt;
        } else {
            timeAtMaxTension = 0f;
        }

        if (timeAtMaxTension >= 2) {
            playerRef.sendMessage(Message.raw("LINHA QUEBROU! "));
            StopFishingEvent.dispatch(player);
        }

        if (fishComponent.currentStamina <= 0f) {
            fishRested = false;
        }

        if (fishComponent.currentStamina >= fishComponent.maxStamina && !fishRested) {
            fishComponent.currentStamina = fishComponent.maxStamina;
            fishRested = true;
        }

        if (fishRested) {
            fishComponent.currentStamina -= dt;
        } else {
            fishComponent.currentStamina += (dt * fishComponent.staminaRegen);
        }

        // playerRef.sendMessage(Message.raw("TEN: %f
        // (%fs)".formatted(tension,timeAtMaxTension)));
        // playerRef.sendMessage(Message.raw("%b".formatted(fishRested)));


        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.append("Hud/FishingHUD.ui");
        uiCommandBuilder.set("#TensionLabel.TextSpans", Message.raw("Tension: %.1f %%".formatted((tension/MAX_TENSION) *100f)));


        var customHud = playerObj.getHudManager().getCustomHud("FishingHudKey");

        if(customHud != null) customHud.update(true, uiCommandBuilder);

        fishermanComponent.setSide(0);
        fishermanComponent.setHeight(0);
        timeSameSide += dt;
        totalTime += dt;
        timeTilSwimSound += dt;
        timeTilReelSound += dt;

        ParticleUtil.spawnParticleEffect("Water_Sprint", bobberPos,0f,0f,0f,0.5f, 0.2f, commandBuffer);

        if(timeTilReelSound >= 1f) {
            if (fishComponent.distanceVelocity > 0) {
                SoundUtil.playSoundEvent3dToPlayer(player, reelOutAudio, SoundCategory.SFX, playerPos, store);
            } else if (fishComponent.distanceVelocity < 0) {
                SoundUtil.playSoundEvent3dToPlayer(player, reelInAudio, SoundCategory.SFX, playerPos, store);
            }
            timeTilReelSound = 0f;
        }
    }

    private float newTimeTilSideChange() {
        return MIN_TIME + random.nextFloat() * (MAX_TIME - MIN_TIME);
    }

    private float newAngle() {
        return random.nextFloat() * (MAX_ANGLE);
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    private double getForceExerted(double height) {
        return (Math.clamp(height, -0.20f, 0.20f)) / 0.20f;
    }


}
