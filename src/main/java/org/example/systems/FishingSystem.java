package org.example.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import org.joml.Vector3d;

import java.util.Random;

public class FishingSystem extends EntityTickingSystem<EntityStore> {

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(FishComponent.getComponentType());
    }

    private enum Behavior{
        PULLING(0.8f, 0.3f),
        THRASHING(0.2f, 0.8f),
        BALANCED(0.5f, 0.5f);

        public final float strengthRatio;
        public final float targetAngle;

        Behavior(float strengthRatio, float targetAngle) {this.strengthRatio = strengthRatio; this.targetAngle = targetAngle;}
    }


    private final Random random = new Random();

    PlayerRef playerRef;

    // Minigame Session State
    private boolean reelingIn;
    private boolean isInitialized = false;
    private float fishBaseStrength;
    private float playerBaseStrength;
    private float rodMaxTension;

    private FishComponent fishComponent;
    private Ref<EntityStore> player;
    private World world;
    private TransformComponent bobberTransform;
    private TransformComponent playerTransform;
    private FishermanComponent fishermanComponent;
    private PlayerRPGComponent playerRPGComponent;

    private int fluidId;

    private float splashScale;

    double distanceXZ;

    private boolean inWater;
    private float maxFishHorizontalSpeed;
    private float maxFishVerticalSpeed;
    private final float MAX_REEL_SPEED = 0.2f;
    private final float SWEET_SPOT = 0f;
    private boolean fishRested = true;
    private float totalTime = 0f;
    private float timeTilReelSound = 0f;
    private float timeTilSwimSound = 0f;
    private float timeAtMaxTension = 0f;
    private float timeSameSide = 0f;
    private float tension = 0f;
    private float currentPlayerStrenght;
    private float timeTilSideChange = 0f;
    private float targetAngle = 0.7f;
    private final float SPEED_MODIFIER = 0.2f;
    private final float ORBIT_SPEED_MODIFIER = 0.05f;
    private final float LERP_MODIFIER = 1.0f;
    float ORBIT_LERP_MODIFIER = 2f;
    private float targetOrbitVelocity;
    private Vector3d initalPlayerPosition;
    private float fishStrengthRatio;
    private float fishHorizontalStrength;
    private float fishVerticalStrength;
    private float fishTotalStrength;
    private float playerTotalStrength;
    private float playerVerticalStrength;
    private float playerHorizontalStrength;
    private float timeTilChangeBehaviour;
    private Behavior behavior;
    private final float maxTension = 0.99f;
    private double distanceToPlayer;
    Vector3d playerPos;
    Vector3d bobberPos;
    private float targetAngleLerp;
    private float thrashingCD;
    private float timeTilThrash;
    private Player playerObj;
    private CustomUIHud customHud;
    UICommandBuilder uiCommandBuilder;
    private HeadRotation playerHeadRotation;

    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        if (!isInitialized) {
            initialize(archetypeChunk, index, store);
        }
        if(!playerRPGComponent.isFishing()){
            isInitialized = false;
        }

        MANAGE_BEHAVIOR(dt);

        CALCULATE_POSITIONS_AND_DISTANCE();

        GET_REEL_PLAYER_INPUT(commandBuffer, bobberPos, playerPos, player, fishermanComponent);

        CHANGE_FISH_SWIMMING_SIDE(store, commandBuffer);

        SET_CURRENT_FISH_STRENGTH(store, fishComponent, player, bobberPos);

        SET_ORBIT_ANGLE(dt, fishComponent, maxFishHorizontalSpeed);

        CALCULATE_DISTANCE(dt);

        CALCULATE_TENSION(dt);

        MANAGE_HUD();

        SET_BOBBER_POSITION();

        CHECK_IF_STILL_IN_WATER(bobberPos);

        CHECK_IF_CAUGHT(store);

        MANAGE_TENSION(dt, store, fishermanComponent, playerRef, player, playerPos);

        MANAGE_FISH_STAMINA(dt, fishComponent);

        SPAWN_PARTICLE(commandBuffer);

        PLAY_ROD_SFX(store, fishermanComponent, fishComponent, player, playerPos);

        INCREASE_TIMERS_RESET_INPUT(dt, fishermanComponent);

        FISH_STRUGGLE(store,commandBuffer);

        HANDLE_FISHING_LINE(commandBuffer);

        /*TODO maybe I can convey what the fish is doing by showing text above the bobber like a comic book
        (huf puf - tired, *Struggle* - when thrasing, hooked! - when stabilizing stamina back to 0, and also for the differnt behaviors,
        could give it some more character, maybe a comic book style could be a trope for the mod)*/

        //TODO somehow render that fishing line, focusing on third person view

    }

    private void HANDLE_FISHING_LINE(CommandBuffer<EntityStore> commandBuffer){
        var playerHeadDirection = playerHeadRotation.getDirection();
        var rodTipPos = new Vector3d(playerPos).add(0.0, 1.5, 0.0).add(new Vector3d(playerHeadDirection).mul(1.5));;
        var lineVector = new Vector3d(bobberPos).sub(rodTipPos);
        double lineLength = lineVector.length();
        var direction = new Vector3d(lineVector).normalize();
        double spacing = 0.05;

        for (double dist = 0.0; dist < lineLength; dist += spacing) {
            Vector3d particlePos = new Vector3d(direction).mul(dist).add(rodTipPos);
            // Spawn a flat, stationary white particle
            ParticleUtil.spawnParticleEffect("Water_Sprint", particlePos, 0f, 0f, 0f, 0.05f, 0.2f, commandBuffer);
        }
    }


    private void MANAGE_HUD() {
        if(playerRPGComponent.isFishing()) {
            uiCommandBuilder = new UICommandBuilder();
            uiCommandBuilder.append("Hud/FishingHUD.ui");
            uiCommandBuilder.set("#TensionLabel.TextSpans", Message.raw("Tension: %.2f".formatted(tension)));
            if (customHud != null) customHud.update(true, uiCommandBuilder);
        }
    }

    private void FISH_STRUGGLE(Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer){
        if(timeTilThrash >= thrashingCD){
            if(fishComponent.orbitAngle >= fishComponent.initialAngle + targetAngle - 0.05 || fishComponent.orbitAngle <= fishComponent.initialAngle - targetAngle + 0.05){
                THRASH(store,commandBuffer);
            }
            timeTilThrash = 0f;
        }
    }

    private void SPAWN_PARTICLE(CommandBuffer<EntityStore> commandBuffer){
        ParticleUtil.spawnParticleEffect("Water_Sprint", bobberPos, 0f, 0f, 0f, splashScale, 1f, commandBuffer);
    }

    private void CHECK_IF_CAUGHT(@NonNullDecl Store<EntityStore> store) {
        if (distanceXZ <= 2 || distanceToPlayer <= 2) {
            SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getWaterMoveOutAudio(), SoundCategory.SFX, playerPos, store);
            CatchFishEvent.dispatch(player, fishComponent.type.getItemId());
            STOP_FISHING();
        }
    }

    private void SET_BOBBER_POSITION() {
        double targetX = initalPlayerPosition.x + Math.cos(fishComponent.orbitAngle) * fishComponent.currentDistance;//change
        double targetZ = initalPlayerPosition.z + Math.sin(fishComponent.orbitAngle) * fishComponent.currentDistance;//change
        bobberTransform.setPosition(new Vector3d(targetX, bobberPos.y, targetZ));
    }

    private void CHANGE_FISH_SWIMMING_SIDE(@NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if (timeSameSide >= timeTilSideChange) {
            ChangeSides(fishComponent);
            //THRASH(store, commandBuffer);
        }
    }

    private void CALCULATE_POSITIONS_AND_DISTANCE() {
        playerPos = new Vector3d(playerTransform.getPosition());
        bobberPos = new Vector3d(bobberTransform.getPosition());
        distanceXZ = new Vector3d(initalPlayerPosition.x, 0, initalPlayerPosition.z).distance(new Vector3d(bobberPos.x, 0, bobberPos.z));
        distanceToPlayer = new Vector3d(playerPos.x, 0, playerPos.z).distance(new Vector3d(bobberPos.x, 0, bobberPos.z));

        if(distanceXZ > 15) STOP_FISHING();
    }

    private void CHECK_IF_STILL_IN_WATER(Vector3d bobberPos) {
        fluidId = world.getFluidId((int) bobberPos.x, (int) Math.floor(bobberPos.y), (int) bobberPos.z);
        inWater = (fluidId == 7 || fluidId == 8 || fluidId == 12 || fluidId == 2);

        if (!inWater) {
            playerRef.sendMessage(Message.raw("NOT IN WATER!!!"));
            STOP_FISHING();
        }
    }

    private void CALCULATE_DISTANCE(float dt){
        float playerForceVector =  playerVerticalStrength / (Math.abs(playerVerticalStrength) + fishVerticalStrength);
        float fishForceVector = fishVerticalStrength / (Math.abs(playerVerticalStrength) + fishVerticalStrength);
        float targetDistanceChange;
        if(fishermanComponent.isReeling()){

            targetDistanceChange =  fishForceVector - playerForceVector;
            /*if(fishermanComponent.getHeight() >= 0.2 ) {

                targetDistanceChange = Math.clamp(targetDistanceChange* MAX_REEL_SPEED, MAX_REEL_SPEED, 0 );


            }
            else if(fishermanComponent.getHeight() <= -0.2){
                targetDistanceChange = Math.clamp(Math.abs(targetDistanceChange) * maxFishVerticalSpeed,0,maxFishVerticalSpeed);

            }
            else{
                targetDistanceChange = 0f;
            }*/
            //playerRef.sendMessage(Message.raw("TDG: %.2f | PV: %.2f | FV: %.2f".formatted(targetDistanceChange, playerForceVector, fishForceVector)));
            if(targetDistanceChange > 0) {targetDistanceChange *= maxFishVerticalSpeed;}
            else targetDistanceChange *= MAX_REEL_SPEED;
        }
        else{
            targetDistanceChange = maxFishVerticalSpeed * fishStrengthRatio;
        }

        targetDistanceChange = Math.clamp(targetDistanceChange, -MAX_REEL_SPEED, maxFishVerticalSpeed)  * SPEED_MODIFIER;



        if (distanceXZ >= 7) {
            if (targetDistanceChange > 0) {
                targetDistanceChange = 0f;
            }
        }


        fishComponent.distanceVelocity = lerp(fishComponent.distanceVelocity, targetDistanceChange, dt * LERP_MODIFIER);
        fishComponent.currentDistance += fishComponent.distanceVelocity;

    }

    private void CALCULATE_TENSION(float dt){
        float targetTension;
        if(fishComponent.orbitAngle >= fishComponent.initialAngle + targetAngle - 0.05 || fishComponent.orbitAngle <= fishComponent.initialAngle - targetAngle + 0.05){
            targetTension = (float) Math.clamp((fishermanComponent.getSide() * fishComponent.side) - (1 - fishStrengthRatio), -1,0);
            //playerRef.sendMessage(Message.raw("OUTER"));
        }
        else{
            targetTension = (float) Math.clamp(fishermanComponent.getHeight() + fishStrengthRatio, 0,1);
           // playerRef.sendMessage(Message.raw("INNER"));
        }
        tension = lerp(tension, targetTension, dt * LERP_MODIFIER * 4f);
        playerRef.sendMessage(Message.raw("ESCAPE CHANCE: %f".formatted(CALCULATE_ESCAPE_CHANCE())));
    }

    private void THRASH(@NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        ParticleUtil.spawnParticleEffect("Water_Sprint", bobberPos, 0f, 0f, 0f, splashScale * 3, 1f, commandBuffer);
        SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getWaterMoveOutAudio(), SoundCategory.SFX, playerPos, store);
        //playerRef.sendMessage(Message.raw(String.valueOf(CALCULATE_ESCAPE_CHANCE())));

        if(random.nextFloat() <= CALCULATE_ESCAPE_CHANCE()){
            playerRef.sendMessage(Message.raw("FISH ESCAPED!"));
            STOP_FISHING();
        }
    }

    private float CALCULATE_ESCAPE_CHANCE(){
        return (float) Math.clamp(( Math.abs( fishComponent.orbitAngle - fishComponent.initialAngle ) * ( Math.clamp( (tension * -1) , 0 , 1 )) /2f ) + 0.01f , 0f , 1f);
    }

    private void MANAGE_BEHAVIOR(float dt){
        if(timeTilChangeBehaviour >= 5f){
            int n = random.nextInt() % 3;
            switch(n){
                case 0: behavior = Behavior.BALANCED; break;
                case 1: behavior = Behavior.PULLING; break;
                case 2: behavior = Behavior.THRASHING; break;
            }


            fishStrengthRatio = behavior.strengthRatio;
            targetAngleLerp = behavior.targetAngle;

            timeTilChangeBehaviour = 0f;
        }
        targetAngle = lerp(targetAngle,targetAngleLerp,dt);
    }

    private void INCREASE_TIMERS_RESET_INPUT(float dt, FishermanComponent fishermanComponent) {
        fishermanComponent.setSide(0);
        fishermanComponent.setHeight(0);
        timeTilChangeBehaviour += dt;
        timeSameSide += dt;
        totalTime += dt;
        timeTilSwimSound += dt;
        timeTilReelSound += dt;
        timeTilThrash += dt;
    }

    private void PLAY_ROD_SFX(@NonNullDecl Store<EntityStore> store, FishermanComponent fishermanComponent, FishComponent fishComponent, Ref<EntityStore> player, Vector3d playerPos) {
        // play reel in / reel out / max tension SFX
        if (timeTilReelSound >= 0.3f) {
            if (tension < maxTension) {
                if (fishComponent.distanceVelocity > 0) {
                    SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getReelOutAudio(), SoundCategory.SFX, playerPos, store);
                } else if (fishComponent.distanceVelocity <= 0) {
                    SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getReelInAudio(), SoundCategory.SFX, playerPos, store);
                }
            } else {
                SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getMaxTensionAudio(), SoundCategory.SFX, playerPos, store);
            }
            timeTilReelSound = 0f;
        }
    }

    private void MANAGE_FISH_STAMINA(float dt, FishComponent fishComponent) {
        // TODO revamp fish stamina or just remove it completely

        if (fishComponent.currentStamina <= 0f) {
            fishRested = false;
        }
        if (fishComponent.currentStamina >= fishComponent.type.maxStamina && !fishRested) {
            fishComponent.currentStamina = fishComponent.type.maxStamina;
            fishRested = true;
        }
        if (fishRested) {
            fishComponent.currentStamina -= dt;
        } else {
            fishComponent.currentStamina += (dt * fishComponent.type.staminaRegen);
        }
    }

    private void MANAGE_TENSION(float dt, @NonNullDecl Store<EntityStore> store, FishermanComponent fishermanComponent, PlayerRef playerRef, Ref<EntityStore> player, Vector3d playerPos) {

        if (tension >= maxTension) {
            CameraControllerEvent.dispatch(player, CameraState.STRUGGLE, timeAtMaxTension / 10f); // esse 40f é pra transformar o intervalo de 0-2 em 0-0.05 (intensidade do camera shake é mais sensível)
            timeAtMaxTension += dt;
            //playerRef.sendMessage(Message.raw("%f".formatted(timeAtMaxTension)));
        } else {
            if(timeAtMaxTension > 0) {timeAtMaxTension -= dt * 1.5f;}
            else timeAtMaxTension = 0;
        }
        if (timeAtMaxTension >= 2) {
            SoundUtil.playSoundEvent3dToPlayer(player, fishermanComponent.getLineBreakAduio(), SoundCategory.SFX, playerPos, store);
            STOP_FISHING();
        }
    }

    private void SET_CURRENT_FISH_STRENGTH(@NonNullDecl Store<EntityStore> store, FishComponent fishComponent, Ref<EntityStore> player, Vector3d bobberPos) {
        if (!fishRested) { //TODO revamp how stamina works
            fishTotalStrength = fishBaseStrength * fishComponent.type.tiredFishStrenghtModifier;
            if (timeTilSwimSound >= 1.1f) {
                timeTilSwimSound = 0f;
                if (fishComponent.getSwimSlowAudio() != 0)
                    SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getSwimSlowAudio(), SoundCategory.SFX,
                            bobberPos, store);
            }

        } else {
            if (timeTilSwimSound >= 1.1f) {
                timeTilSwimSound = 0f;
                if (fishComponent.getSwimFastAudio() != 0)
                    SoundUtil.playSoundEvent3dToPlayer(player, fishComponent.getSwimFastAudio(), SoundCategory.SFX,
                            bobberPos, store);
            }

            fishTotalStrength = fishBaseStrength;
        }
        fishVerticalStrength = fishTotalStrength * fishStrengthRatio;
        fishHorizontalStrength = fishTotalStrength - fishVerticalStrength;
    }

    private void SET_ORBIT_ANGLE(float dt, @NonNullDecl FishComponent fishComponent, float maxFishHorizontalSpeed) {

        float fishForceVector = (fishHorizontalStrength * fishComponent.side) / (Math.abs(playerHorizontalStrength) + fishHorizontalStrength);

        if(Math.signum(playerHorizontalStrength) != Math.signum(fishComponent.side)){fishComponent.orbitVelocity = Math.max(maxFishHorizontalSpeed,MAX_REEL_SPEED) * fishComponent.side;}
        else fishComponent.orbitVelocity = fishForceVector * maxFishHorizontalSpeed;

        //fishComponent.orbitVelocity = lerp(fishComponent.orbitVelocity, targetOrbitVelocity, dt * ORBIT_LERP_MODIFIER);
        //fishComponent.orbitAngle += fishComponent.orbitVelocity * ORBIT_SPEED_MODIFIER;
        fishComponent.orbitAngle = Math.clamp(lerp((float)fishComponent.orbitAngle,targetAngle * fishComponent.side,dt * fishComponent.orbitVelocity ), fishComponent.initialAngle - targetAngle, fishComponent.initialAngle + targetAngle);
    }

    private void ChangeSides(@NonNullDecl FishComponent fishComponent) {
        fishComponent.side *= -1;
        // targetAngle = newAngle(fishComponent.type.maxAngle);

        timeTilSideChange = newTimeTilSideChange(fishComponent.type.minTimeToChangeSides,fishComponent.type.maxTimeToChangeSides) * 3f;
        timeSameSide = 0f;
    }

    private void GET_REEL_PLAYER_INPUT(@NonNullDecl CommandBuffer<EntityStore> commandBuffer, Vector3d bobberPos, Vector3d playerPos, Ref<EntityStore> player, @NonNullDecl FishermanComponent fishermanComponent) {

        double mappedSide = 0;
        double mappedHeight = 0;

        if(fishermanComponent.isReeling()) {
            var playerToBobber = new Vector3d(bobberPos).sub(playerPos).normalize();
            var playerHeadRotation = commandBuffer.getComponent(player, HeadRotation.getComponentType());
            var playerHeadDirection = playerHeadRotation.getDirection().normalize();

            double rawSide = ((playerHeadDirection.x * playerToBobber.z) - (playerHeadDirection.z * playerToBobber.x)) * 3f;
            double rawHeight = (playerHeadRotation.getRotation().pitch() / 1.56f) * 8f;

            rawSide = Math.clamp(rawSide, -1f, 1f);
            rawHeight = Math.clamp(rawHeight, -1f, 1f);

            double absSide = Math.abs(rawSide);
            double absHeight = Math.abs(rawHeight);

            double sum = absSide + absHeight;

            if (sum > 0) {
                double scale = Math.max(absSide, absHeight) / sum;
                mappedSide = rawSide * scale;
                mappedHeight = rawHeight * scale;
            }

            fishermanComponent.setHeight(mappedHeight);
            fishermanComponent.setSide(mappedSide);
        }

        playerVerticalStrength = (float) mappedHeight * playerRPGComponent.getFishermanStrenght();
        playerHorizontalStrength = (float) mappedSide * playerRPGComponent.getFishermanStrenght();
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

    private void STOP_FISHING(){
        isInitialized = false;
        StopFishingEvent.dispatch(player);
    }

    private void initialize(ArchetypeChunk<EntityStore> archetypeChunk, int index, Store<EntityStore> store) {
        this.fishComponent = archetypeChunk.getComponent(index, FishComponent.getComponentType());
        this.player = store.getExternalData().getRefFromUUID(fishComponent.getPlayerId());
        this.playerRef = store.getComponent(player, PlayerRef.getComponentType());
        this.playerObj = store.getComponent(player, Player.getComponentType());
        //playerRef.sendMessage(Message.raw("Before initializing: %s".formatted(this.toString())));

        this.world = store.getExternalData().getWorld();
        this.bobberTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        this.playerTransform = store.getComponent(player, TransformComponent.getComponentType());
        this.fishermanComponent = store.getComponent(player, FishermanComponent.getComponentType());
        this.playerRPGComponent = store.getComponent(player, PlayerRPGComponent.getComponentType());
        this.playerHeadRotation = store.getComponent(player, HeadRotation.getComponentType());

        this.fishBaseStrength = fishComponent.type.strength;
        this.fishTotalStrength = fishBaseStrength;
        this.playerBaseStrength = playerRPGComponent.getFishermanStrenght();
        this.rodMaxTension = fishermanComponent.getMaxTension();
        this.fishStrengthRatio = Behavior.BALANCED.strengthRatio;
        this.customHud = playerObj.getHudManager().getCustomHud("FishingHudKey");


        this.thrashingCD = 1.5f - (fishComponent.type.speed / 100);
        this.maxFishHorizontalSpeed = (fishComponent.type.speed / 100.0f) * 0.5f;
        this.maxFishVerticalSpeed = (fishComponent.type.speed/100f) * 0.3f;
        this.splashScale = fishComponent.type.getSizeClass().particleScale;
        this.initalPlayerPosition = new Vector3d(playerTransform.getPosition());
        this.behavior = Behavior.BALANCED;

        // Reset session state for new minigame
        this.tension = 0f;
        this.totalTime = 0f;
        this.timeTilThrash = 0f;
        this.timeAtMaxTension = 0f;
        this.fishRested = true;
        this.timeTilReelSound = 0f;
        this.timeTilSwimSound = 0f;
        this.timeSameSide = 0f;
        this.reelingIn = false;
        this.timeTilChangeBehaviour = 0f;

        this.isInitialized = true;
    }

    @Override
    public String toString() {
        return "FishingSystem{" +
                "Tension= " + tension +
                '}';
    }
}
