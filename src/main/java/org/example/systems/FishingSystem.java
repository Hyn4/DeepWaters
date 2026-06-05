package org.example.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.components.BobberPhysicsComponent;
import org.example.components.FishComponent;
import org.example.components.FishermanComponent;
import org.example.events.CatchFishEvent;
import org.example.events.StopFishingEvent;
import org.joml.Vector3d;

public class FishingSystem extends EntityTickingSystem<EntityStore> {

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery(){
        return Query.and(FishComponent.getComponentType());
    }

    double fishdefaultspeed = -2f;
    double fishermanstrenght = 0f;

    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        var fishComponent = archetypeChunk.getComponent(index, FishComponent.getComponentType());
        var player = store.getExternalData().getRefFromUUID(fishComponent.getPlayerId());
        var playerRef = store.getComponent(player, PlayerRef.getComponentType());
        var world  = store.getExternalData().getWorld();

        var bobberVelocity = archetypeChunk.getComponent(index, Velocity.getComponentType());
        var bobberTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        var playerTransform = store.getComponent(player, TransformComponent.getComponentType());

        var fishermanComponent = store.getComponent(player, FishermanComponent.getComponentType());


        Vector3d playerPos = new Vector3d(playerTransform.getPosition());
        Vector3d bobberPos = new Vector3d(bobberTransform.getPosition());
        Vector3d velocity = new Vector3d();
        bobberVelocity.assignVelocityTo(velocity);

        int fluidId = world.getFluidId((int)bobberPos.x, (int)Math.floor(bobberPos.y), (int)bobberPos.z);
        boolean inWater = (fluidId == 7||fluidId == 8||fluidId == 12);

        if(fishermanComponent.getSide() > 0) {
            fishermanstrenght = 4f;
        }else{
            fishermanstrenght = 0;
        }

        velocity.z = fishdefaultspeed + fishermanstrenght;

        Vector3d scaledVel = new Vector3d();
        velocity.mul(dt, scaledVel);
        bobberPos.add(scaledVel);

        bobberVelocity.set(velocity);
        bobberTransform.setPosition(bobberPos);

        double distanceXZ = new Vector3d(playerPos.x, 0, playerPos.z)
                .distance(new Vector3d(bobberPos.x, 0, bobberPos.z));

        playerRef.sendMessage(Message.raw("DISTANCE: %f".formatted(distanceXZ)));

        if(distanceXZ >= 10){
            StopFishingEvent.dispatch(player);
        } else if (distanceXZ <= 2) {
            CatchFishEvent.dispatch(player);
            StopFishingEvent.dispatch(player);
        }

        fishermanComponent.setSide(0);
    }
}
