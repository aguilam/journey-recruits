package com.aguilam.journeyrecruits.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RecruitBridgeConfig {

    public static final String CATEGORY_LABELS = "labels";

    public static class Client {

        public final ForgeConfigSpec.DoubleValue nameShowMaxZoom;
        public final ForgeConfigSpec.DoubleValue labelFontScaleFullscreen;
        public final ForgeConfigSpec.DoubleValue labelFontScaleMinimap;
        public final ForgeConfigSpec.BooleanValue showLabelOnMinimap;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment(" Настройки отображения названий государств на карте JourneyMap ")
                    .push(CATEGORY_LABELS);

            nameShowMaxZoom = builder
                    .comment(" При каком приближении (зуме) появляется название государства. Больше значение = название видно при более отдалённом виде. Обычно 1–5. ")
                    .translation("config.journeyrecruits.nameShowMaxZoom")
                    .defineInRange("nameShowMaxZoom", 2.0, 0.5, 10.0);

            labelFontScaleFullscreen = builder
                    .comment(" Размер шрифта названия государства на большой карте (1.0 = стандарт, 2–4 = крупнее). ")
                    .translation("config.journeyrecruits.labelFontScaleFullscreen")
                    .defineInRange("labelFontScaleFullscreen", 2.5, 0.5, 8.0);

            labelFontScaleMinimap = builder
                    .comment(" Размер шрифта названия государства на миникарте (1.0 = стандарт, 2–4 = крупнее). ")
                    .translation("config.journeyrecruits.labelFontScaleMinimap")
                    .defineInRange("labelFontScaleMinimap", 1.5, 0.5, 8.0);

            showLabelOnMinimap = builder
                    .comment(" Показывать название на миникарте в углу экрана. ")
                    .translation("config.journeyrecruits.showLabelOnMinimap")
                    .define("showLabelOnMinimap", true);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }
}
