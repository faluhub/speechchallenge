package me.falu.speechchallenge.mixin;

import com.mojang.brigadier.CommandDispatcher;
import me.falu.speechchallenge.command.CancelPunishmentCommand;
import me.falu.speechchallenge.command.PunishCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void registerCommands(CallbackInfo ci) {
        CancelPunishmentCommand.register(this.dispatcher);
        PunishCommand.register(this.dispatcher);
    }
}
