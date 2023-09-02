package com.sceneryHider;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("SceneryHiderConfigGroup")
public interface SceneryHiderConfig extends Config {
    @ConfigItem(
            keyName = "hiddenObjectIds",
            name = "Hidden Object IDs",
            description = "Comma-separated list of object IDs to hide",
            position = 0
    )
    default String getHiddenObjectIds() {
        return "";
    }
}
