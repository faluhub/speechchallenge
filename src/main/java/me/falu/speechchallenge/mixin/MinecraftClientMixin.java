package me.falu.speechchallenge.mixin;

import me.falu.speechchallenge.SpeechChallengeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow public abstract @Nullable ClientPlayNetworkHandler getNetworkHandler();

    @Inject(method = "stop", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        SpeechChallengeClient.stop();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cancelPunishment(CallbackInfo ci) {
        if (this.getNetworkHandler() != null && SpeechChallengeClient.CANCEL_BUTTON.wasPressed()) {
            this.getNetworkHandler().sendChatCommand("cancelpunishment");
        }
    }
}
