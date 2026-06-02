package org.example.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.xml.validation.Validator;
import java.util.concurrent.ThreadLocalRandom;

public class RPGSpawnCommand extends AbstractPlayerCommand {

    private static final String DEFAULT_TYPE = "Skeleton";
    private static final int DEFAULT_COUNT = 5;
    private static final int MAX_COUNT = 50;

    private final OptionalArg<String> typeArg;
    private final OptionalArg<Integer> countArg;

    public RPGSpawnCommand(){
        super("spawn", "Spawn NPCs around you");
        this.typeArg = withOptionalArg("type", "type of monster to spawn", ArgTypes.STRING);
        this.countArg = withOptionalArg("count", "amount of monsters to spawn", ArgTypes.INTEGER)
                .addValidator(Validators.greaterThanOrEqual(1))
                .addValidator(Validators.lessThan(MAX_COUNT));
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        var npcType = typeArg.get(commandContext);
        if(npcType == null) npcType = DEFAULT_TYPE;

        var count = countArg.get(commandContext);
        if(count == null) count = DEFAULT_COUNT;

        var transform = store.getComponent(ref, TransformComponent.getComponentType());
        if( transform == null){
            playerRef.sendMessage(Message.raw("Error: could not get transform"));
            return;
        }

        var roleIndex = NPCPlugin.get().getIndex(npcType);
        if(roleIndex < 0){
            playerRef.sendMessage(Message.raw("Unknown NPC: %s".formatted(npcType)));
        }

        var playerPos = transform.getPosition();
        var worldStore = world.getEntityStore().getStore();
        var random = ThreadLocalRandom.current();
        var spawned = 0;

        for (int i=0; i<count; i++){
            var spawnPos = new Vector3d(
                    playerPos.x + random.nextDouble() * 6 - 3,
                    playerPos.y + 0.5,
                    playerPos.z + random.nextDouble() * 6 - 3
            );

            Rotation3fc rotation = new Rotation3f();
            var result = NPCPlugin.get().spawnNPC(
                    worldStore, npcType, null, spawnPos, rotation
            );

            if (result != null && result.first() != null) spawned++;
        }

        playerRef.sendMessage(Message.raw("You spawned %d %s".formatted(spawned,npcType)));
    }
}
