package nl.enjarai.essentialsnt;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import nl.enjarai.essentialsnt.commands.ManagementCommands;
import nl.enjarai.essentialsnt.commands.SpawnCommand;
import nl.enjarai.essentialsnt.commands.WarpCommand;
import nl.enjarai.essentialsnt.commands.WildCommand;
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

        ManagementCommands.register();
        SpawnCommand.register();
        WarpCommand.register();
        WildCommand.register();

        LOGGER.info("Essentials bad lol, even i can do better");
    }

    private static void onServerStarting(MinecraftServer server) {
        SERVER = server;
    }
}
