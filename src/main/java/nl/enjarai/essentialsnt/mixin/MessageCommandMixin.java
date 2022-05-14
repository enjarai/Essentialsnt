package nl.enjarai.essentialsnt.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MessageCommand.class, priority = 100)
public abstract class MessageCommandMixin {
    @Inject(
            method = "register",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        ci.cancel();
    }
}
