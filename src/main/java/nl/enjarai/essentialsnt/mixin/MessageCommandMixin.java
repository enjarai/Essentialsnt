package nl.enjarai.essentialsnt.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    /**
     * @author Essentialsn't
     * @reason To remove the vanilla /msg command and aliases
     */
    @Overwrite
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    }
}
