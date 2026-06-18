package org.example.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.example.components.BobberPhysicsComponent;
import org.example.components.FishermanComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.CatchFishEvent;
import org.example.events.StopFishingEvent;
import org.joml.Vector3d;

import java.util.UUID;

public class FishingInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<FishingInteraction> CODEC= BuilderCodec.builder(
            FishingInteraction.class, FishingInteraction::new, SimpleInstantInteraction.CODEC
    ).build();


    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType,
                            @NonNullDecl InteractionContext interactionContext,
                            @NonNullDecl CooldownHandler cooldownHandler) {

        var player = interactionContext.getOwningEntity();
        var commandBuffer = interactionContext.getCommandBuffer();
        var fishermanComponent = commandBuffer.getComponent(player, FishermanComponent.getComponentType());

        fishermanComponent.switchReelingState();

        //commandBuffer.getComponent(player,PlayerRef.getComponentType()).sendMessage(Message.raw("IsReeling: %b".formatted(fishermanComponent.isReeling())));
    }




}
