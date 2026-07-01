package org.example.handlers;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.example.components.PlayerRPGComponent;
import org.example.events.CameraControllerEvent;
import org.example.events.StopFishingEvent;

import java.util.function.Consumer;

public class CameraControllerHandler implements Consumer<CameraControllerEvent> {
    @Override
    public void accept(CameraControllerEvent event) {
        if (!event.playerRef().isValid())return;

        var playerRef = event.playerRef();
        var store = event.playerRef().getStore();
        var player = store.getComponent(playerRef, PlayerRef.getComponentType());
        var playerTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        var rot  = playerTransform.getRotation();
        float intensity;

        ServerCameraSettings settings = new ServerCameraSettings();


        //player.sendMessage(Message.raw("Attempting camera State: %s".formatted(event.cameraState().toString())));


        switch (event.cameraState()){
            case RIGHT -> {

                //settings.isFirstPerson = false;
                //settings.eyeOffset = true;
                //settings.attachedToType = AttachedToType.LocalPlayer;
                //settings.distance = 4.0F;
                //settings.positionOffset = new Position(1,2,0);
               // settings.applyLookType = ApplyLookType.LocalPlayerLookOrientation;
                //settings.rotationType = RotationType.Custom;
                //settings.rotation = new Direction(rot.y, rot.x, rot.z); // Current rotation
                settings.baseFov = 110f;

                //settings.movementForceRotationType = MovementForceRotationType.Custom;

                SetServerCamera packet = new SetServerCamera(ClientCameraView.Custom, false, settings);
                player.getPacketHandler().writeNoCache(packet);


            }

            case REEL_IN -> {
                settings.isFirstPerson = true;
                settings.eyeOffset = true;             // CRITICAL: Moves camera from feet to eye level
                settings.allowPitchControls = true;     // CRITICAL: Allows looking up and down
                settings.displayReticle = true;         // Keeps the crosshair/reticle visible
                settings.displayCursor = false;         // Keeps the cursor hidden (mouse controls camera)
// --- 2. Keep standard movement and look orientation ---
                settings.attachedToType = AttachedToType.LocalPlayer;
                settings.positionType = PositionType.AttachedToPlusOffset;
                settings.rotationType = RotationType.AttachedToPlusOffset;
                settings.canMoveType = CanMoveType.AttachedToLocalPlayer;
                settings.applyMovementType = ApplyMovementType.CharacterController;
                settings.applyLookType = ApplyLookType.LocalPlayerLookOrientation;
                settings.mouseInputType = MouseInputType.LookAtTarget;
// --- 3. Set the Custom FOV ---
                settings.baseFov = 30.0f; // Set your custom minigame FOV here (default is usually ~70)
// --- 4. Send the Packet ---
// Set isLocked to true so the player cannot bypass the camera settings (e.g. by pressing F5)
                player.getPacketHandler().writeNoCache(
                        new SetServerCamera(ClientCameraView.Custom, true, settings)
                );

            }

            case THIRD_PERSON -> {

                SetServerCamera packet = new SetServerCamera(ClientCameraView.ThirdPerson, false, new ServerCameraSettings());
                player.sendMessage(Message.raw("third person"));
                player.getPacketHandler().writeNoCache(packet);
            }

            case DEFAULT -> {
                SetServerCamera packet = new SetServerCamera(ClientCameraView.FirstPerson, true, null);
                player.sendMessage(Message.raw("DEFAULT"));
                player.getPacketHandler().writeNoCache(packet);
            }

            case STRUGGLE -> {
                //settings.isFirstPerson = false;
                //settings.positionLerpSpeed = 0.05F; // Extremely smooth/laggy
                //settings.rotationLerpSpeed = 0.05F;

                if(event.intensity() > 0) {
                    intensity = event.intensity();
                }else{
                    intensity = 0.03f;
                }

                player.getPacketHandler().writeNoCache(new CameraShakeEffect(1, intensity, AccumulationMode.Sum));
            }

            case null, default -> {
                player.sendMessage(Message.raw("Something went wrong!"));
            }
        }

        //player.sendMessage(Message.raw("Camera State set: %s".formatted(event.cameraState().toString())));


    }
}
