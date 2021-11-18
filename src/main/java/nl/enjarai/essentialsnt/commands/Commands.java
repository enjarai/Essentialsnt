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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nl.enjarai.essentialsnt.ConfigManager;
import nl.enjarai.essentialsnt.Helpers;
import nl.enjarai.essentialsnt.TeleportManager;
import nl.enjarai.essentialsnt.types.Location;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.*;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("essentialsnt")
                    .requires(Permissions.require("essentialsnt.commands.essentialsnt", 3))
                    .then(literal("reload")
                            .executes(Commands::reloadConfig)
                    )
            );

            dispatcher.register(literal("spawn")
                    .requires(Permissions.require("essentialsnt.commands.spawn", true))
                    .requires(Predicates.isPlayerPredicate())
                    .executes(Commands::spawn)
                    .then(literal("set")
                            .requires(Permissions.require("essentialsnt.commands.spawn.set", 3))
                            .executes(Commands::setSpawn)
                    )
            );
            dispatcher.register(literal("stp")
                    .requires(Permissions.require("essentialsnt.commands.spawn", true))
                    .requires(Predicates.isPlayerPredicate())
                    .executes(Commands::spawn)
            );
            dispatcher.register(literal("setspawn")
                    .requires(Permissions.require("essentialsnt.commands.spawn.set", 3))
                    .executes(Commands::setSpawn)
            );

            LiteralCommandNode<ServerCommandSource> warp = dispatcher.register(literal("warp")
                    .requires(Permissions.require("essentialsnt.commands.warp", true))
                    .requires(Predicates.isPlayerPredicate())
                    .then(argument("name", StringArgumentType.string())
                            .executes(Commands::warp)
                            .then(literal("set")
                                    .requires(Permissions.require("essentialsnt.commands.warp.set", 3))
                                    .executes(ctx -> modWarp(ctx, ModificationType.SET))
                            )
                            .then(literal("delete")
                                    .requires(Permissions.require("essentialsnt.commands.warp.delete", 3))
                                    .executes(ctx -> modWarp(ctx, ModificationType.DELETE))
                            )
                    )
            );
            dispatcher.register(literal("wtp")
                    .redirect(warp)
            );
            dispatcher.register(literal("warps")
                    .requires(Permissions.require("essentialsnt.commands.warps", true))
                    .requires(Predicates.isPlayerPredicate())
                    .executes(Commands::warps)
            );
            dispatcher.register(literal("listwarps")
                    .requires(Permissions.require("essentialsnt.commands.warps", true))
                    .requires(Predicates.isPlayerPredicate())
                    .executes(Commands::warps)
            );
            dispatcher.register(literal("setwarp")
                    .requires(Permissions.require("essentialsnt.commands.warp.set", 3))
                    .then(argument("name", StringArgumentType.string())
                            .executes(ctx -> modWarp(ctx, ModificationType.SET))
                    )
            );
            dispatcher.register(literal("delwarp")
                    .requires(Permissions.require("essentialsnt.commands.warp.delete", 3))
                    .then(argument("name", StringArgumentType.string())
                            .executes(ctx -> modWarp(ctx, ModificationType.DELETE))
                    )
            );
        });
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> ctx) {
        CONFIG = ConfigManager.loadConfigFile(CONFIG_FILE);
        // TODO reload all timers and shit
        ctx.getSource().sendFeedback(TextParser.parse("Reloaded Essentialsn't config!"), true);
        return 1;
    }

    private static int setSpawn(CommandContext<ServerCommandSource> ctx) {
        Vec3d pos = ctx.getSource().getPosition();
        ServerWorld dim = ctx.getSource().getWorld();

        CONFIG.spawn = new Location(pos, dim);
        CONFIG.saveConfigFile(CONFIG_FILE);

        ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.spawn_set), true);
        return 1;
    }

    private static int spawn(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.spawn), true);
        TeleportManager.delayedTeleport(player, CONFIG.spawn, CONFIG.teleport_delay);

        return 1;
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
        }


        HashMap<String, Text> placeholders = new HashMap<>();

        placeholders.put("name", new LiteralText(name));

        ctx.getSource().sendFeedback(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(CONFIG.messages.warp),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), true);


        TeleportManager.delayedTeleport(player, CONFIG.warps.get(name), CONFIG.teleport_delay);

        return 1;
    }

    private static int warps(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        HashMap<String, Location> accessibleWarps = Helpers.getAccessibleWarps(player);

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
}
