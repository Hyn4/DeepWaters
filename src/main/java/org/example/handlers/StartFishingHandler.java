package org.example.handlers;

import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.example.components.PlayerRPGComponent;
import org.example.events.GiveXPEvent;
import org.example.events.LevelUpEvent;
import org.example.events.StartFishingEvent;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class StartFishingHandler implements Consumer<StartFishingEvent> {
    @Override
    public void accept(StartFishingEvent event) {
        if (!event.playerRef().isValid())return;

        var store = event.playerRef().getStore();
        var player = store.getComponent(event.playerRef(), PlayerRef.getComponentType());

        var rpg = store.getComponent(event.playerRef(), PlayerRPGComponent.getComponentType());
        if(rpg == null) return;
        if(rpg.isFishing()) {
            player.sendMessage(Message.raw("Is already fishing"));
            return;
        }

        MovementManager movementManager = store.getComponent(event.playerRef(), MovementManager.getComponentType());
        MovementSettings movementSettings = movementManager.getSettings();

        movementSettings.baseSpeed = 1.0f; // Lower the base speed

        // 2. Prevent jumping
        movementSettings.jumpForce = 0.0f;

        // 3. Prevent movement while crouching
        movementSettings.forwardCrouchSpeedMultiplier = 0.0f;
        movementSettings.strafeCrouchSpeedMultiplier = 0.0f;

        // 4. Disable rolling
        movementSettings.minFallSpeedToEngageRoll = Float.MAX_VALUE;

        ServerCameraSettings cameraSettings = new ServerCameraSettings();

        cameraSettings.lookMultiplier = new Vector2f(0,0);
        cameraSettings.allowPitchControls = false;
        cameraSettings.rotationType = RotationType.Custom;
        cameraSettings.applyLookType = ApplyLookType.Rotation;
        cameraSettings.rotation = new Direction(0.5f,0.5f,0.5f);
        cameraSettings.distance = 4.0f;

        SetServerCamera packet = new SetServerCamera(ClientCameraView.ThirdPerson, true, cameraSettings);

        movementManager.update(player.getPacketHandler());
        player.getPacketHandler().writeNoCache(packet);

        player.sendMessage(Message.raw("Fishing!"));
        rpg.setFishing(true);
    }
}
