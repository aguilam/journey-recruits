package com.aguilam.journeyrecruits.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

public class RecruitBridgeConfigScreen {

    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.journeyrecruits.title"))
                .setSavingRunnable(RecruitBridgeConfigScreen::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory labels = builder.getOrCreateCategory(Component.translatable("config.journeyrecruits.category.labels"));

        labels.addEntry(entryBuilder.startDoubleField(
                        Component.translatable("config.journeyrecruits.nameShowMaxZoom"),
                        RecruitBridgeConfig.CLIENT.nameShowMaxZoom.get())
                .setDefaultValue(2.0)
                .setTooltip(Component.translatable("config.journeyrecruits.nameShowMaxZoom.tooltip"))
                .setSaveConsumer(RecruitBridgeConfig.CLIENT.nameShowMaxZoom::set)
                .build());

        labels.addEntry(entryBuilder.startDoubleField(
                        Component.translatable("config.journeyrecruits.labelFontScaleFullscreen"),
                        RecruitBridgeConfig.CLIENT.labelFontScaleFullscreen.get())
                .setDefaultValue(2.5)
                .setTooltip(Component.translatable("config.journeyrecruits.labelFontScaleFullscreen.tooltip"))
                .setSaveConsumer(RecruitBridgeConfig.CLIENT.labelFontScaleFullscreen::set)
                .build());

        labels.addEntry(entryBuilder.startDoubleField(
                        Component.translatable("config.journeyrecruits.labelFontScaleMinimap"),
                        RecruitBridgeConfig.CLIENT.labelFontScaleMinimap.get())
                .setDefaultValue(1.5)
                .setTooltip(Component.translatable("config.journeyrecruits.labelFontScaleMinimap.tooltip"))
                .setSaveConsumer(RecruitBridgeConfig.CLIENT.labelFontScaleMinimap::set)
                .build());

        labels.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.journeyrecruits.showLabelOnMinimap"),
                        RecruitBridgeConfig.CLIENT.showLabelOnMinimap.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.journeyrecruits.showLabelOnMinimap.tooltip"))
                .setSaveConsumer(RecruitBridgeConfig.CLIENT.showLabelOnMinimap::set)
                .build());

        return builder.build();
    }

    public static void save() {
        RecruitBridgeConfig.CLIENT.nameShowMaxZoom.save();
        RecruitBridgeConfig.CLIENT.labelFontScaleFullscreen.save();
        RecruitBridgeConfig.CLIENT.labelFontScaleMinimap.save();
        RecruitBridgeConfig.CLIENT.showLabelOnMinimap.save();
    }
}
