package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import nl.enjarai.essentialsnt.ConfigManager;
import nl.enjarai.essentialsnt.CooldownManager;

import static net.minecraft.server.command.CommandManager.literal;
import static nl.enjarai.essentialsnt.Essentialsnt.*;

public class ManagementCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("essentialsnt")
                    .requires(Permissions.require("essentialsnt.commands.essentialsnt", 3))
                    .then(literal("reload")
                            .executes(ManagementCommands::reload)
                    )
            );
        });
    }


    private static int reload(CommandContext<ServerCommandSource> ctx) {
        CONFIG = ConfigManager.loadConfigFile(CONFIG_FILE);
        WildCommand.COOLDOWN = new CooldownManager(CONFIG.wild_cooldown);
        // TODO reload all timers and shit
        ctx.getSource().sendFeedback(TextParser.parse("Reloaded Essentialsn't config!"), true);
        return 1;
    }
}
