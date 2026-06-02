package org.example.handlers;

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

import java.util.function.Consumer;

public class StopFishingHandler implements Consumer<StopFishingEvent> {
    @Override
    public void accept(StopFishingEvent event) {
        if (!event.playerRef().isValid())return;

        var store = event.playerRef().getStore();
        var player = store.getComponent(event.playerRef(), PlayerRef.getComponentType());

        var rpg = store.getComponent(event.playerRef(), PlayerRPGComponent.getComponentType());
        if(rpg == null) return;
        if(!rpg.isFishing()) {
            player.sendMessage(Message.raw("Is not fishing!"));
            return;
        }

        MovementManager movementManager = store.getComponent(event.playerRef(), MovementManager.getComponentType());
        movementManager.applyDefaultSettings();

        ServerCameraSettings cameraSettings = new ServerCameraSettings();

        cameraSettings.lookMultiplier = new Vector2f(1,1);
        cameraSettings.allowPitchControls = true;

        cameraSettings.distance = 4.0f;

        SetServerCamera packet = new SetServerCamera(ClientCameraView.FirstPerson, false, cameraSettings);

        movementManager.update(player.getPacketHandler());

        player.getPacketHandler().writeNoCache(packet);

        movementManager.update(player.getPacketHandler());


        player.sendMessage(Message.raw("Stopped fishing!"));
        rpg.setFishing(false);
    }
}
