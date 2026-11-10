package com.aguilam.journeyrecruits.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RecruitBridgeConfig {

    public static final String CATEGORY_LABELS = "labels";

    public static class Client {

        public final ModConfigSpec.DoubleValue nameShowMaxZoom;
        public final ModConfigSpec.DoubleValue labelFontScaleFullscreen;
        public final ModConfigSpec.DoubleValue labelFontScaleMinimap;
        public final ModConfigSpec.BooleanValue showLabelOnMinimap;

        public Client(ModConfigSpec.Builder builder) {
            builder.push(CATEGORY_LABELS);

            nameShowMaxZoom = builder
                    .translation("config.journeyrecruits.nameShowMaxZoom")
                    .defineInRange("nameShowMaxZoom", 2.0, 0.5, 10.0);

            labelFontScaleFullscreen = builder
                    .translation("config.journeyrecruits.labelFontScaleFullscreen")
                    .defineInRange("labelFontScaleFullscreen", 2.5, 0.5, 8.0);

            labelFontScaleMinimap = builder
                    .translation("config.journeyrecruits.labelFontScaleMinimap")
                    .defineInRange("labelFontScaleMinimap", 1.5, 0.5, 8.0);

            showLabelOnMinimap = builder
                    .translation("config.journeyrecruits.showLabelOnMinimap")
                    .define("showLabelOnMinimap", true);

            builder.pop();
        }
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }
}
