package org.example.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class FishingUI extends CustomUIHud {


    public FishingUI(@Nonnull PlayerRef player){
       super(player, "FishingHudKey");
   }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/FishingHUD.ui");
        uiCommandBuilder.set("#TensionLabel.TextSpans", Message.raw("NewText"));
    }
}
