package nl.enjarai.essentialsnt.api

import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.MESSAGES_CONFIG
import nl.enjarai.essentialsnt.types.ConfigLocation
import java.util.*

object DelayedTP {
    const val DELAY_BYPASS_PERMISSION_NODE = "essentialsnt.bypass.tpdelay"

    fun delayedTeleport(player: ServerPlayerEntity, destination: ConfigLocation) {
        delayedTeleport(player, destination, object : TPCallback {})
    }

    fun delayedTeleport(player: ServerPlayerEntity, destination: ConfigLocation, callback: TPCallback) {
        delayedTeleport(player, destination, GENERAL_CONFIG.teleport_delay, callback)
    }

    fun delayedTeleport(player: ServerPlayerEntity, destination: ConfigLocation, seconds: Int) {
        delayedTeleport(
            player,
            destination,
            seconds,
            object : TPCallback {}
        )
    }

    fun delayedTeleport(player: ServerPlayerEntity, destination: ConfigLocation, seconds: Int, callback: TPCallback) {
        val oldPos = player.pos
        if (Permissions.check(player, DELAY_BYPASS_PERMISSION_NODE, false)) {
            destination.teleportHere(player)
            callback.success(player, destination)
            return
        }
        val placeholders = HashMap<String, Text>()
        placeholders["duration"] = LiteralText(seconds.toString())
        player.sendMessage(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(MESSAGES_CONFIG.teleporting_wait),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ), true
        )
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (oldPos.isInRange(player.pos, 0.1)) {
                    player.sendMessage(TextParser.parse(MESSAGES_CONFIG.teleporting), true)
                    destination.teleportHere(player)
                    callback.success(player, destination)
                } else {
                    player.sendMessage(TextParser.parse(MESSAGES_CONFIG.moved), true)
                    callback.failure(player, destination)
                }
            }
        }, seconds * 1000L)
    }

    interface TPCallback {
        fun success(player: ServerPlayerEntity, destination: ConfigLocation) {}
        fun failure(player: ServerPlayerEntity, destination: ConfigLocation) {}
    }
}