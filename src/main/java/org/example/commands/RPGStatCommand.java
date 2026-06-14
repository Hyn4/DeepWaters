package org.example.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.example.components.PlayerRPGComponent;

public class RPGStatCommand extends AbstractPlayerCommand {

    public RPGStatCommand(){
        super("stats", "show your RPG stats");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        var rpg = store.getComponent(ref, PlayerRPGComponent.getComponentType());
        if (rpg == null){
            playerRef.sendMessage(Message.raw("No RPG component found!"));
            return;
        }

        var level = rpg.getLevel();
        var totalXP = rpg.getTotalExeperience();
        var currentXP = rpg.getCurrentLevelXP();
        var progress = rpg.getProgress()*100;
        var fishermanStrength = rpg.getFishermanStrenght();
        var toNext = (int) (rpg.getXPtoNextLevel());

        playerRef.sendMessage(Message.raw("======== RPG STATS ========="));
        playerRef.sendMessage(Message.raw("Level: %d%s".formatted(level,rpg.isMaxLevel() ? " (MAX) " : "")));
        playerRef.sendMessage(Message.raw("Total XP: %d".formatted(totalXP)));
        playerRef.sendMessage(Message.raw("Fisherman Strength : %.1f".formatted(fishermanStrength)));

        if (!rpg.isMaxLevel()){
            playerRef.sendMessage(Message.raw("Progress: %d/%d (%f%%)".formatted(currentXP,(currentXP + toNext), progress)));

            playerRef.sendMessage(Message.raw("To next level: %d XP".formatted(toNext)));
        }


    }
}
