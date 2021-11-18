package nl.enjarai.essentialsnt;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import nl.enjarai.essentialsnt.commands.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Essentialsnt implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Essentialsn't");
    public static MinecraftServer SERVER;

    public static final File CONFIG_FILE = new File("config/essentialsnt.json");
    public static ConfigManager CONFIG = ConfigManager.loadConfigFile(CONFIG_FILE);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(Essentialsnt::onServerStarting);

        Commands.register();

        LOGGER.info("Essentials bad lol");
    }

    private static void onServerStarting(MinecraftServer server) {
        SERVER = server;
    }
}
