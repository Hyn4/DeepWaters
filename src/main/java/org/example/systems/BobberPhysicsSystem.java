package org.example.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.components.BobberPhysicsComponent;
import org.example.components.FishComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.StartFishingEvent;
import org.example.events.StopFishingEvent;
import org.joml.Vector3d;

import java.util.Random;


public class BobberPhysicsSystem extends EntityTickingSystem<EntityStore> {

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(BobberPhysicsComponent.getComponentType());
    }

    private final Random random = new Random();

    private final float REEL_IN_TIME_WINDOW = 6f;
    private final float MAX_CHATCH_TIME = 15f;
    private final float MIN_CATCH_TIME = 5f;
    private float timeTilCatch = 0f;
    private float timeInWater = 0f;
    private float timeFishing = 0f;


    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {


        //pega as coisa
        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        Velocity velocityComp = archetypeChunk.getComponent(index, Velocity.getComponentType());
        BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, BoundingBox.getComponentType());
        BobberPhysicsComponent bobberPhysicsComponent = archetypeChunk.getComponent(index, BobberPhysicsComponent.getComponentType());
        if(bobberPhysicsComponent.getPlayerId() == null) return;
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(bobberPhysicsComponent.getPlayerId());
        Vector3d playerPos= store.getComponent(playerRef, TransformComponent.getComponentType()).getPosition();
        boolean playerIsFishing = store.getComponent(playerRef, PlayerRPGComponent.getComponentType()).isFishing();
        int audio = SoundEvent.getAssetMap().getIndex("SFX_Water_Movein");

        if (transform == null || velocityComp == null || boundingBoxComponent == null) return;



        World world = store.getExternalData().getWorld();
        Vector3d position = new Vector3d(transform.getPosition());
        Vector3d velocity = new Vector3d();
        velocityComp.assignVelocityTo(velocity);


        double distance = new Vector3d(playerPos.x, playerPos.y, playerPos.z)
                .distance(new Vector3d(position.x, position.y, position.z));

        if(distance >= 20f){
            StartFishingEvent.dispatch(playerRef);
            StopFishingEvent.dispatch(playerRef);
        }

        int fluidId = world.getFluidId((int)position.x, (int)Math.floor(position.y), (int)position.z);
        boolean inWater = (fluidId == 7||fluidId == 8||fluidId == 12);


        //seta vel e timers
        if(!bobberPhysicsComponent.inWater){
            if(!inWater){
                velocity.y -= 25.0 * dt;
                velocity.x *= 0.99;
                velocity.z *= 0.99;
            }else {
                velocity.y = 0;
                velocity.x = 0;
                velocity.z = 0;
                bobberPhysicsComponent.inWater = true;
                SoundUtil.playSoundEvent3dToPlayer(playerRef, audio, SoundCategory.SFX, position, store);

            }
        }

        if (bobberPhysicsComponent.inWater){
            if(!playerIsFishing){
                timeInWater += dt;
            }else {
                timeFishing += dt;
            }
        }


        //start fishing event
        if(timeInWater >= 1.5f && timeInWater <= 2.0f && !playerIsFishing){
            StartFishingEvent.dispatch(playerRef);
            timeTilCatch = setTimeTilCatch();
            timeInWater = 0f;
        }



        //peixe mordeu
        if(timeFishing >= timeTilCatch && playerIsFishing){
            ParticleUtil.spawnParticleEffect("Alerted", position, commandBuffer);
            SoundUtil.playSoundEvent3dToPlayer(playerRef, audio, SoundCategory.SFX, position, store);
            store.getComponent(playerRef, PlayerRef.getComponentType()).sendMessage(Message.raw("MORDEU!!!!! %f sec".formatted(timeTilCatch)));
            store.getComponent(playerRef, PlayerRPGComponent.getComponentType()).setFishBiting(true);
            timeTilCatch = setTimeTilCatch();
            timeFishing = 0f;
        }

        //peixe fugiu
        if(store.getComponent(playerRef, PlayerRPGComponent.getComponentType()).isFishBiting() && timeFishing >= REEL_IN_TIME_WINDOW){
            store.getComponent(playerRef, PlayerRef.getComponentType()).sendMessage(Message.raw("FUGIU!!!!!"));
            store.getComponent(playerRef, PlayerRPGComponent.getComponentType()).setFishBiting(false);
        }


        //atualiza posição
        Vector3d scaledVel = new Vector3d();
        velocity.mul(dt, scaledVel);
        position.add(scaledVel);

        velocityComp.set(velocity);
        transform.setPosition(position);
    }

    private float setTimeTilCatch(){
        return MIN_CATCH_TIME + random.nextFloat() * (MAX_CHATCH_TIME - MIN_CATCH_TIME);
    }



}
