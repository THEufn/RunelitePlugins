package com.sceneryHider;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SceneryHiderPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(SceneryHiderPlugin.class);
        RuneLite.main(args);
    }
}