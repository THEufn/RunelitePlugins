package com.sceneryHider;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.callback.ClientThread;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
        name = "Scenery Hider"
)
public class SceneryHiderPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private SceneryHiderConfig config;

    private int[] hiddenObjectIds;

    @Provides
    SceneryHiderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SceneryHiderConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        // Initialize the list of hidden object IDs when the plugin starts
        updateHiddenObjectIds();
        toggleObjects();
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        int spawnedObjectId = event.getGameObject().getId();

        // Check if the object ID is in the list of IDs to hide
        for (int hiddenObjectId : hiddenObjectIds) {
            if (spawnedObjectId == hiddenObjectId) {
                // Hide the object if it matches an ID in the list
                client.getScene().removeGameObject(event.getGameObject());
                break;
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("SceneryHiderConfigGroup")) {
            // Update the list of hidden object IDs when the configuration changes
            updateHiddenObjectIds();
            toggleObjects();
        }
    }

    private void updateHiddenObjectIds() {
        String[] objectIds = config.getHiddenObjectIds().split(",");
        hiddenObjectIds = new int[objectIds.length];

        int validCount = 0; // To keep track of valid object IDs

        for (String objectId : objectIds) {
            objectId = objectId.trim(); // Remove leading/trailing spaces

            if (!objectId.isEmpty()) { // Continue if the string is not empty
                try {
                    hiddenObjectIds[validCount] = Integer.parseInt(objectId);
                    validCount++;
                } catch (NumberFormatException e) {
                    // Handle invalid input here, e.g., log an error
                    System.err.println("Invalid object ID: " + objectId);
                }
            }
        }

        // Resize the array to remove any unused slots
        hiddenObjectIds = Arrays.copyOf(hiddenObjectIds, validCount);
    }

    private void toggleObjects() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            // Setting the game state to loading forces a full map reload,
            // which will reload all the objects

            clientThread.invoke(() -> {
                if (client.getGameState() == GameState.LOGGED_IN) {
                    client.setGameState(GameState.LOADING);
                }
            });

            // Get the player's current location
            WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

            // Get the scene tile at the player's location
            Tile[][][] sceneTiles = client.getScene().getTiles();
            int sceneX = playerLocation.getX() / 128;
            int sceneY = playerLocation.getY() / 128;
            Tile tile = sceneTiles[client.getPlane()][sceneX][sceneY];

            // Iterate through the game objects on the tile
            if (tile != null) {
                GameObject[] objects = tile.getGameObjects();
                if (objects != null) {
                    for (GameObject gameObject : objects) {
                        int objectId = gameObject.getId();
                        boolean shouldHide = shouldHideObject(objectId);

                        if (shouldHide) {
                            client.getScene().removeGameObject(gameObject);
                        }
                    }
                }
            }
        }
    }

    // Check if the object ID should be hidden based on the current configuration
    private boolean shouldHideObject(int objectId) {
        for (int hiddenObjectId : hiddenObjectIds) {
            if (objectId == hiddenObjectId) {
                return true;
            }
        }
        return false;
    }
}
