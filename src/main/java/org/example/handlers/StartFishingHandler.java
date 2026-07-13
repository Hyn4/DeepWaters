package org.example.handlers;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.protocol.packets.interface_.SetPage;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import org.example.Pages.FishingPage;
import org.example.components.PlayerRPGComponent;
import org.example.events.CameraControllerEvent;
import org.example.events.StartFishingEvent;
import org.example.events.StopFishingEvent;
import org.example.ui.FishingUI;
import org.example.utils.CameraState;
import org.joml.Vector2f;
import org.joml.Vector3f;

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
            return;
        }
        var playerObj = store.getComponent(event.playerRef(), Player.getComponentType());
        if (playerObj != null) {
            playerObj.getHudManager().addCustomHud(player, new FishingUI(player));
        }


        MovementManager movementManager = store.getComponent(event.playerRef(), MovementManager.getComponentType());
        MovementSettings movementSettings = movementManager.getSettings();
        movementSettings.baseSpeed = 1.0f;
        movementSettings.jumpForce = 0.0f;
        movementSettings.forwardCrouchSpeedMultiplier = 0.0f;
        movementSettings.strafeCrouchSpeedMultiplier = 0.0f;
        movementSettings.minFallSpeedToEngageRoll = Float.MAX_VALUE;
        movementManager.update(player.getPacketHandler());

        rpg.setFishing(true);




    }
}
