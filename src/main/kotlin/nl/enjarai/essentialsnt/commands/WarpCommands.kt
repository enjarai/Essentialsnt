package nl.enjarai.essentialsnt.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.MESSAGES_CONFIG
import nl.enjarai.essentialsnt.api.DelayedTP
import nl.enjarai.essentialsnt.types.ConfigLocation

object WarpCommands {
    const val WARP_PERMISSION_NODE = "essentialsnt.commands.warp"
    const val SET_WARP_PERMISSION_NODE = "essentialsnt.commands.warp.set"
    const val DELETE_WARP_PERMISSION_NODE = "essentialsnt.commands.warp.delete"
    const val LIST_WARPS_PERMISSION_NODE = "essentialsnt.commands.warps"

    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, _: Boolean ->
            val warp = dispatcher.register(
                CommandManager.literal("warp")
                    .requires(Permissions.require(WARP_PERMISSION_NODE, true))
                    .then(
                        CommandManager.argument("name", StringArgumentType.string())
                            .suggests { ctx: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder? ->
                                CommandSource.suggestMatching(
                                    getAccessibleWarps(ctx.source.player).keys, builder
                                )
                            }
                            .executes(this::warp)
                            .then(
                                CommandManager.literal("set")
                                    .requires(
                                        Permissions.require(SET_WARP_PERMISSION_NODE, 3)
                                    )
                                    .executes { ctx: CommandContext<ServerCommandSource> ->
                                        modWarp(ctx, ModificationType.SET)
                                    }
                            )
                            .then(
                                CommandManager.literal("delete")
                                    .requires(
                                        Permissions.require(DELETE_WARP_PERMISSION_NODE, 3)
                                    )
                                    .executes { ctx: CommandContext<ServerCommandSource> ->
                                        modWarp(ctx, ModificationType.DELETE)
                                    }
                            )
                    )
            )
            dispatcher.register(
                CommandManager.literal("wtp")
                    .redirect(warp)
            )
            dispatcher.register(
                CommandManager.literal("warps")
                    .requires(Permissions.require(LIST_WARPS_PERMISSION_NODE, true))
                    .executes(this::warps)
            )
            dispatcher.register(
                CommandManager.literal("listwarps")
                    .requires(Permissions.require(LIST_WARPS_PERMISSION_NODE, true))
                    .executes(this::warps)
            )
            dispatcher.register(
                CommandManager.literal("setwarp")
                    .requires(Permissions.require(SET_WARP_PERMISSION_NODE, 3))
                    .then(
                        CommandManager.argument("name", StringArgumentType.string())
                            .suggests { ctx: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder? ->
                                CommandSource.suggestMatching(
                                    getAccessibleWarps(ctx.source.player).keys, builder
                                )
                            }
                            .executes { ctx: CommandContext<ServerCommandSource> ->
                                modWarp(ctx, ModificationType.SET)
                            }
                    )
            )
            dispatcher.register(
                CommandManager.literal("delwarp")
                    .requires(
                        Permissions.require(
                            DELETE_WARP_PERMISSION_NODE,
                            3
                        )
                    )
                    .then(
                        CommandManager.argument("name", StringArgumentType.string())
                            .suggests { ctx: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder? ->
                                CommandSource.suggestMatching(
                                    getAccessibleWarps(ctx.source.player).keys, builder
                                )
                            }
                            .executes { ctx: CommandContext<ServerCommandSource> ->
                                modWarp(ctx, ModificationType.DELETE)
                            }
                    )
            )
        })
    }


    private fun modWarp(ctx: CommandContext<ServerCommandSource>, modType: ModificationType): Int {
        val name = ctx.getArgument("name", String::class.java)
        val pos = ctx.source.position
        val dim = ctx.source.world
        val message = when (modType) {
            ModificationType.SET -> {
                GENERAL_CONFIG.warps.put(name, ConfigLocation(pos, dim))
                MESSAGES_CONFIG.warp_set
            }
            ModificationType.DELETE -> {
                if (!GENERAL_CONFIG.warps.containsKey(name)) {
                    ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.warp_not_exists), true)
                    return 0
                }
                GENERAL_CONFIG.warps.remove(name)
                MESSAGES_CONFIG.warp_delete
            }
        }
        GENERAL_CONFIG.save()
        val placeholders = HashMap<String, Text>()
        placeholders["name"] = LiteralText(name)
        ctx.source.sendFeedback(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(message),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ), true
        )
        return 1
    }

    private fun warp(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player
        val name = ctx.getArgument("name", String::class.java)
        val warp = GENERAL_CONFIG.warps[name];
        if (!(warp != null && Permissions.check(player, "essentialsnt.warps.$name", true))) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.warp_not_exists), true)
            return 0
        }
        val placeholders = HashMap<String, Text>()
        placeholders["name"] = LiteralText(name)
        ctx.source.sendFeedback(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(MESSAGES_CONFIG.warp),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ), true
        )
        DelayedTP.delayedTeleport(player, warp, GENERAL_CONFIG.teleport_delay)
        return 1
    }

    private fun warps(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player
        val accessibleWarps: HashMap<String, ConfigLocation> = getAccessibleWarps(player)
        if (accessibleWarps.isEmpty()) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.warps_none), true)
        } else {
            val warpNames: Set<String> = accessibleWarps.keys
            val formatted = StringBuilder("  <dark_aqua><yellow>")
            var separator: String
            for (i in warpNames.indices.reversed()) {
                separator = if (i == 0) {
                    "</yellow>"
                } else if (i == 1) {
                    "</yellow> and <yellow>"
                } else {
                    "</yellow>, <yellow>"
                }
                formatted.append(warpNames.toTypedArray()[i]).append(separator)
            }
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.warps), true)
            ctx.source.sendFeedback(TextParser.parse(formatted.toString()), true)
        }
        return 1
    }


    private fun getAccessibleWarps(player: ServerPlayerEntity): HashMap<String, ConfigLocation> {
        val result: HashMap<String, ConfigLocation> = HashMap()
        for ((key, value) in GENERAL_CONFIG.warps.entries) {
            if (Permissions.check(player, "essentialsnt.warps.$key", true)) {
                result[key] = value
            }
        }
        return result
    }
}