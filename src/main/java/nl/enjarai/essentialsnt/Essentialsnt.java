package nl.enjarai.essentialsnt;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import nl.enjarai.essentialsnt.commands.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Essentialsnt implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Essentialsn't");
    public static MinecraftServer SERVER;

    public static String VERSION = FabricLoader.getInstance().getModContainer("essentialsnt").get().getMetadata().getVersion().getFriendlyString();

    public static final File CONFIG_FILE = new File("config/essentialsnt.json");
    public static ConfigManager CONFIG = ConfigManager.loadConfigFile(CONFIG_FILE);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(Essentialsnt::onServerStarting);

        ManagementCommands.register();
        SpawnCommand.register();
        WarpCommand.register();
        WildCommand.register();
        MsgCommand.register();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CONFIG.saveConfigFile(CONFIG_FILE);
            }
        }, 60000, 60000);

        LOGGER.info("Essentials bad lol, even i can do better");
    }

    private static void onServerStarting(MinecraftServer server) {
        SERVER = server;
    }
}
