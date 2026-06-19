package org.example.handlers;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
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
        var intensity = 0.03f;

        ServerCameraSettings settings = new ServerCameraSettings();

        //player.sendMessage(Message.raw("Attempting camera State: %s".formatted(event.cameraState().toString())));


        switch (event.cameraState()){
            case RIGHT -> {

                settings.isFirstPerson = false;
                settings.eyeOffset = true;
                settings.attachedToType = AttachedToType.LocalPlayer;
                settings.distance = 4.0F;

                settings.positionOffset = new Position(1,2,0);

                settings.applyLookType = ApplyLookType.LocalPlayerLookOrientation;

                settings.rotationType = RotationType.Custom;
                settings.rotation = new Direction(rot.y, rot.x, rot.z); // Current rotation

                settings.movementForceRotationType = MovementForceRotationType.Custom;

                SetServerCamera packet = new SetServerCamera(ClientCameraView.Custom, true, settings);
                player.getPacketHandler().writeNoCache(packet);


            }

            case THIRD_PERSON -> {
                SetServerCamera packet = new SetServerCamera(ClientCameraView.ThirdPerson, true, new ServerCameraSettings());
                player.getPacketHandler().writeNoCache(packet);
            }

            case DEFAULT -> {
                SetServerCamera packet = new SetServerCamera(ClientCameraView.FirstPerson, false, new ServerCameraSettings());
                player.getPacketHandler().writeNoCache(packet);
            }

            case STRUGGLE -> {
                //settings.isFirstPerson = false;
                settings.positionLerpSpeed = 0.05F; // Extremely smooth/laggy
                settings.rotationLerpSpeed = 0.05F;
                if(event.intensity() > 0) intensity = event.intensity();


                player.getPacketHandler().writeNoCache(new CameraShakeEffect(1, intensity, AccumulationMode.Sum));
            }

            case null, default -> {
                player.sendMessage(Message.raw("Something went wrong!"));
            }
        }

        //player.sendMessage(Message.raw("Camera State set: %s".formatted(event.cameraState().toString())));


    }
}
