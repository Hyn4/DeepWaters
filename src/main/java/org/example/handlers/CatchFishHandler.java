package org.example.handlers;


import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import org.example.events.CatchFishEvent;
import org.example.events.GiveXPEvent;
import org.joml.Vector3d;

import java.util.function.Consumer;

public class CatchFishHandler implements Consumer<CatchFishEvent> {

    @Override
    public void accept(CatchFishEvent event){
        if (!event.player().isValid()) return;
        var store = event.player().getStore();
        var playerRef = store.getComponent(event.player(), PlayerRef.getComponentType());

        String fishId = "Template_Fish_Item";

        ItemStack fishStack = InventoryHelper.createItem(fishId);

        if(fishStack == null){
            playerRef.sendMessage(Message.raw("no fish =("));
        }

        TransformComponent transform = store.getComponent(event.player(), TransformComponent.getComponentType());
        Vector3d spawnPos = transform.getPosition();

        playerRef.sendMessage(Message.raw("FISH!"));
        GiveXPEvent.dispatch(event.player(),50L);

        ItemUtils.interactivelyPickupItem(event.player(), fishStack, spawnPos, store);
        playerRef.sendMessage(Message.raw("FISH!"));

    }

}
