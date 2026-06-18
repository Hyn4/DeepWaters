package org.example.handlers;


import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import org.example.components.BobberPhysicsComponent;
import org.example.components.FishComponent;
import org.example.components.FishermanComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.CatchFishEvent;
import org.example.events.GiveXPEvent;
import org.example.events.StartMinigameEvent;
import org.example.events.StopFishingEvent;
import org.example.utils.FishType;
import org.joml.Vector3d;

import java.util.Random;
import java.util.function.Consumer;

public class StartMinigameHandler implements Consumer<StartMinigameEvent> {

    @Override
    public void accept(StartMinigameEvent event){
        if (!event.player().isValid()) return;
        var store = event.player().getStore();
        var playerRef = store.getComponent(event.player(), PlayerRef.getComponentType());
        var commandBuffer = event.commandBuffer();
        var playerPos = store.getComponent(event.player(), TransformComponent.getComponentType()).getPosition();

        var rpgComponent = store.getComponent(event.player(), PlayerRPGComponent.getComponentType());

        var bobberRef = store.getExternalData().getRefFromUUID(rpgComponent.getBobberId());

        if(!bobberRef.isValid()){
            playerRef.sendMessage(Message.raw("DEU MERDA no bobberRef"));
            StopFishingEvent.dispatch(event.player());
            return;
        }

        var bobberPyhsicsComponent = store.getComponent(bobberRef, BobberPhysicsComponent.getComponentType());

        if(bobberPyhsicsComponent == null){
            playerRef.sendMessage(Message.raw("DEU MERDA no bobberphysics"));
            StopFishingEvent.dispatch(event.player());
            return;
        }

        var playerId = bobberPyhsicsComponent.getPlayerId();

        var bobberPos = store.getComponent(bobberRef,TransformComponent.getComponentType()).getPosition();

        double distanceXZ = new Vector3d(playerPos.x, 0, playerPos.z)
                .distance(new Vector3d(bobberPos.x, 0, bobberPos.z));

        double dx = bobberPos.x - playerPos.x;
        double dz = bobberPos.z - playerPos.z;

        FishType fishType = FishType.getWeightedRandom(new Random());

        playerRef.sendMessage(Message.raw(fishType.toString()));

        var fishComponent = new FishComponent(playerId, (float)distanceXZ, Math.atan2(dz, dx), fishType);

        //coloca o fish component e passa o playerID
        commandBuffer.addComponent(bobberRef, FishComponent.getComponentType(),fishComponent);

        //tira o bobber physics system
        commandBuffer.removeComponent(bobberRef, BobberPhysicsComponent.getComponentType());

        rpgComponent.setFishBiting(false);
    }


}
