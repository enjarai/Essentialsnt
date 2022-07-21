package nl.enjarai.essentialsnt.commands.dayvote

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import java.util.*
import java.util.concurrent.CompletableFuture

class DayVoteArgumentType : ArgumentType<DayVoteOption> {
    companion object {
        @JvmStatic
        fun dayVote(): DayVoteArgumentType = DayVoteArgumentType()
    }

    override fun parse(reader: StringReader): DayVoteOption {
        val option = reader.readUnquotedString()
        return try {
            VotingOption.valueOf(option.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            ResultOption.valueOf(option.uppercase(Locale.getDefault()))
        }
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val options = if (!DayVoteCommand.voting) {
            ResultOption.values().map { it.name }
        } else {
            VotingOption.values().map { it.name }
        }
        return CommandSource.suggestMatching(options, builder)
    }

    override fun getExamples(): Collection<String> {
        return ResultOption.values().map { it.name }
    }
}