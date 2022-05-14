package nl.enjarai.essentialsnt.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.TextParser
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.MESSAGES_CONFIG
import nl.enjarai.essentialsnt.api.DelayedTP
import nl.enjarai.essentialsnt.types.Location

object SpawnCommands {
    const val SPAWN_PERMISSION_NODE = "essentialsnt.commands.spawn"
    const val SET_SPAWN_PERMISSION_NODE = "essentialsnt.commands.spawn.set"

    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
            dispatcher.register(
                CommandManager.literal("spawn")
                    .requires(Permissions.require(SPAWN_PERMISSION_NODE, true))
                    .executes(this::spawn)
                    .then(
                        CommandManager.literal("set")
                            .requires(Permissions.require(SET_SPAWN_PERMISSION_NODE, 3))
                            .executes(this::setSpawn)
                    )
            )
            dispatcher.register(
                CommandManager.literal("stp")
                    .requires(Permissions.require(SPAWN_PERMISSION_NODE, true))
                    .executes(this::spawn)
                    .then(
                        CommandManager.literal("set")
                            .requires(Permissions.require(SET_SPAWN_PERMISSION_NODE, 3))
                            .executes(this::setSpawn)
                    )
            )
            dispatcher.register(
                CommandManager.literal("setspawn")
                    .requires(Permissions.require(SET_SPAWN_PERMISSION_NODE, 3))
                    .executes(this::setSpawn)
            )
        })
    }


    private fun setSpawn(ctx: CommandContext<ServerCommandSource>): Int {
        val pos = ctx.source.position
        val dim = ctx.source.world
        GENERAL_CONFIG.spawn = Location(pos, dim)
        GENERAL_CONFIG.save()
        ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.spawn_set), true)
        return 1
    }

    private fun spawn(ctx: CommandContext<ServerCommandSource>): Int {
        if (GENERAL_CONFIG.spawn == null) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.spawn_not_exists), true)
            return 0
        }
        ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.spawn), true)
        val player = ctx.source.player
        DelayedTP.delayedTeleport(player, GENERAL_CONFIG.spawn!!)
        return 1
    }
}