package org.example.handlers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.RespawnPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.example.components.FishermanComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.CameraControllerEvent;
import org.example.events.StartFishingEvent;
import org.example.events.StopFishingEvent;
import org.example.ui.FishingUI;
import org.example.utils.CameraState;
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

        if (rpg.getBobberId() != null) {
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
        }

        /*var playerObj = store.getComponent(event.playerRef(), Player.getComponentType());
        playerObj.getHudManager().removeCustomHud(player, "FishingHudKey");
        player.sendMessage(Message.raw("HUD HIDDEN!"));*/

        rpg.setBobberId(null);
        rpg.setFishBiting(false);

        MovementManager movementManager = store.getComponent(event.playerRef(), MovementManager.getComponentType());
        movementManager.applyDefaultSettings();

        movementManager.update(player.getPacketHandler());

        CameraControllerEvent.dispatch(event.playerRef(), CameraState.DEFAULT);

        player.sendMessage(Message.raw("Stopped fishing!"));
        rpg.setFishing(false);
    }
}
