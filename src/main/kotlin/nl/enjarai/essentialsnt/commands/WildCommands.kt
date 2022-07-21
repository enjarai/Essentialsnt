package nl.enjarai.essentialsnt.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.TextParser
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.MESSAGES_CONFIG
import nl.enjarai.essentialsnt.api.DelayedTP
import nl.enjarai.essentialsnt.api.RandomTP
import nl.enjarai.essentialsnt.types.ConfigLocation
import nl.enjarai.essentialsnt.util.Cooldown

object WildCommands {
    const val WILD_PERMISSION_NODE = "essentialsnt.commands.wild"
    const val WILD_COOLDOWN_BYPASS_PERMISSION_NODE = "essentialsnt.bypass.wildcooldown"

    var COOLDOWN = Cooldown { GENERAL_CONFIG.wild_cooldown }

    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
            dispatcher.register(
                CommandManager.literal("wild")
                    .requires(Permissions.require(WILD_PERMISSION_NODE, true))
                    .executes(this::wild)
            )
            dispatcher.register(
                CommandManager.literal("wilderness")
                    .requires(Permissions.require(WILD_PERMISSION_NODE, true))
                    .executes(this::wild)
            )
            dispatcher.register(
                CommandManager.literal("rtp")
                    .requires(Permissions.require(WILD_PERMISSION_NODE, true))
                    .executes(this::wild)
            )
        })
    }


    private fun wild(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player
        if (!(Permissions.check(player, WILD_COOLDOWN_BYPASS_PERMISSION_NODE, false) ||
                    COOLDOWN.check(player.uuid))
        ) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.wild_cooldown), true)
            return 0
        }
        ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.wild), true)
        try {
            RandomTP.randomTeleport(
                player,
                player.getWorld(),
                GENERAL_CONFIG.wild_min_range,
                GENERAL_CONFIG.wild_max_range,
                object : DelayedTP.TPCallback {
                    override fun failure(player: ServerPlayerEntity, destination: ConfigLocation) {
                        COOLDOWN.resetCooldown(player.uuid)
                    }
                }
            )
            COOLDOWN.trigger(player.uuid)
        } catch (e: RandomTP.NoValidLocationException) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.wild_error), true)
        }
        return 1
    }
}