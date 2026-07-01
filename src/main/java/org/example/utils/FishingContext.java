package org.example.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.Position;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record FishingContext(
        int hour,
        String zone,
        int tier,
        double yPos,
        int waterDepth
) {
    public static FishingContext getContext(Vector3d position, Store<EntityStore> store, Ref<EntityStore> player){
        var worldMapTracker = store.getComponent(player, Player.getComponentType()).getWorldMapTracker();
        WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
        String regionName = currentZone.regionName();
        ZoneInfo zoneInfo = EnvironmentParser.parse(regionName);
        long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
        WorldChunk chunk = store.getExternalData().getWorld().getChunk(chunkIndex);
        WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
        int currentHour = timeResource.getCurrentHour();
        int depth = 0;
        if (chunk != null) {
            // 2. Get local coordinates inside the chunk (0-31)
            int localX = (int) position.x & ChunkUtil.SIZE_MASK;
            int localZ = (int) position.z & ChunkUtil.SIZE_MASK;


            // 3. Query the chunk's heightmap to get the sea-floor Y level
            short floorY = chunk.getHeight(localX, localZ);


            // 4. Calculate depth relative to the bobber
            depth = (int) (position.y - floorY);
        }

        return new FishingContext(currentHour,zoneInfo.zone(), zoneInfo.tier(), position.y, depth);
    }

    public static FishingContext defaultContext(){
        return new FishingContext(12,"1",1,115,5);
    }

    public String ToString(){
        return String.format(
            """
            [CONTEXT]
              Zone  : %s | Tier  : %d
              Depth : %d  |  Y position : %f | Hour : %d
            """,
            zone, tier,
            waterDepth, yPos, hour
        );
    }
}
