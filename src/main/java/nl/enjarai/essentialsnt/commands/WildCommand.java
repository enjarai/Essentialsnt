package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.essentialsnt.TimerManager;
import nl.enjarai.essentialsnt.api.RandomTPAPI;

import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;

public class WildCommand {
    public static TimerManager COOLDOWN = new TimerManager(CONFIG.wild_cooldown);

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("wild")
                    .requires(Permissions.require("essentialsnt.commands.wild", true))
                    .executes(WildCommand::wild)
            );
            dispatcher.register(literal("wilderness")
                    .requires(Permissions.require("essentialsnt.commands.wild", true))
                    .executes(WildCommand::wild)
            );
            dispatcher.register(literal("rtp")
                    .requires(Permissions.require("essentialsnt.commands.wild", true))
                    .executes(WildCommand::wild)
            );
        });
    }


    private static int wild(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (!(Permissions.check(player, "essentialsnt.bypass.wildcooldown", false) ||
                COOLDOWN.check(player.getUuid()))) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.wild_cooldown), true);
            return 0;
        }

        ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.wild), true);
        try {
            RandomTPAPI.randomTeleport(player, player.getWorld(), CONFIG.wild_min_range, CONFIG.wild_max_range);
            COOLDOWN.trigger(player.getUuid());
        } catch (RandomTPAPI.NoValidLocationException e) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.wild_error), true);
        }

        return 1;
    }
}
