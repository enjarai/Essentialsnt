package nl.enjarai.multichats.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import nl.enjarai.multichats.MultiChats;
import nl.enjarai.multichats.types.GroupPermissionLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static nl.enjarai.multichats.MultiChats.CONFIG;

public class CommandHelpers {
    public static ServerPlayerEntity checkPlayer(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player;

        try {
            player = ctx.getSource().getPlayer();
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFeedback(TextParser.parse(MultiChats.CONFIG.messages.noPlayerError), true);
            return null;
        }

        return player;
    }

    public static void sendMemberList(CommandContext<ServerCommandSource> ctx, HashMap<UUID, GroupPermissionLevel> members, String format) {
        for (Map.Entry<UUID, GroupPermissionLevel> set : members.entrySet()) {
            GameProfile player = MultiChats.SERVER.getUserCache().getByUuid(set.getKey()).orElse(null);
            if (player == null) { continue; }

            HashMap<String, Text> p2 = new HashMap<>();

            p2.put("permissionLevel", TextParser.parse(set.getValue().displayName));
            p2.put("player", new LiteralText(player.getName()));

            ctx.getSource().sendFeedback(PlaceholderAPI.parsePredefinedText(
                    TextParser.parse(format),
                    PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                    p2
            ), false);
        }
    }
}
