package org.example.handlers;

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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.example.Pages.FishingPage;
import org.example.components.PlayerRPGComponent;
import org.example.events.StartFishingEvent;
import org.example.events.StopFishingEvent;

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


        //SetServerCamera packet = new SetServerCamera(ClientCameraView.ThirdPerson, true, new ServerCameraSettings());

        //player.getPacketHandler().writeNoCache(packet);

        movementManager.update(player.getPacketHandler());

        player.sendMessage(Message.raw("Fishing!"));
        rpg.setFishing(true);
    }
}
