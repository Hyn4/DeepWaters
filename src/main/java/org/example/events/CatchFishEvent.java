package org.example.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public record CatchFishEvent (
        @Nonnull Ref<EntityStore> player
) implements IEvent<Void> {
    public static void dispatch(Ref<EntityStore> player){
        IEventDispatcher<CatchFishEvent, CatchFishEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(CatchFishEvent.class);

        if(dispatcher.hasListener()){
            dispatcher.dispatch(new CatchFishEvent(player));
        }
    }

}
