package com.aguilam.journeyrecruits;

import com.aguilam.journeyrecruits.config.RecruitBridgeConfig;
import com.aguilam.journeyrecruits.config.RecruitBridgeConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

@Mod(RecruitBridgeMod.MOD_ID)
public class RecruitBridgeMod {

    public static final String MOD_ID = "journeyrecruits";
    public static final Logger LOGGER = LogManager.getLogger("RecruitBridge");

    public RecruitBridgeMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RecruitBridgeConfig.CLIENT_SPEC, "journeyrecruits-client.toml");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().getActiveContainer().registerExtensionPoint(
                    net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(
                            (mc, parent) -> RecruitBridgeConfigScreen.build(parent)
                    )
            );
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        private static int tick = 0;
        private static Field claimsField = null;
        private static boolean reflectionFailed = false;

        private static final int UPDATE_INTERVAL = 240; 

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            
            if (++tick < UPDATE_INTERVAL) return;
            tick = 0;

            JMPlugin plugin = JMPlugin.INSTANCE;
            if (plugin == null || !plugin.isReady()) return;

            List<?> rawClaims = getClaimsFromRecruits();
            
            plugin.updateClaims(rawClaims);
        }

        private static List<?> getClaimsFromRecruits() {
            if (reflectionFailed) return Collections.emptyList();

            try {
                if (claimsField == null) {
                    Class<?> clientManagerClass = Class.forName("com.talhanation.recruits.client.ClientManager");
                    
                    try {
                        claimsField = clientManagerClass.getDeclaredField("claims");
                    } catch (NoSuchFieldException e) {
                        try {
                            claimsField = clientManagerClass.getDeclaredField("clientClaims");
                        } catch (NoSuchFieldException ignored) {}
                    }

                    if (claimsField == null) {
                        for (Field f : clientManagerClass.getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers()) &&
                                List.class.isAssignableFrom(f.getType())) {
                                claimsField = f;
                                LOGGER.info("[RecruitBridge] Found candidate field: " + f.getName());
                                break;
                            }
                        }
                    }

                    if (claimsField != null) {
                        claimsField.setAccessible(true);
                        LOGGER.info("[RecruitBridge] Hooked into Recruits claims field successfully!");
                    } else {
                        LOGGER.error("[RecruitBridge] Could not find claims list in ClientManager!");
                        reflectionFailed = true;
                        return Collections.emptyList();
                    }
                }

                Object obj = claimsField.get(null);
                if (obj instanceof List) {
                    return (List<?>) obj;
                }

            } catch (Exception e) {
                LOGGER.error("[RecruitBridge] Error fetching claims", e);
                reflectionFailed = true;
            }

            return Collections.emptyList();
        }
    }
}