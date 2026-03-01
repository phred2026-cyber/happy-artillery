package happy.artillery.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * Manages loading and saving the Happy Artillery config file.
 * Config is stored at: config/happy-artillery.json
 */
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("happy-artillery");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HappyArtilleryConfig instance;

    public static HappyArtilleryConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("happy-artillery.json");
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                instance = GSON.fromJson(reader, HappyArtilleryConfig.class);
                if (instance == null) instance = new HappyArtilleryConfig();
                LOGGER.info("[HappyArtillery] Config loaded from {}", configPath);
            } catch (IOException e) {
                LOGGER.warn("[HappyArtillery] Failed to read config, using defaults: {}", e.getMessage());
                instance = new HappyArtilleryConfig();
            }
        } else {
            instance = new HappyArtilleryConfig();
            LOGGER.info("[HappyArtillery] Config not found, creating default config at {}", configPath);
        }
        save(); // always write to disk so the file exists with all keys
    }

    public static void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("happy-artillery.json");
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            LOGGER.error("[HappyArtillery] Failed to save config: {}", e.getMessage());
        }
    }
}
