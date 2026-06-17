package org.example.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
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

        reelInAudio = SoundEvent.getAssetMap().getIndex(REEL_IN_SFX);
        reelOutAudio = SoundEvent.getAssetMap().getIndex(REEL_OUT_SFX);
        lineBreakAduio = SoundEvent.getAssetMap().getIndex(LINE_BREAK_SFX);
        maxTensionAudio = SoundEvent.getAssetMap().getIndex(MAX_TENSION_SFX);
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

    public float getMaxTension() {
        return maxTension;
    }

    public UICommandBuilder uiCommandBuilder;
    private double side;
    private double height;

    public boolean isReeling() {
        return isReeling;
    }

    public void setReeling(boolean reeling) {
        isReeling = reeling;
    }

    public void switchReelingState(){
        this.setReeling(!this.isReeling());
    }

    private boolean isReeling;
    private float maxTension = 100f;

    public final String REEL_IN_SFX = "SFX_Reel_In";
    public final String REEL_OUT_SFX = "SFX_Reel_Out";
    public final String LINE_BREAK_SFX = "SFX_Line_Break";
    public final String MAX_TENSION_SFX = "SFX_Max_Tension";


    public int getReelInAudio() {
        return reelInAudio;
    }

    public int getReelOutAudio() {
        return reelOutAudio;
    }

    public int getMaxTensionAudio() {
        return maxTensionAudio;
    }

    public int getLineBreakAduio() {
        return lineBreakAduio;
    }

    private int reelInAudio;
    private int reelOutAudio;
    private int maxTensionAudio;
    private int lineBreakAduio;

}
