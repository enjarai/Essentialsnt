package nl.enjarai.essentialsnt.commands.dayvote

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import nl.enjarai.essentialsnt.util.Vote
import java.util.*

object DayVoteCommand {
    val TIMER = Timer()
    const val DAYVOTE_PERMISSION_NODE = "essentialsnt.commands.dayvote"

    var currentVote: Vote? = null
    val voting: Boolean get() = currentVote != null

    fun register() {
        ArgumentTypes.register("dayvote", DayVoteArgumentType::class.java, ConstantArgumentSerializer(DayVoteArgumentType::dayVote))
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
            dispatcher.register(
                CommandManager.literal("vote")
                    .requires(
                        Permissions.require(
                            DAYVOTE_PERMISSION_NODE,
                            true
                        )
                    )
                    .then(
                        CommandManager.argument("option", DayVoteArgumentType())
                            .executes(this::vote)
                    )
            )
        })
    }


    private fun vote(ctx: CommandContext<ServerCommandSource>): Int {
        val player = try { ctx.source.player } catch (_: CommandSyntaxException) { return 0 }
        val option = ctx.getArgument("option", DayVoteOption::class.java)

        option.execute(ctx)

        return 1
    }
}