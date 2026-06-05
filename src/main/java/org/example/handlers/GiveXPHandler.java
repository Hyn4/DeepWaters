package org.example.handlers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.example.components.PlayerRPGComponent;
import org.example.events.GiveXPEvent;
import org.example.events.LevelUpEvent;
import org.example.systems.PlayerJoinSystem;

import java.util.function.Consumer;

public class GiveXPHandler implements Consumer<GiveXPEvent> {
    @Override
    public void accept(GiveXPEvent event) {
        if (!event.playerRef().isValid()) return;

        var store = event.playerRef().getStore();

        var rpg = store.getComponent(event.playerRef(), PlayerRPGComponent.getComponentType());
        var player = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if(rpg == null) return;


        var oldLevel = rpg.getLevel();
        var leveledUp = rpg.addExperience(event.amount());

        player.sendMessage(Message.raw(" +%d XP".formatted(event.amount())));

        if (leveledUp){
            LevelUpEvent.dispatch(event.playerRef() , oldLevel , rpg.getLevel());
        }

    }
}
