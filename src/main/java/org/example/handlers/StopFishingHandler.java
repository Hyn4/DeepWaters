package org.example.handlers;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.example.components.PlayerRPGComponent;
import org.example.events.StartFishingEvent;
import org.example.events.StopFishingEvent;
import org.joml.Vector2f;

import java.util.UUID;
import java.util.function.Consumer;

public class StopFishingHandler implements Consumer<StopFishingEvent> {
    @Override
    public void accept(StopFishingEvent event) {
        if (!event.playerRef().isValid())return;

        var store = event.playerRef().getStore();
        var player = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        var world = store.getExternalData().getWorld();


        if(player == null) return;
        var rpg = store.getComponent(event.playerRef(), PlayerRPGComponent.getComponentType());
        if(rpg == null) return;

        if(!rpg.isFishing()) {
            player.sendMessage(Message.raw("Is not fishing!"));
            return;
        }

        var bobberRef = world.getEntityRef(rpg.getBobberId());

        try {
            world.execute(() -> {
                if (!bobberRef.isValid()) {
                    return;
                }
                try {
                    store.removeEntity(bobberRef, RemoveReason.REMOVE);
                } catch (Exception e) {
                    player.sendMessage(Message.raw(e.toString()));
                }
            });
        } catch (Exception e) {
            player.sendMessage(Message.raw("Failed to enqueue bobber remove"));
        }

        rpg.setBobberId(null);

        MovementManager movementManager = store.getComponent(event.playerRef(), MovementManager.getComponentType());
        movementManager.applyDefaultSettings();

        SetServerCamera packet = new SetServerCamera(ClientCameraView.FirstPerson, false, new ServerCameraSettings());

        movementManager.update(player.getPacketHandler());

        player.getPacketHandler().writeNoCache(packet);

        player.sendMessage(Message.raw("Stopped fishing!"));
        rpg.setFishing(false);
    }
}
