package org.example.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.example.utils.CameraState;

import javax.annotation.Nonnull;


public record CameraControllerEvent(
        @Nonnull Ref<EntityStore> playerRef,
        @Nonnull CameraState cameraState,
        float intensity
        )implements IEvent<Void>{
    public static void dispatch(Ref<EntityStore> playerRef, CameraState cameraState, float intensity){
        IEventDispatcher<CameraControllerEvent, CameraControllerEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(CameraControllerEvent.class);

        if(dispatcher.hasListener()){
            dispatcher.dispatch(new CameraControllerEvent(playerRef, cameraState, intensity));
        }
    }
}
