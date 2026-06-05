package org.example.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public record StartMinigameEvent(
        @Nonnull Ref<EntityStore> player,
        @Nonnull CommandBuffer<EntityStore> commandBuffer
) implements IEvent<Void> {
    public static void dispatch(Ref<EntityStore> player, CommandBuffer<EntityStore> commandBuffer){
        IEventDispatcher<StartMinigameEvent, StartMinigameEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(StartMinigameEvent.class);

        if(dispatcher.hasListener()){
            dispatcher.dispatch(new StartMinigameEvent(player, commandBuffer));
        }
    }

}
