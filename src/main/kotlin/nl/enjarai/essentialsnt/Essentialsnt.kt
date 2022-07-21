package nl.enjarai.essentialsnt

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import nl.enjarai.essentialsnt.commands.*
import nl.enjarai.essentialsnt.commands.dayvote.DayVoteCommand
import nl.enjarai.essentialsnt.config.GeneralConfig
import nl.enjarai.essentialsnt.config.MessagesConfig
import nl.enjarai.essentialsnt.config.ModConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

object Essentialsnt : ModInitializer {
    const val MODID = "essentialsnt"
    val LOGGER: Logger = LogManager.getLogger(MODID)
    val CONFIG_DIR = FabricLoader.getInstance().configDir.resolve(MODID)

    lateinit var GENERAL_CONFIG: GeneralConfig
    lateinit var MESSAGES_CONFIG: MessagesConfig

    lateinit var SERVER: MinecraftServer

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register { SERVER = it }

        CONFIG_DIR.toFile().mkdir()
        reloadConfig()

        ManagementCommands.register()
        SpawnCommands.register()
        WarpCommands.register()
        WildCommands.register()
        MessageCommands.register()
        DayVoteCommand.register()

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                GENERAL_CONFIG.save()
                MESSAGES_CONFIG.save()
            }
        }, 60000, 60000)

        LOGGER.info("Essentials is bad lol, even i can do better")
    }

    fun reloadConfig() {
        GENERAL_CONFIG = ModConfig.loadConfigFile(GeneralConfig::class.java)
        MESSAGES_CONFIG = ModConfig.loadConfigFile(MessagesConfig::class.java)
    }
}