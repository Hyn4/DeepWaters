package org.example;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.example.commands.RpgCommand;
import org.example.components.BobberPhysicsComponent;
import org.example.components.FishingComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.*;
import org.example.handlers.*;
import org.example.interactions.MyCustomInteraction;
import org.example.systems.BobberPhysicsSystem;
import org.example.systems.PlayerJoinSystem;
import org.example.systems.XPGainSystem;

public class RPGmod extends JavaPlugin {

    public RPGmod(JavaPluginInit init){
        super(init);
    }

    @Override
    protected void setup(){
        var registry = getEntityStoreRegistry();

        var rpgType = registry.registerComponent(
                PlayerRPGComponent.class,
                "MiniRPG_PlayerData",
                PlayerRPGComponent.CODEC
        );
        PlayerRPGComponent.setComponentType(rpgType);

        var bobberPhysicsType = registry.registerComponent(
                BobberPhysicsComponent.class,
                "BobberPhysics_data",
                BobberPhysicsComponent.CODEC);
        BobberPhysicsComponent.setComponentType(bobberPhysicsType);

        var fishingType = registry.registerComponent(FishingComponent.class, FishingComponent::new);
        FishingComponent.setComponentType(fishingType);

        registry.registerSystem(new PlayerJoinSystem());
        registry.registerSystem(new XPGainSystem());
        registry.registerSystem(new BobberPhysicsSystem());

        getEventRegistry().register(GiveXPEvent.class, new GiveXPHandler());
        getEventRegistry().register(LevelUpEvent.class, new LevelUpHandler());
        getEventRegistry().register(RemoveXPEvent.class, new RemoveXPHandler());
        getEventRegistry().register(StartFishingEvent.class, new StartFishingHandler());
        getEventRegistry().register(StopFishingEvent.class, new StopFishingHandler());
        getEventRegistry().register(CatchFishEvent.class, new CatchFishHandler());

        getCommandRegistry().registerCommand(new RpgCommand());

        getCodecRegistry(Interaction.CODEC).register("my_custom_interaction_id", MyCustomInteraction.class, MyCustomInteraction.CODEC);


    }
}
