package nl.enjarai.essentialsnt.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

public class Predicates {
    public static Predicate<ServerCommandSource> isPlayerPredicate() {
        return source -> {
            try {
                return source.getPlayer() != null;
            } catch (CommandSyntaxException e) {
                return false;
            }
        };
    }
}
