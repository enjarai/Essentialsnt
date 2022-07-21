package nl.enjarai.essentialsnt.commands.dayvote

import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import net.minecraft.network.MessageType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Util
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.MESSAGES_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.SERVER
import nl.enjarai.essentialsnt.util.Vote
import java.util.*
import kotlin.collections.HashMap

interface DayVoteOption {
    fun execute(ctx: CommandContext<ServerCommandSource>)

    fun getDisplayName(): Text
}

enum class ResultOption(displayName: String, private val timeSet: Long?, private val rain: Boolean?, private val thunder: Boolean?) :
    DayVoteOption {
    DAY("<c:#debb05>Day", 0, null, null),
    NIGHT("<c:#3105de>Night", 12000, null, null),
    RAIN("<c:#403c4e>Rain", null, true, false),
    STORM("<c:#29282e>Storm", null, true, true),
    CLEAR("<c:#07b1f6>Clear", null, false, false);

    private val displayName: Text = TextParser.parse(displayName)

    override fun execute(ctx: CommandContext<ServerCommandSource>) { // TODO make this schedule the vote
        if (DayVoteCommand.currentVote != null) {
            ctx.source.sendError(TextParser.parse(MESSAGES_CONFIG.vote_in_progress))
            return
        }

        val placeholders = HashMap<String, Text>()
        placeholders["sender"] = LiteralText(ctx.source.name)
        placeholders["type"] = getDisplayName()
        SERVER.playerManager.broadcast(
            PlaceholderAPI.parsePredefinedText(
                TextParser.parse(MESSAGES_CONFIG.vote_start),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
            ),
            MessageType.SYSTEM, Util.NIL_UUID
        )

        DayVoteCommand.currentVote = Vote({
            SERVER.execute {
                if (timeSet != null) {
                    val t = SERVER.overworld.timeOfDay + timeSet
                    SERVER.overworld.timeOfDay = t - t % 24000L
                }
                if (rain != null && thunder != null) {
                    SERVER.overworld.setWeather(0, 0, rain, thunder)
                }
            }
            SERVER.playerManager.broadcast(TextParser.parse(MESSAGES_CONFIG.vote_success), MessageType.SYSTEM, Util.NIL_UUID)
        }, {
            SERVER.playerManager.broadcast(TextParser.parse(MESSAGES_CONFIG.vote_fail), MessageType.SYSTEM, Util.NIL_UUID)
        })
        DayVoteCommand.currentVote?.vote(ctx.source.player.uuid, true)

        DayVoteCommand.TIMER.schedule(object : TimerTask() {
            override fun run() {
                DayVoteCommand.currentVote?.end()
                DayVoteCommand.currentVote = null
            }
        }, GENERAL_CONFIG.dayvote_duration.toLong() * 1000L)
    }

    override fun getDisplayName(): Text {
        return displayName
    }
}

enum class VotingOption(displayName: String, private val success: Boolean) : DayVoteOption {
    YES("<green>Yes", true),
    NO("<red>No", false);

    private val displayName: Text = TextParser.parse(displayName)

    override fun execute(ctx: CommandContext<ServerCommandSource>) {
        if (!DayVoteCommand.voting) {
            ctx.source.sendError(TextParser.parse(MESSAGES_CONFIG.vote_none))
            return
        }

        if (DayVoteCommand.currentVote?.tryVote(ctx.source.player.uuid, this.success) == true) {
            val placeholders = HashMap<String, Text>()
            placeholders["vote"] = getDisplayName()
            ctx.source.sendFeedback(
                PlaceholderAPI.parsePredefinedText(
                    TextParser.parse(MESSAGES_CONFIG.vote_cast),
                    PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                    placeholders
                ),
                false
            )
        } else {
            ctx.source.sendError(TextParser.parse(MESSAGES_CONFIG.vote_already_voted))
        }
    }

    override fun getDisplayName(): Text {
        return displayName
    }
}