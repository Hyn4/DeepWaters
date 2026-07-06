package org.example;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.example.commands.RpgCommand;
import org.example.components.BobberPhysicsComponent;
import org.example.components.FishComponent;
import org.example.components.FishermanComponent;
import org.example.components.PlayerRPGComponent;
import org.example.events.*;
import org.example.handlers.*;
import org.example.interactions.StartReelingToggleInteraction;
import org.example.interactions.MyCustomInteraction;
import org.example.interactions.StopReelingToggleInteraction;
import org.example.systems.BobberPhysicsSystem;
import org.example.systems.FishingSystem;
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

        var fishType = registry.registerComponent(FishComponent.class, FishComponent::new);
        FishComponent.setComponentType(fishType);

        var fishermanType = registry.registerComponent(FishermanComponent.class, FishermanComponent::new);
        FishermanComponent.setComponentType(fishermanType);

        registry.registerSystem(new PlayerJoinSystem());
        registry.registerSystem(new XPGainSystem());
        registry.registerSystem(new BobberPhysicsSystem());
        registry.registerSystem(new FishingSystem());

        getEventRegistry().register(GiveXPEvent.class, new GiveXPHandler());
        getEventRegistry().register(LevelUpEvent.class, new LevelUpHandler());
        getEventRegistry().register(RemoveXPEvent.class, new RemoveXPHandler());
        getEventRegistry().register(StartFishingEvent.class, new StartFishingHandler());
        getEventRegistry().register(StopFishingEvent.class, new StopFishingHandler());
        getEventRegistry().register(CatchFishEvent.class, new CatchFishHandler());
        getEventRegistry().register(StartMinigameEvent.class, new StartMinigameHandler());
        getEventRegistry().register(CameraControllerEvent.class, new CameraControllerHandler());

        getCommandRegistry().registerCommand(new RpgCommand());

        getCodecRegistry(Interaction.CODEC).register("my_custom_interaction_id", MyCustomInteraction.class, MyCustomInteraction.CODEC);
        getCodecRegistry(Interaction.CODEC).register("start_reeling_toggle", StartReelingToggleInteraction.class, StartReelingToggleInteraction.CODEC);
        getCodecRegistry(Interaction.CODEC).register("stop_reeling_toggle", StopReelingToggleInteraction.class, StopReelingToggleInteraction.CODEC);



    }
}
