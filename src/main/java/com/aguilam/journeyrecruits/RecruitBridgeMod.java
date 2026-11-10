package com.aguilam.journeyrecruits;

import com.aguilam.journeyrecruits.config.RecruitBridgeConfig;
import com.aguilam.journeyrecruits.config.RecruitBridgeConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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

    public RecruitBridgeMod(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, RecruitBridgeConfig.CLIENT_SPEC, "journeyrecruits-client.toml");
        if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("cloth_config")) {
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (IConfigScreenFactory) (modContainer, parent) -> RecruitBridgeConfigScreen.build(parent));
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        private static int tick = 0;
        private static Field claimsField = null;
        private static boolean reflectionFailed = false;

        private static final int UPDATE_INTERVAL = 240;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
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
