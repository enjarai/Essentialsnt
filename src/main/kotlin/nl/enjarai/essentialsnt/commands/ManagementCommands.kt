package nl.enjarai.essentialsnt.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.TextParser
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import nl.enjarai.essentialsnt.Essentialsnt

object ManagementCommands {
    const val MANAGEMENT_PERMISSION_NODE = "essentialsnt.commands.essentialsnt"

    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
            dispatcher.register(
                CommandManager.literal("essentialsnt")
                    .requires(
                        Permissions.require(
                            MANAGEMENT_PERMISSION_NODE,
                            3
                        )
                    )
                    .then(
                        CommandManager.literal("reload")
                            .executes(this::reload)
                    )
            )
        })
    }


    private fun reload(ctx: CommandContext<ServerCommandSource>): Int {
        Essentialsnt.reloadConfig()
        ctx.source.sendFeedback(TextParser.parse("Reloaded Essentialsn't config!"), true)
        return 1
    }
}