package org.example.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.components.PlayerRPGComponent;
import org.example.events.GiveXPEvent;

import javax.annotation.Nullable;

public class XPGainSystem extends DeathSystems.OnDeathSystem {
    private static final long XP_PER_KILL = 100L;

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(DeathComponent.getComponentType());
    }

    @Override
    public void onComponentAdded(@NonNullDecl Ref<EntityStore> ref,
                                 @NonNullDecl DeathComponent deathComponent,
                                 @NonNullDecl Store<EntityStore> store,
                                 @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        //if death component is not null
        var deathInfo = deathComponent.getDeathInfo();
        if (deathInfo == null) return;

        if(!(deathInfo.getSource() instanceof Damage.EntitySource source)) return;

        var killerRef = source.getRef();
        if(!killerRef.isValid()) return;

        var killer = store.getComponent(killerRef, PlayerRef.getComponentType());
        if(killer == null) return;

        var playerRPGComponent = store.getComponent(killerRef, PlayerRPGComponent.getComponentType());
        if(playerRPGComponent == null) return;

        killer.sendMessage(Message.raw((" +%d XP".formatted(XP_PER_KILL))));
        GiveXPEvent.dispatch(killerRef,XP_PER_KILL);
    }
}
