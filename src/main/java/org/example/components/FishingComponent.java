package org.example.components;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Component;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;


public class FishingComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, FishingComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, FishingComponent> type) {TYPE = type;}

    public static ComponentType<EntityStore, FishingComponent> getComponentType(){ return TYPE;}

    @NullableDecl
    @Override
    public Component<EntityStore> clone(){
        return new FishingComponent();
    }

    public FishingComponent(){}
}
