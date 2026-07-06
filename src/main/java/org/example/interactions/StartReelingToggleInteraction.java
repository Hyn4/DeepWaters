package org.example.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.example.components.FishermanComponent;

public class StartReelingToggleInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<StartReelingToggleInteraction> CODEC= BuilderCodec.builder(
            StartReelingToggleInteraction.class, StartReelingToggleInteraction::new, SimpleInstantInteraction.CODEC
    ).build();


    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType,
                            @NonNullDecl InteractionContext interactionContext,
                            @NonNullDecl CooldownHandler cooldownHandler) {

        var player = interactionContext.getOwningEntity();
        var commandBuffer = interactionContext.getCommandBuffer();
        var fishermanComponent = commandBuffer.getComponent(player, FishermanComponent.getComponentType());

        assert fishermanComponent != null;
        fishermanComponent.setReeling(true);

        //commandBuffer.getComponent(player,PlayerRef.getComponentType()).sendMessage(Message.raw("IsReeling: %b".formatted(fishermanComponent.isReeling())));
    }




}
