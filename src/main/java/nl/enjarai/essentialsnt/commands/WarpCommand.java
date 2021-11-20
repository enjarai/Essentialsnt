package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nl.enjarai.essentialsnt.api.DelayedTPAPI;
import nl.enjarai.essentialsnt.types.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG_FILE;

public class WarpCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> warp = dispatcher.register(literal("warp")
                    .requires(Permissions.require("essentialsnt.commands.warp", true))
                    .then(argument("name", StringArgumentType.string())
                            .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                    getAccessibleWarps(ctx.getSource().getPlayer()).keySet(), builder))
                            .executes(WarpCommand::warp)
                            .then(literal("set")
                                    .requires(Permissions.require("essentialsnt.commands.warp.set", 3))
                                    .executes(ctx -> WarpCommand.modWarp(ctx, ModificationType.SET))
                            )
                            .then(literal("delete")
                                    .requires(Permissions.require("essentialsnt.commands.warp.delete", 3))
                                    .executes(ctx -> WarpCommand.modWarp(ctx, ModificationType.DELETE))
                            )
                    )
            );
            dispatcher.register(literal("wtp")
                    .redirect(warp)
            );
            dispatcher.register(literal("warps")
                    .requires(Permissions.require("essentialsnt.commands.warps", true))
                    .executes(WarpCommand::warps)
            );
            dispatcher.register(literal("listwarps")
                    .requires(Permissions.require("essentialsnt.commands.warps", true))
                    .executes(WarpCommand::warps)
            );
            dispatcher.register(literal("setwarp")
                    .requires(Permissions.require("essentialsnt.commands.warp.set", 3))
                    .then(argument("name", StringArgumentType.string())
                            .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                    getAccessibleWarps(ctx.getSource().getPlayer()).keySet(), builder))
                            .executes(ctx -> WarpCommand.modWarp(ctx, ModificationType.SET))
                    )
            );
            dispatcher.register(literal("delwarp")
                    .requires(Permissions.require("essentialsnt.commands.warp.delete", 3))
                    .then(argument("name", StringArgumentType.string())
                            .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                    getAccessibleWarps(ctx.getSource().getPlayer()).keySet(), builder))
                            .executes(ctx -> WarpCommand.modWarp(ctx, ModificationType.DELETE))
                    )
            );
        });
    }


    private static int modWarp(CommandContext<ServerCommandSource> ctx, ModificationType modType) {
        String name = ctx.getArgument("name", String.class);

        Vec3d pos = ctx.getSource().getPosition();
        ServerWorld dim = ctx.getSource().getWorld();

        String message;
        switch (modType) {
            case SET -> {
                CONFIG.warps.put(name, new Location(pos, dim));
                message = CONFIG.messages.warp_set;
            }
            case DELETE -> {
                if (!CONFIG.warps.containsKey(name)) {
                    ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.warp_not_exists), true);
                    return 0;
                }

                CONFIG.warps.remove(name);
                message = CONFIG.messages.warp_delete;
            }
            default -> {
                return 0;
            }
        }
        CONFIG.saveConfigFile(CONFIG_FILE);


        HashMap<String, Text> placeholders = new HashMap<>();

        placeholders.put("name", new LiteralText(name));

        ctx.getSource().sendFeedback(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(message),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), true);
        return 1;
    }

    private static int warp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String name = ctx.getArgument("name", String.class);

        if (!(CONFIG.warps.containsKey(name) && Permissions.check(player, "essentialsnt.warps." + name, true))) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.warp_not_exists), true);
            return 0;
        }


        HashMap<String, Text> placeholders = new HashMap<>();

        placeholders.put("name", new LiteralText(name));

        ctx.getSource().sendFeedback(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(CONFIG.messages.warp),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), true);


        DelayedTPAPI.delayedTeleport(player, CONFIG.warps.get(name), CONFIG.teleport_delay);

        return 1;
    }

    private static int warps(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        HashMap<String, Location> accessibleWarps = getAccessibleWarps(player);

        if (accessibleWarps.isEmpty()) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.warps_none), true);
        } else {
            Set<String> warpNames = accessibleWarps.keySet();

            StringBuilder formatted = new StringBuilder("  <dark_aqua><yellow>");
            String separator;
            for (int i = warpNames.size() - 1; i >= 0; i--) {
                if (i == 0) {
                    separator = "</yellow>";
                } else if (i == 1) {
                    separator = "</yellow> and <yellow>";
                } else {
                    separator = "</yellow>, <yellow>";
                }

                formatted.append(warpNames.toArray()[i]).append(separator);
            }

            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.warps), true);
            ctx.getSource().sendFeedback(TextParser.parse(formatted.toString()), true);
        }

        return 1;
    }


    private static HashMap<String, Location> getAccessibleWarps(ServerPlayerEntity player) {
        HashMap<String, Location> result = new HashMap<>();
        for (Map.Entry<String, Location> warp : CONFIG.warps.entrySet()) {
            if (Permissions.check(player, "essentialsnt.warps." + warp.getKey(), true)) {
                result.put(warp.getKey(), warp.getValue());
            }
        }
        return result;
    }
}
