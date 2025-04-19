package me.falu.speechchallenge.mixin;

import com.mojang.authlib.GameProfile;
import me.falu.speechchallenge.SpeechChallengeServer;
import me.falu.speechchallenge.owner.ChallengePlayerDataOwner;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ChallengePlayerDataOwner {
    @Unique private boolean shouldExplodeOnFall = false;
    @Unique private int doomTimer = -1;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public void speechchallenge$startTimer() {
        this.doomTimer = 40;
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, this.doomTimer, 1, false, false, false));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, this.doomTimer, 1, false, false, false));
    }

    @Override
    public void speechchallenge$cancelTimer() {
        this.doomTimer = -1;
        this.removeStatusEffect(StatusEffects.GLOWING);
        this.removeStatusEffect(StatusEffects.DARKNESS);
    }

    @Override
    public void speechchallenge$explodeOnFall() {
        this.shouldExplodeOnFall = true;
    }

    @Override
    public int getTeamColorValue() {
        return this.doomTimer == -1 ? super.getTeamColorValue() : Objects.requireNonNull(Formatting.RED.getColorValue());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void doomTimerLogic(CallbackInfo ci) {
        if (this.doomTimer > 0) {
            this.doomTimer--;
        } else if (this.doomTimer == 0) {
            SpeechChallengeServer.punishPlayer((ServerPlayerEntity) (Object) this);
            this.doomTimer = -1;
        }
    }

    @Inject(method = "handleFall", at = @At("TAIL"))
    private void fallLogic(double xDifference, double yDifference, double zDifference, boolean onGround, CallbackInfo ci) {
        if (yDifference < 0.0D && this.shouldExplodeOnFall) {
            this.getWorld().createExplosion(null, null, null, this.getX(), this.getY(), this.getZ(), 10.0F, true, World.ExplosionSourceType.TNT);
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
            this.shouldExplodeOnFall = false;
        }
    }
}
