package com.aguilam.journeyrecruits;

import com.aguilam.journeyrecruits.config.RecruitBridgeConfig;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.MarkerOverlay;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@ClientPlugin
public class JMPlugin implements IClientPlugin {

    static JMPlugin INSTANCE;
    private IClientAPI api;
    private final Logger logger = RecruitBridgeMod.LOGGER;

    private Method mGetUUID, mGetName, mGetClaimedChunks, mGetOwnerFaction;
    private boolean reflectionSetup = false;
    private boolean reflectionFailed = false;

    private Method mGetStringID, mGetTeamDisplayName;
    private boolean factionReflectionSetup = false;

    private final Map<String, Integer> factionColorCache = new HashMap<>();

    private static final int[] RECRUITS_PALETTE = {
        0xFFFFFF, 0x000000, 0xD3D3D3, 0x808080, 0x404040,
        0x337B9A, 0x335C9A, 0x15253E, 0x269C08, 0x008000,
        0x0E3903, 0xB91515, 0x941010, 0x4A0808, 0x8C4C18,
        0x5E3B1D, 0x3F2713, 0x009696, 0x174444, 0x0B2222,
        0xEDCA12, 0xE56A11, 0xFF00FF, 0x570265, 0xDD9A19
    };

    private static final int[] FALLBACK_PALETTE = {
        0xFF4444, 0x44FF44, 0x4488FF, 0xFFFF44
    };

    private static final ResourceLocation ICON_LOC =
        new ResourceLocation("journeymap", "ui/img/waypoint-icon.png");

    private static class FactionData {
        final String displayName;
        final int color;
        final List<ChunkPos> allChunks = new ArrayList<>();
        final List<String> claimNames = new ArrayList<>();

