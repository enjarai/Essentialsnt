package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import nl.enjarai.essentialsnt.api.SocialSpyAPI;

import java.util.HashMap;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;
import static nl.enjarai.essentialsnt.Essentialsnt.SERVER;

public class MsgCommand {
    private static final ReplyManager REPLIES = new ReplyManager();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> msg = dispatcher.register(literal("msg")
                    .requires(Permissions.require("essentialsnt.commands.msg", true))
                    .then(Common.playerArgument("player")
                            .then(argument("message", StringArgumentType.greedyString())
                                    .executes(ctx -> msg(ctx, false))
                            )
                    )
            );
            dispatcher.register(literal("tell")
                    .redirect(msg)
            );
            dispatcher.register(literal("w")
                    .redirect(msg)
            );

            LiteralCommandNode<ServerCommandSource> r = dispatcher.register(literal("r")
                    .requires(Permissions.require("essentialsnt.commands.r", true))
                    .then(argument("message", StringArgumentType.greedyString())
                            .executes(ctx -> msg(ctx, true))
                    )
            );
            dispatcher.register(literal("reply")
                    .redirect(r)
            );

            dispatcher.register(literal("socialspy")
                    .requires(Permissions.require("essentialsnt.commands.socialspy", 3))
                    .executes(MsgCommand::socialSpy)
            );
            dispatcher.register(literal("ss")
                    .requires(Permissions.require("essentialsnt.commands.socialspy", 3))
                    .executes(MsgCommand::socialSpy)
            );
        });
    }


    private static int msg(CommandContext<ServerCommandSource> ctx, boolean reply) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String message = ctx.getArgument("message", String.class);

        ServerPlayerEntity receivePlayer;
        if (reply) {
            receivePlayer = REPLIES.get(player);
        } else {
            String receivePlayerName = ctx.getArgument("player", String.class);
            receivePlayer = SERVER.getPlayerManager().getPlayer(receivePlayerName);
        }

        if (receivePlayer == null) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.msg_no_player), true);
            return 0;
        }

        sendMsg(player, receivePlayer, message);

        return 1;
    }

    private static int socialSpy(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (SocialSpyAPI.toggle(player)) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.socialspy_enable), true);
        } else {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.socialspy_disable), true);
        }

        return 1;
    }


    private static void sendMsg(ServerPlayerEntity player, ServerPlayerEntity receivePlayer, String message) {
        REPLIES.update(player, receivePlayer);

        HashMap<String, Text> placeholders = new HashMap<>();

        placeholders.put("sender", player.getDisplayName());
        placeholders.put("receiver", receivePlayer.getDisplayName());
        placeholders.put("message", TextParser.parse(message));

        player.sendMessage(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(CONFIG.messages.msg_send),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), false);
        receivePlayer.sendMessage(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(CONFIG.messages.msg_receive),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), false);
        SocialSpyAPI.sendToAll(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(CONFIG.messages.msg),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), player, receivePlayer);
    }

    private static class ReplyManager {
        public HashMap<UUID, UUID> replies = new HashMap<>();

        public void update(ServerPlayerEntity sendingPlayer, ServerPlayerEntity receivingPlayer) {
            replies.put(sendingPlayer.getUuid(), receivingPlayer.getUuid());
            replies.put(receivingPlayer.getUuid(), sendingPlayer.getUuid());
        }

        public ServerPlayerEntity get(ServerPlayerEntity player) {
            UUID resultUuid = replies.get(player.getUuid());
            if (resultUuid == null) { return null; }
            return SERVER.getPlayerManager().getPlayer(resultUuid);
        }
    }
}
