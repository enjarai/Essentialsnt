package nl.enjarai.multichats;

import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import nl.enjarai.essentialsnt.api.SocialSpyAPI;
import nl.enjarai.multichats.types.Group;
import nl.enjarai.multichats.types.GroupPermissionLevel;

import java.util.HashMap;
import java.util.UUID;

public class Helpers {
    public static void sendToChat(Group group, ServerPlayerEntity sendingPlayer, String message) {
        Text chatFormat = TextParser.parse(group.getFormat());
        HashMap<String, Text> placeholders = new HashMap<>();

        placeholders.put("player", sendingPlayer.getDisplayName());
        placeholders.put("group", group.displayNameShort);
        placeholders.put("message", new LiteralText(message));
        placeholders.put("prefix", new LiteralText(group.prefix));

        Text output = PlaceholderAPI.parsePredefinedText(
                chatFormat,
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        );
        MultiChats.LOGGER.info(output.getString());

        MultiChats.SERVER.getPlayerManager().getPlayerList().forEach(player -> {
            if (group.checkAccess(player.getUuid()) || SocialSpyAPI.check(player)) {
                player.sendMessage(output, false);
            }
        });
    }

    public static GroupPermissionLevel getPermissionLevelFromInt(int permissionInt) {
        return switch (permissionInt) {
            case 0 -> GroupPermissionLevel.MEMBER;
            case 1 -> GroupPermissionLevel.MANAGER;
            case 2 -> GroupPermissionLevel.OWNER;
            default -> null;
        };
    }

    public static boolean areAllEqual(int checkValue, int... otherValues) {
        for (int value : otherValues) {
            if (value != checkValue) {
                return false;
            }
        }
        return true;
    }

    public static void updatePlayerListEntry(UUID uuid) {
        ServerPlayerEntity player = MultiChats.SERVER.getPlayerManager().getPlayer(uuid);
        if (player != null) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
            MultiChats.SERVER.getPlayerManager().sendToAll(packet);
        }
    }

    public static void updatePlayerListEntry(UUID... uuids) {
        for (UUID uuid : uuids) {
            updatePlayerListEntry(uuid);
        }
    }
}
