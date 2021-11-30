package nl.enjarai.essentialsnt.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value=MessageCommand.class, priority = 1000000)  // TODO: make this not garbage
public class MessageCommandMixin {
    /**
     * @author Essentialsn't
     * @reason To remove the vanilla /msg command and aliases
     */
    @Overwrite
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    }

//    @ModifyArg(method = "register", index = 0, at = @At(value = "INVOKE", ))
}
