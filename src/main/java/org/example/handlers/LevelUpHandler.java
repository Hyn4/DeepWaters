package org.example.handlers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.example.events.LevelUpEvent;

import java.util.function.Consumer;

public class LevelUpHandler implements Consumer<LevelUpEvent> {

    @Override
    public void accept(LevelUpEvent event) {
        if(!event.playerRef().isValid()) return;

        var store = event.playerRef().getStore();
        var playerRef = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if(playerRef == null) return;


        if(event.levelsChanged() >= 1) playerRef.sendMessage(Message.raw("LEVEL UP! +%d Levels! Now level %d".formatted(event.levelsChanged(), event.newLevel())));
        if(event.levelsChanged() <= -1) playerRef.sendMessage(Message.raw("LEVEL DOWN! -%d Levels! Now level %d".formatted(event.levelsChanged(), event.newLevel())));
    }
}
