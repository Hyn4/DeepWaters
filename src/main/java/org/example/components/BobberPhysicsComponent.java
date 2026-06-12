package org.example.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.UUID;

public class BobberPhysicsComponent implements Component<EntityStore> {

    private UUID playerId;

    public boolean inWater = false;

    public static final BuilderCodec<BobberPhysicsComponent> CODEC =
            BuilderCodec
                    .builder(BobberPhysicsComponent.class, BobberPhysicsComponent::new)
                    .append(
                            new KeyedCodec<>("PlayerId",Codec.UUID_BINARY),
                            (component, value) -> component.playerId = value,
                            component -> component.playerId
                    ).add()
                    .build();


    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    private static ComponentType<EntityStore, BobberPhysicsComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, BobberPhysicsComponent> type){
        TYPE = type;
    }

    public static ComponentType<EntityStore, BobberPhysicsComponent> getComponentType(){
        return TYPE;
    }

    @NullableDecl
    @Override
    public BobberPhysicsComponent clone(){
        return new BobberPhysicsComponent();
    }

    public BobberPhysicsComponent(){}

    public BobberPhysicsComponent(UUID playerId){
        this.setPlayerId(playerId);
    }

}
