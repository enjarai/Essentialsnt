package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import nl.enjarai.essentialsnt.api.DelayedTPAPI;
import nl.enjarai.essentialsnt.types.Location;

import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG_FILE;

public class SpawnCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("spawn")
                    .requires(Permissions.require("essentialsnt.commands.spawn", true))
                    .executes(SpawnCommand::spawn)
                    .then(literal("set")
                            .requires(Permissions.require("essentialsnt.commands.spawn.set", 3))
                            .executes(SpawnCommand::setSpawn)
                    )
            );
            dispatcher.register(literal("stp")
                    .requires(Permissions.require("essentialsnt.commands.spawn", true))
                    .executes(SpawnCommand::spawn)
                    .then(literal("set")
                            .requires(Permissions.require("essentialsnt.commands.spawn.set", 3))
                            .executes(SpawnCommand::setSpawn)
                    )
            );
            dispatcher.register(literal("setspawn")
                    .requires(Permissions.require("essentialsnt.commands.spawn.set", 3))
                    .executes(SpawnCommand::setSpawn)
            );
        });
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
        DelayedTPAPI.delayedTeleport(player, CONFIG.spawn, CONFIG.teleport_delay);

        return 1;
    }
}
