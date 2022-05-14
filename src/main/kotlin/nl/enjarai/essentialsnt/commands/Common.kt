package nl.enjarai.essentialsnt.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

object Common {
    fun isPlayerPredicate(): Predicate<ServerCommandSource> {
        return Predicate { source: ServerCommandSource ->
            try {
                return@Predicate source.player != null
            } catch (e: CommandSyntaxException) {
                return@Predicate false
            }
        }
    }

    fun playerArgument(name: String): RequiredArgumentBuilder<ServerCommandSource, String> {
        return CommandManager.argument(name, StringArgumentType.word())
            .suggests { ctx: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder ->
                val remaining = builder.remaining.lowercase()
                for (player in ctx.source.server.playerNames) {
                    if (player.lowercase().contains(remaining)) {
                        builder.suggest(player)
                    }
                }
                builder.buildFuture()
            }
    }
}