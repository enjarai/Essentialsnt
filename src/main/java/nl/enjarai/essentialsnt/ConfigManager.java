package nl.enjarai.essentialsnt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.enjarai.essentialsnt.types.Location;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ConfigManager {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting() // Makes the json use new lines instead of being a "one-liner"
            .serializeNulls() // Makes fields with `null` value to be written as well.
            .disableHtmlEscaping() // We'll be able to use custom chars without them being saved differently
            .create();

    // Config values
    public int teleport_delay = 3;
    public int wild_cooldown = 600;
    public int wild_min_range = 200;
    public int wild_max_range = 2000;

    public Location spawn = null;

    public HashMap<String, Location> warps = new HashMap<>();

    public HashSet<String> socialspy_enabled = new HashSet<>();

    public Messages messages = new Messages();
    public static class Messages {
        public String spawn = "<dark_aqua>Teleporting to spawn.";
        public String spawn_set = "<dark_aqua>Spawn set to current location.";

        public String warp = "<dark_aqua>Teleporting to <yellow>${name}</yellow>.";
        public String warp_set = "<dark_aqua>Warp <yellow>${name}</yellow> set to current location.";
        public String warp_delete = "<dark_aqua>Warp <yellow>${name}</yellow> deleted.";
        public String warp_not_exists = "<red>That warp doesn't exist.";
        public String warps = "<dark_aqua>You have access to the following warps:";
        public String warps_none = "<dark_aqua>You don't have access to any warps.";

        public String wild = "<dark_aqua>Teleporting you to a random location.";
        public String wild_cooldown = "<red>You cant use /wild right now, please wait 10 minutes.";
        public String wild_error = "<red>Error, can't find a valid location to teleport to.";

        public String msg = "${sender} <dark_aqua>-></dark_aqua> ${receiver} <dark_gray>»</dark_gray> <white>${message}";
        public String msg_send = "<dark_aqua>you -></dark_aqua> ${receiver} <dark_gray>»</dark_gray> <white>${message}";
        public String msg_receive = "${sender} <dark_aqua>-> you</dark_aqua> <dark_gray>»</dark_gray> <white>${message}";
        public String msg_no_player = "<red>Can't find that player.";

        public String socialspy_enable = "<dark_aqua>Socialspy is now <green>enabled</green>.";
        public String socialspy_disable = "<dark_aqua>Socialspy is now <red>disabled</red>.";

        public String teleporting = "<dark_aqua>Teleporting...";
        public String teleporting_wait = "<dark_aqua>Teleportation commencing in <yellow>${duration}</yellow> seconds. Please wait...";
        public String moved = "<red>You moved, teleportation cancelled.";
    }

    // Reading and saving

    /**
     * Loads config file.
     *
     * @param file file to load the config file from.
     * @return ConfigManager object
     */
    public static ConfigManager loadConfigFile(File file) {
        ConfigManager config = null;

        if (file.exists()) {
            // An existing config is present, we should use its values
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                // Parses the config file and puts the values into config object
                config = gson.fromJson(fileReader, ConfigManager.class);
            } catch (IOException e) {
                throw new RuntimeException("[Essentialsn't] Problem occurred when trying to load config: ", e);
            }
        }
        // gson.fromJson() can return null if file is empty
        if (config == null) {
            config = new ConfigManager();
        }

        // Saves the file in order to write new fields if they were added
        config.saveConfigFile(file);
        return config;
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    public void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
