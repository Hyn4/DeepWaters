package org.example.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.example.components.PlayerRPGComponent;
import org.example.events.GiveXPEvent;
import org.example.events.RemoveXPEvent;

public class RPGXpCommand extends AbstractPlayerCommand {
    private static final int DEFAULT_AMOUNT = 50;
    private final OptionalArg<Integer> amountArg;

    public RPGXpCommand(){
        super("xp","Give yourself XP");
        this.amountArg = withOptionalArg("amount", "XP amount", ArgTypes.INTEGER);

    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        var amount = amountArg.get(commandContext);
        if (amount ==null) amount = DEFAULT_AMOUNT;

        if(store.getComponent(ref, PlayerRPGComponent.getComponentType()) == null){
            playerRef.sendMessage(Message.raw("No RPG component found"));
            return;
        }

        playerRef.sendMessage(Message.raw("%d XP".formatted(amount)));

        if(amount > 0){
            GiveXPEvent.dispatch(ref,amount);
        }else{
            amount = amount*-1;
            RemoveXPEvent.dispatch(ref,amount);
        }

    }
}
