package org.example.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.example.components.PlayerRPGComponent;
import org.example.events.GiveXPEvent;
import org.example.events.RemoveXPEvent;
import org.example.utils.FishType;
import org.example.utils.FishingContext;

import java.util.ArrayList;

public class DisplayFishPoolCommand extends AbstractPlayerCommand {
    private static final int DEFAULT_AMOUNT = 50;

    public DisplayFishPoolCommand(){
        super("pool","Display Fish pool for current context");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        var pos = store.getComponent(ref, TransformComponent.getComponentType()).getPosition();

        FishingContext context = FishingContext.getContext(pos, store, ref);

        playerRef.sendMessage(Message.raw(context.toString()));

        playerRef.sendMessage(Message.raw("POOL: "));

        ArrayList<FishType> pool = FishType.getFishPool(context);

        for (FishType f : pool){
            playerRef.sendMessage(Message.raw(f.toString()));
        }
    }
}
