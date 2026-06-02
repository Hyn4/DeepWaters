package org.example.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.example.level.XPTable;

import java.util.UUID;

public class PlayerRPGComponent implements Component<EntityStore>{

    private static ComponentType<EntityStore, PlayerRPGComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, PlayerRPGComponent> type){
        TYPE = type;
    }

    public static ComponentType<EntityStore, PlayerRPGComponent> getComponentType(){
        return TYPE;
    }

    public static final BuilderCodec<PlayerRPGComponent> CODEC =
            BuilderCodec
                    .builder(PlayerRPGComponent.class, PlayerRPGComponent::new)
                    .append(
                            new KeyedCodec<>("TotalExperience",Codec.LONG),
                            (component, value) -> component.totalExperience = value,
                            component -> component.totalExperience
                    ).add()
                    .append(
                            new KeyedCodec<>("BobberRef",Codec.UUID_BINARY),
                            (component, value) -> component.bobberId = value,
                            component -> component.bobberId
                    ).add().append(
                            new KeyedCodec<>("IsFishing",Codec.BOOLEAN),
                            (component, value) -> component.isFishing = value,
                            component -> component.isFishing
                    ).add()
                    .build();


    private long totalExperience = 0;

    private boolean isFishing = false;

    private UUID bobberId;

    public PlayerRPGComponent(){}

    public PlayerRPGComponent(long totalExperience){
        this.totalExperience = Math.max(0L, totalExperience);
    }

    public boolean isFishing() {
        return isFishing;
    }

    public void setFishing(boolean fishing) {
        isFishing = fishing;
    }

    public UUID getBobberId() {
        return bobberId;
    }

    public void setBobberId(UUID bobberRef) {
        this.bobberId = bobberRef;
    }

    public long getTotalExeperience(){
        return totalExperience;
    }

    public void setTotalExperience(long amount){
        this.totalExperience = Math.max(0L,amount);
    }

    public int getLevel(){
        return XPTable.getLevelFromXP(totalExperience);
    }

    public long getCurrentLevelXP(){
        return XPTable.getXPInCurrentLevel(totalExperience);
    }

    public long getXPtoNextLevel(){
        return XPTable.getXPToNextLevel(totalExperience);
    }

    public float getProgress(){
        return XPTable.getProgressToNextLevel(totalExperience);
    }

    public boolean isMaxLevel(){
        return getLevel() >= XPTable.MAX_LEVEL;
    }

    public boolean addExperience(long amount){
        if (amount <= 0 ) return false;

        int oldLevel = getLevel();
        totalExperience +=amount;
        int newLevel = getLevel();

        return newLevel > oldLevel;
    }

    public boolean removeExperience(long amount){
        if (amount <= 0) return false;
        if (totalExperience <= 0) return false;

        int oldLevel = getLevel();
        totalExperience -= amount;
        int newLevel = getLevel();

        return newLevel < oldLevel;
    }

    @NullableDecl
    @Override
    public PlayerRPGComponent clone(){
        return new PlayerRPGComponent(this.totalExperience);
    }

    @Override
    public String toString(){
        return "PlayerRPGComponent{level = " + getLevel() +
                ", total XP = " + totalExperience +
                ", toNext = " + getXPtoNextLevel() + "}";
    }
}


