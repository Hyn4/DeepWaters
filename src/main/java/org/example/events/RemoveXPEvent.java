package org.example.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bouncycastle.util.Store;

import javax.annotation.Nonnull;

public record RemoveXPEvent(
        @Nonnull Ref<EntityStore> playerRef,
        long amount
) implements IEvent<Void>{
    public static void dispatch(Ref<EntityStore> playerRef, long amount){
        IEventDispatcher<RemoveXPEvent , RemoveXPEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(RemoveXPEvent.class);

        if(dispatcher.hasListener()){
            dispatcher.dispatch(new RemoveXPEvent(playerRef,amount));
        }
    }
}

