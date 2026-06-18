package org.example.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.components.FishermanComponent;
import org.example.components.PlayerRPGComponent;

public class PlayerJoinSystem extends RefSystem<EntityStore> {
    @Override
    public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref,
                              @NonNullDecl AddReason addReason,
                              @NonNullDecl Store<EntityStore> store,
                              @NonNullDecl CommandBuffer<EntityStore> commandBuffer)
    {
        if(addReason != AddReason.LOAD) return;

        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if(playerRef == null) return;

        var rpgType = PlayerRPGComponent.getComponentType();
        var rpg = store.getComponent(ref,rpgType);

        if(rpg != null){
            playerRef.sendMessage(Message.raw("Welcome Back! Level %d (%d XP)".formatted(rpg.getLevel(), rpg.getTotalExeperience())));
            rpg.setFishing(false);
            rpg.setFishBiting(false);
            rpg.setBobberId(null);

        }else {
            commandBuffer.addComponent(ref,rpgType,new PlayerRPGComponent());
            playerRef.sendMessage(Message.raw("Welcome! Your adventure begins at level 1!"));
        }

        commandBuffer.ensureComponent(ref, FishermanComponent.getComponentType());
    }

    @Override
    public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref,
                               @NonNullDecl RemoveReason removeReason,
                               @NonNullDecl Store<EntityStore> store,
                               @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }
}

