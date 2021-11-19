package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.essentialsnt.CooldownManager;
import nl.enjarai.essentialsnt.api.DelayedTPAPI;
import nl.enjarai.essentialsnt.api.RandomTPAPI;

import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;

public class WildCommand {
    public static CooldownManager COOLDOWN = new CooldownManager(CONFIG.wild_cooldown);

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("wild")
                    .requires(Permissions.require("essentialsnt.commands.wild", 3))
                    .executes(WildCommand::wild)
            );
        });
    }


    private static int wild(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (!(Permissions.check(player, "essentialsnt.bypass.wildcooldown", false)
                || COOLDOWN.checkAndTouch(player.getUuid()))) {
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.wild_cooldown), true);
            return 0;
        }

        ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.wild), true);
        try {
            RandomTPAPI.randomTeleport(player, player.getServerWorld(), CONFIG.wild_min_range, CONFIG.wild_max_range);
        } catch (RandomTPAPI.NoValidLocationException e) {
            COOLDOWN.resetCooldown(player.getUuid());
            ctx.getSource().sendFeedback(TextParser.parse(CONFIG.messages.wild_error), true);
        }

        return 1;
    }
}
