package org.example.interactions;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import org.example.components.BobberPhysicsComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.StopFishingEvent;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

public class MyCustomInteraction extends SimpleInstantInteraction {

    private final Vector3d bobbleSpawnPositionOffset = new Vector3d(0, 1.5, 0);
    private final Vector3d bobbleLaunchVelocityMultiplier = new Vector3d(15, 15, 15);

    public static final BuilderCodec<MyCustomInteraction> CODEC= BuilderCodec.builder(
            MyCustomInteraction.class, MyCustomInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType,
                            @NonNullDecl InteractionContext interactionContext,
                            @NonNullDecl CooldownHandler cooldownHandler) {


        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        Ref<EntityStore> playerRef = interactionContext.getOwningEntity();
        ItemStack heldItem = interactionContext.getHeldItem();
        PlayerRef ref = commandBuffer.getComponent(playerRef, PlayerRef.getComponentType());
        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        PlayerRPGComponent rpgComponent = commandBuffer.getComponent(playerRef, PlayerRPGComponent.getComponentType());
        EntityStore entityStore = commandBuffer.getStore().getExternalData().getWorld().getEntityStore();
        UUID playerUuid = commandBuffer.getComponent(playerRef, UUIDComponent.getComponentType()).getUuid();
        if(playerUuid == null){
            ref.sendMessage(Message.raw("Player sem UUID"));
            return;
        }
        ref.sendMessage(Message.raw("Player UUID: %s".formatted(playerUuid.toString())));


        if(commandBuffer == null || playerRef==null || heldItem==null || ref==null || player==null || rpgComponent == null) return;
        ref.sendMessage(Message.raw("Checkpoint 1"));


        if(rpgComponent.getBobberId() == null) {
            TransformComponent playerTransformComponent = playerRef.getStore().getComponent(playerRef, TransformComponent.getComponentType());
            Transform playerTransform = TargetUtil.getLook(playerRef, commandBuffer);
            Vector3d bobbleSpawnPos = new Vector3d(playerTransformComponent.getPosition());
            bobbleSpawnPos.add(bobbleSpawnPositionOffset);
            Vector3d playerLookDir = playerTransform.getDirection();
            HeadRotation playerHead = commandBuffer.getComponent(playerRef, HeadRotation.getComponentType());
            Rotation3f rotation = new Rotation3f();
            rotation.setYaw(playerHead.getRotation().yaw() + (float) (Math.PI / 180.0) * 180.0F);
            Vector3d bobberLaunchVelocity = new Vector3d(
                    playerLookDir.x * bobbleLaunchVelocityMultiplier.x,
                    (playerLookDir.y * bobbleLaunchVelocityMultiplier.y) + 2,
                    playerLookDir.z * bobbleLaunchVelocityMultiplier.z
            );

            if (playerTransformComponent == null || playerTransform == null || bobbleSpawnPos == null || playerLookDir == null || playerHead == null)
                return;
            ref.sendMessage(Message.raw("Checkpoint 2"));


            Holder<EntityStore> bobberHolder = EntityStore.REGISTRY.newHolder();


            //bobberHolder.addComponent(HeadRotation.getComponentType(), new HeadRotation());
            bobberHolder.addComponent(TransformComponent.getComponentType(), new TransformComponent(bobbleSpawnPos, rotation));
            bobberHolder.addComponent(Velocity.getComponentType(), new Velocity(bobberLaunchVelocity));
            bobberHolder.ensureComponent(PhysicsValues.getComponentType());
            bobberHolder.putComponent(NetworkId.getComponentType(), new NetworkId(playerRef.getStore().getExternalData().takeNextNetworkId()));
            bobberHolder.addComponent(BobberPhysicsComponent.getComponentType(), new BobberPhysicsComponent(playerUuid));

            //if(playerTransformComponent == null || playerTransform==null || bobbleSpawnPos==null || playerLookDir==null || playerHead==null ) return;

            ref.sendMessage(Message.raw("Checkpoint 3: x %f y %f z %f".formatted(bobberLaunchVelocity.x, bobberLaunchVelocity.y, bobberLaunchVelocity.z)));

            //TODO: custom physics component
            //TODO: bobber component (que na verdade é a dificuldade do minigame - status do peixe)

            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Bobber");
            if (modelAsset == null) {
                modelAsset = ModelAsset.DEBUG;
                ref.sendMessage(Message.raw("Checkpoint 4"));
            }
            Model model = Model.createScaledModel(modelAsset, 2f);
            bobberHolder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            bobberHolder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            if (model.getBoundingBox() != null)
                bobberHolder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));


            UUID bobberId = UUID.randomUUID();

            bobberHolder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(bobberId));

            rpgComponent.setBobberId(bobberId);

            ref.sendMessage(Message.raw("Checkpoint 5, Id: %s".formatted(bobberId.toString())));

            commandBuffer.getExternalData().getWorld().execute(() -> {
                commandBuffer.addEntity(bobberHolder, AddReason.SPAWN);
            });

            ref.sendMessage(Message.raw("Checkpoint 6"));
        }else{

            ref.sendMessage(Message.raw("Removing bobber"));

            UUID bobberUuid = rpgComponent.getBobberId();

            final Ref<EntityStore> bobberRef = entityStore.getRefFromUUID(bobberUuid);

            try {
                commandBuffer.getExternalData().getWorld().execute(() -> {
                    if (!bobberRef.isValid()) {
                        return;
                    }
                    try {
                        entityStore.getStore().removeEntity(bobberRef, RemoveReason.REMOVE);
                    } catch (Exception e) {
                        ref.sendMessage(Message.raw(e.toString()));
                    }
                });
            } catch (Exception e) {
                ref.sendMessage(Message.raw("Failed to enqueue bobber remove"));
            }

            rpgComponent.setBobberId(null);
            StopFishingEvent.dispatch(playerRef);
        }

    }




}
