package nl.enjarai.essentialsnt.api

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.SERVER
import java.util.*

object SocialSpy {
    const val SOCIALSPY_PERMISSION_NODE = "essentialsnt.commands.socialspy";

    fun enable(player: ServerPlayerEntity): Boolean {
        return enable(player.uuid)
    }

    fun enable(uuid: UUID): Boolean {
        return GENERAL_CONFIG.socialspy_enabled.add(uuid)
    }

    fun disable(player: ServerPlayerEntity): Boolean {
        return disable(player.uuid)
    }

    fun disable(uuid: UUID): Boolean {
        return GENERAL_CONFIG.socialspy_enabled.remove(uuid)
    }

    fun check(player: ServerPlayerEntity): Boolean {
        return Permissions.check(player, SOCIALSPY_PERMISSION_NODE, 3) &&
                GENERAL_CONFIG.socialspy_enabled.contains(player.uuid)
    }

    fun sendToAll(message: Text?, vararg excluded: ServerPlayerEntity?) {
        for (player in SERVER.playerManager.playerList) {
            if (check(player) && !listOf(*excluded).contains(player)
            ) {
                player.sendMessage(message, false)
            }
        }
    }

    fun sendToAll(message: Text, vararg excluded: UUID) {
        for (player in SERVER.playerManager.playerList) {
            if (check(player) &&
                Permissions.check(player, SOCIALSPY_PERMISSION_NODE, 3) &&
                !listOf(*excluded).contains(player.uuid)
            ) {
                player.sendMessage(message, false)
            }
        }
    }

    /**
     *
     * @param player player to toggle
     * @return resultant state after toggle
     */
    fun toggle(player: ServerPlayerEntity): Boolean {
        return toggle(player.uuid)
    }

    /**
     *
     * @param uuid uuid of player to toggle
     * @return resultant state after toggle
     */
    fun toggle(uuid: UUID): Boolean {
        if (!enable(uuid)) {
            disable(uuid)
            return false
        }
        return true
    }
}