        FactionData(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    @Override
    public void initialize(IClientAPI api) {
        this.api = api;
        INSTANCE = this;
    }

    @Override
    public String getModId() {
        return RecruitBridgeMod.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event) {}

    public boolean isReady() {
        return api != null;
    }

    @SuppressWarnings("unchecked")
    public void updateClaims(List<?> claims) {
        if (claims == null || claims.isEmpty()) {
            if (isReady()) {
                try { api.removeAll(RecruitBridgeMod.MOD_ID); } catch (Exception ignored) {}
            }
            return;
        }

        setupReflection(claims.get(0).getClass());
        if (reflectionFailed) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ResourceKey<Level> dim = mc.level.dimension();

        try {
            api.removeAll(RecruitBridgeMod.MOD_ID);
            factionColorCache.clear();

            Map<String, FactionData> factionMap = new LinkedHashMap<>();

            for (Object claim : claims) {
                try {
                    List<ChunkPos> chunks = (List<ChunkPos>) mGetClaimedChunks.invoke(claim);
                    if (chunks == null || chunks.isEmpty()) continue;

                    String claimName = (String) mGetName.invoke(claim);

                    Object faction = mGetOwnerFaction.invoke(claim);
                    if (faction == null) continue;

                    setupFactionReflection(faction.getClass());

                    String factionId = (String) mGetStringID.invoke(faction);

                    if (!factionMap.containsKey(factionId)) {
                        String displayName = (String) mGetTeamDisplayName.invoke(faction);
                        int color = getUnitColorFromFaction(faction, factionId);
                        factionMap.put(factionId, new FactionData(displayName, color));
                    }

                    FactionData fd = factionMap.get(factionId);

                    for (ChunkPos chunk : chunks) {
                        fd.allChunks.add(chunk);
                        fd.claimNames.add(claimName);
                    }
                } catch (Exception e) {
                }
            }

            for (Map.Entry<String, FactionData> entry : factionMap.entrySet()) {
                String factionId = entry.getKey();
                FactionData data = entry.getValue();

                for (int i = 0; i < data.allChunks.size(); i++) {
                    ChunkPos chunk = data.allChunks.get(i);
                    String claimName = data.claimNames.get(i);

                    int x1 = chunk.x * 16;
                    int z1 = chunk.z * 16;
                    int x2 = x1 + 16;
                    int z2 = z1 + 16;

                    MapPolygon polygon = new MapPolygon(
                        new BlockPos(x1, 70, z1),
                        new BlockPos(x2, 70, z1),
                        new BlockPos(x2, 70, z2),
                        new BlockPos(x1, 70, z2)
                    );

                    ShapeProperties shape = new ShapeProperties()
                        .setStrokeWidth(0)
                        .setStrokeOpacity(0f)
                        .setFillColor(data.color)
                        .setFillOpacity(0.4f);

                    String polyId = "cl_" + factionId + "_" + i;
                    PolygonOverlay overlay = new PolygonOverlay(
                        RecruitBridgeMod.MOD_ID, polyId, dim, shape, polygon
                    );
                    overlay.setTitle(claimName);

                    try { api.show(overlay); } catch (Exception ignored) {}
                }

                BlockPos centerPos = calculateCenter(data.allChunks);

                MapImage icon = new MapImage(ICON_LOC, 0, 0, 16, 16, data.color, 0.01f);
                icon.setAnchorX(8);
                icon.setAnchorY(8);

                String markerId = "lbl_" + factionId;
                MarkerOverlay marker = new MarkerOverlay(
                    RecruitBridgeMod.MOD_ID, markerId, centerPos, icon
                );
                marker.setDimension(dim);
                marker.setLabel(data.displayName.toUpperCase());

                try {
                    double scale = RecruitBridgeConfig.CLIENT.labelFontScale.get();
                    double maxZoom = RecruitBridgeConfig.CLIENT.nameShowMaxZoom.get();
                    TextProperties tp = new TextProperties()
                        .setColor(0xFFFFFF)
                        .setScale((float) scale)
                        .setMaxZoom((int) Math.round(maxZoom));
                    marker.setTextProperties(tp);
                } catch (Exception ignored) {
                }

                if (RecruitBridgeConfig.CLIENT.showLabelOnMinimap.get()) {
                    setOverlayDisplayOnMinimap(marker, true);
                }

                try { api.show(marker); } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            logger.error("[RecruitBridge-JM] Error drawing claims", e);
        }
    }

    private BlockPos calculateCenter(List<ChunkPos> chunks) {
        double totalX = 0;
        double totalZ = 0;
        for (ChunkPos c : chunks) {
            totalX += (c.x * 16) + 8;
            totalZ += (c.z * 16) + 8;
        }
        return new BlockPos(
            (int) (totalX / chunks.size()),
            100,
            (int) (totalZ / chunks.size())
        );
    }

    private int getUnitColorFromFaction(Object faction, String factionId) {
        if (factionColorCache.containsKey(factionId)) {
            return factionColorCache.get(factionId);
        }
        int color = -1;
        try {
            Field f = faction.getClass().getDeclaredField("unitColor");
            f.setAccessible(true);
            byte id = f.getByte(faction);
            if (id >= 0 && id < RECRUITS_PALETTE.length) color = RECRUITS_PALETTE[id];
        } catch (Exception ignored) {}
        if (color == -1) {
            try {
                Method m = faction.getClass().getMethod("getUnitColor");
                byte id = (byte) m.invoke(faction);
                if (id >= 0 && id < RECRUITS_PALETTE.length) color = RECRUITS_PALETTE[id];
            } catch (Exception ignored) {}
        }
        if (color == -1) color = FALLBACK_PALETTE[Math.abs(factionId.hashCode()) % FALLBACK_PALETTE.length];
        factionColorCache.put(factionId, color);
        return color;
    }

    private void setupFactionReflection(Class<?> factionClass) {
        if (factionReflectionSetup) return;
        try {
            mGetStringID = factionClass.getMethod("getStringID");
            mGetTeamDisplayName = factionClass.getMethod("getTeamDisplayName");
            factionReflectionSetup = true;
        } catch (Exception e) {
            logger.error("Faction reflection failed", e);
            reflectionFailed = true;
        }
    }

    private void setupReflection(Class<?> claimClass) {
        if (reflectionSetup || reflectionFailed) return;
        try {
            mGetUUID = claimClass.getMethod("getUUID");
            mGetName = claimClass.getMethod("getName");
            mGetClaimedChunks = claimClass.getMethod("getClaimedChunks");
            mGetOwnerFaction = claimClass.getMethod("getOwnerFaction");
            reflectionSetup = true;
        } catch (Exception e) {
            logger.error("Reflection setup failed", e);
            reflectionFailed = true;
        }
    }

    private void setOverlayDisplayOnMinimap(MarkerOverlay overlay, boolean showOnMinimap) {
        try {
            for (String methodName : new String[] { "setDisplayOnMinimap", "setShowOnMinimap", "setMinimapVisible" }) {
                try {
                    Method m = overlay.getClass().getMethod(methodName, boolean.class);
                    m.invoke(overlay, showOnMinimap);
                    return;
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}
    }
}