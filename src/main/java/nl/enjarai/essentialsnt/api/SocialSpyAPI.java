package nl.enjarai.essentialsnt.api;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import nl.enjarai.essentialsnt.Essentialsnt;

import java.util.List;
import java.util.UUID;

import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;

public class SocialSpyAPI {
    public static boolean enable(ServerPlayerEntity player) {
        return enable(player.getUuid());
    }
    public static boolean enable(UUID uuid) {
        return CONFIG.socialspy_enabled.add(uuid.toString());
    }

    public static boolean disable(ServerPlayerEntity player) {
        return disable(player.getUuid());
    }
    public static boolean disable(UUID uuid) {
        return CONFIG.socialspy_enabled.remove(uuid.toString());
    }

    public static boolean check(ServerPlayerEntity player) {
        return check(player.getUuid());
    }
    public static boolean check(UUID uuid) {
        return CONFIG.socialspy_enabled.contains(uuid.toString());
    }

    public static void sendToAll(Text message, ServerPlayerEntity... excluded) {
        for (ServerPlayerEntity player : Essentialsnt.SERVER.getPlayerManager().getPlayerList()) {
            if (
                    check(player) &&
                    Permissions.check(player, "essentialsnt.commands.socialspy", 3) &&
                    !List.of(excluded).contains(player)
            ) {
                player.sendMessage(message, false);
            }
        }
    }

    public static void sendToAll(Text message, UUID... excluded) {
        for (ServerPlayerEntity player : Essentialsnt.SERVER.getPlayerManager().getPlayerList()) {
            if (
                    check(player) &&
                    Permissions.check(player, "essentialsnt.commands.socialspy", 3) &&
                    !List.of(excluded).contains(player.getUuid())
            ) {
                player.sendMessage(message, false);
            }
        }
    }

    /**
     *
     * @param player player to toggle
     * @return resultant state after toggle
     */
    public static boolean toggle(ServerPlayerEntity player) {
        return toggle(player.getUuid());
    }
    /**
     *
     * @param uuid uuid of player to toggle
     * @return resultant state after toggle
     */
    public static boolean toggle(UUID uuid) {
        if (!enable(uuid)) {
            disable(uuid);
            return false;
        }
        return true;
    }
}
