package org.example.handlers;

import org.example.components.PlayerRPGComponent;
import org.example.events.LevelUpEvent;
import org.example.events.RemoveXPEvent;

import java.util.function.Consumer;

public class RemoveXPHandler implements Consumer<RemoveXPEvent> {

    @Override
    public void accept(RemoveXPEvent event) {
        if(!event.playerRef().isValid()) return;

        var store = event.playerRef().getStore();

        var rpg = store.getComponent(event.playerRef(), PlayerRPGComponent.getComponentType());
        if(rpg == null) return;

        var oldLevel = rpg.getLevel();
        var leveldDown = rpg.removeExperience(event.amount());

        if (leveldDown){
            LevelUpEvent.dispatch(event.playerRef(), oldLevel, rpg.getLevel());
        }
    }
}
