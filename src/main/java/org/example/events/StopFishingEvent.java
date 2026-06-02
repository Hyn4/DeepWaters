package org.example.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public record StopFishingEvent(
        @Nonnull Ref<EntityStore> playerRef
)implements IEvent<Void>{
    public static void dispatch(Ref<EntityStore> playerRef){
        IEventDispatcher<StopFishingEvent, StopFishingEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(StopFishingEvent.class);

        if(dispatcher.hasListener()){
            dispatcher.dispatch(new StopFishingEvent(playerRef));
        }
    }
}
