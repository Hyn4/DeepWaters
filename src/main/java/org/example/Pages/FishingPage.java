package org.example.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.function.Consumer;

public class FishingPage extends CustomUIPage {
    private final Consumer<PlayerRef> onCancel;
    public FishingPage(PlayerRef playerRef, Consumer<PlayerRef> onCancel) {
        // Pass 'CanDismiss' so the client knows it's allowed to close this page with ESC
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.onCancel = onCancel;
    }
    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder commands, @NonNullDecl UIEventBuilder events, @NonNullDecl Store<EntityStore> store) {
        // You don't need to add any buttons or commands if you just want it to be a logic hook
    }
    @Override
    public void onDismiss(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        // THIS IS CALLED AUTOMATICALLY BY PAGE MANAGER ON ESC
        onCancel.accept(this.playerRef);
    }
}