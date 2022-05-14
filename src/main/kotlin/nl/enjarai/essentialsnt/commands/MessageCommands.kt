package nl.enjarai.essentialsnt.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import nl.enjarai.essentialsnt.Essentialsnt.MESSAGES_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.SERVER
import nl.enjarai.essentialsnt.api.SocialSpy
import nl.enjarai.essentialsnt.api.SocialSpy.SOCIALSPY_PERMISSION_NODE
import java.util.*

object MessageCommands {
    const val MSG_PERMISSION_NODE = "essentialsnt.commands.msg"
    const val R_PERMISSION_NODE = "essentialsnt.commands.r"

    private val REPLIES = ReplyManager()

    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
            val msg = dispatcher.register(
                CommandManager.literal("msg")
                    .requires(Permissions.require(MSG_PERMISSION_NODE, true))
                    .then(
                        Common.playerArgument("player")
                            .then(
                                CommandManager.argument("message", StringArgumentType.greedyString())
                                    .executes { ctx: CommandContext<ServerCommandSource> ->
                                        msg(ctx, false)
                                    }
                            )
                    )
            )
            dispatcher.register(
                CommandManager.literal("tell")
                    .redirect(msg)
            )
            dispatcher.register(
                CommandManager.literal("w")
                    .redirect(msg)
            )
            val r = dispatcher.register(
                CommandManager.literal("r")
                    .requires(Permissions.require(R_PERMISSION_NODE, true))
                    .then(
                        CommandManager.argument("message", StringArgumentType.greedyString())
                            .executes { ctx: CommandContext<ServerCommandSource> ->
                                msg(ctx, true)
                            }
                    )
            )
            dispatcher.register(
                CommandManager.literal("reply")
                    .redirect(r)
            )
            dispatcher.register(
                CommandManager.literal("socialspy")
                    .requires(Permissions.require(SOCIALSPY_PERMISSION_NODE, 3))
                    .executes(this::socialSpy)
            )
            dispatcher.register(
                CommandManager.literal("ss")
                    .requires(Permissions.require(SOCIALSPY_PERMISSION_NODE, 3))
                    .executes(this::socialSpy)
            )
        })
    }


    private fun msg(ctx: CommandContext<ServerCommandSource>, reply: Boolean): Int {
        val player = ctx.source.player
        val message = ctx.getArgument("message", String::class.java)
        val receivePlayer = if (reply) {
            REPLIES[player]
        } else {
            val receivePlayerName = ctx.getArgument("player", String::class.java)
            SERVER.playerManager.getPlayer(receivePlayerName)
        }
        if (receivePlayer == null) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.msg_no_player), true)
            return 0
        }
        sendMsg(player, receivePlayer, message)
        return 1
    }

    private fun socialSpy(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player
        if (SocialSpy.toggle(player)) {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.socialspy_enable), false)
        } else {
            ctx.source.sendFeedback(TextParser.parse(MESSAGES_CONFIG.socialspy_disable), false)
        }
        return 1
    }


    private fun sendMsg(player: ServerPlayerEntity, receivePlayer: ServerPlayerEntity, message: String) {
        REPLIES.update(player, receivePlayer)
        val placeholders = HashMap<String, Text>()
        placeholders["sender"] = player.displayName
        placeholders["receiver"] = receivePlayer.displayName
        placeholders["message"] = TextParser.parse(message)
        player.sendMessage(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(MESSAGES_CONFIG.msg_send),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ), false
        )
        receivePlayer.sendMessage(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(MESSAGES_CONFIG.msg_receive),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ), false
        )
        SocialSpy.sendToAll(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(MESSAGES_CONFIG.msg),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ), player, receivePlayer
        )
    }

    private class ReplyManager {
        var replies = HashMap<UUID, UUID>()
        fun update(sendingPlayer: ServerPlayerEntity, receivingPlayer: ServerPlayerEntity) {
            replies[sendingPlayer.uuid] = receivingPlayer.uuid
            replies[receivingPlayer.uuid] = sendingPlayer.uuid
        }

        operator fun get(player: ServerPlayerEntity): ServerPlayerEntity? {
            val resultUuid = replies[player.uuid] ?: return null
            return SERVER.playerManager.getPlayer(resultUuid)
        }
    }
}