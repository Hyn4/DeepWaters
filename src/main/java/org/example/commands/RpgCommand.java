package org.example.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class RpgCommand extends AbstractCommandCollection {

    public RpgCommand(){
        super("rpg", "RPG debug commands");
        addSubCommand(new RPGSpawnCommand());
        addSubCommand(new RPGXpCommand());
        addSubCommand(new RPGStatCommand());
        addSubCommand(new DisplayFishPoolCommand());
    }
}
