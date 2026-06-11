package org.example.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;


public class FishermanComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, FishermanComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, FishermanComponent> type) {TYPE = type;}

    public static ComponentType<EntityStore, FishermanComponent> getComponentType(){ return TYPE;}

    @NullableDecl
    @Override
    public Component<EntityStore> clone(){
        return new FishermanComponent();
    }

    public FishermanComponent(){
        this.uiCommandBuilder = new UICommandBuilder();
    }

    public FishermanComponent(float stamina, float staminaRegen) {
        this.uiCommandBuilder = new UICommandBuilder();
    }

    public double getSide() {
        return side;
    }

    public void setSide(double side) {
        this.side = side;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public UICommandBuilder uiCommandBuilder;
    private double side;
    private double height;

}
