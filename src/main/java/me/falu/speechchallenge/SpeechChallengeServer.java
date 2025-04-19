package me.falu.speechchallenge;

import me.falu.speechchallenge.owner.ChallengePlayerDataOwner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SpeechChallengeServer implements ModInitializer {
    public static void punishPlayer(ServerPlayerEntity player) {
        Random random = new Random();
        int value = random.nextInt(100);
        SpeechChallenge.log("Punishment value: " + value);
        punishPlayer(player, value);
    }

    public static void punishPlayer(ServerPlayerEntity player, int value) {
        if (value < 50 || (value < 80 && player.getServerWorld().getRegistryKey().equals(World.NETHER))) {
            for (int i = 0; i < 5; i++) {
                player.getWorld().createExplosion(null, null, null, player.getX(), player.getY(), player.getZ(), (float) (4.0D + player.getRandom().nextDouble() * 1.5D * 5.0D), false, World.ExplosionSourceType.TNT);
            }
        } else if (value < 60) {
            player.kill();
        } else if (value < 65) {
            player.setVelocity(new Vec3d(0.0D, 100.0D, 0.0D));
            player.velocityModified = true;
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
            ((ChallengePlayerDataOwner) player).speechchallenge$explodeOnFall();
        } else if (value < 70) {
            player.setHealth(2.0F);
            player.setFireTicks(20 * 20);
        } else if (value < 75) {
            for (int i = 0; i < 100; i++) {
                Entity entity = spawnEntity(EntityType.CHICKEN, player.getPos(), player.getServerWorld());
                if (entity != null) {
                    entity.kill();
                }
            }
            player.kill();
        } else if (value < 80) {
            player.teleport(player.getServerWorld(), player.getX(), 400.0D, player.getZ(), player.getHeadYaw(), player.getPitch());
        } else if (value < 85) {
            new Thread(() -> {
                for (int i = 0; i < 20; i++) {
                    spawnEntity(EntityType.LIGHTNING_BOLT, player.getPos(), player.getServerWorld());
                    try { Thread.sleep(500L); }
                    catch (InterruptedException ignored) {}
                }
            }).start();
        } else if (value < 90) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 100, false, false));
            player.teleport(player.getServerWorld(), player.getBlockX() + 0.5D, player.getY(), player.getBlockZ() + 0.5D, player.getHeadYaw(), player.getPitch());
            for (BlockPos pos : BlockPos.iterate(player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getBlockX(), player.getBlockY() + 24, player.getBlockZ())) {
                player.getServerWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
            }
            player.getServerWorld().setBlockState(player.getBlockPos().up(24), Blocks.ANVIL.getDefaultState());
        } else if (value < 95) {
            new Thread(() -> {
                if (player.getServer() == null) { return; }
                for (BlockPos pos : BlockPos.iterate(player.getBlockX() - 1, player.getBlockY(), player.getBlockZ() - 1, player.getBlockX() + 1, 0, player.getBlockZ() + 1)) {
                    if (pos.getY() <= 5 && player.getServerWorld().getBlockState(pos).isOf(Blocks.BEDROCK)) {
                        continue;
                    }
                    player.getServer().submit(() -> player.getServerWorld().setBlockState(pos, Blocks.AIR.getDefaultState()));
                    try { Thread.sleep(500L); }
                    catch (InterruptedException ignored) {}
                }
            }).start();
        } else if (value < 99) {
            NbtCompound compound = new NbtCompound();
            compound.putBoolean("powered", true);
            for (int i = 0; i < 3; i++) {
                spawnEntity(EntityType.CREEPER, player.getPos(), player.getServerWorld(), compound);
            }
        } else {
            ServerPlayNetworking.send(player, SpeechChallenge.CRASH_ID, PacketByteBufs.empty());
        }
    }

    public static Entity spawnEntity(EntityType<?> entityType, Vec3d pos, ServerWorld world) {
        return spawnEntity(entityType, pos, world, new NbtCompound());
    }

    public static Entity spawnEntity(EntityType<?> entityType, Vec3d pos, ServerWorld world, @NotNull NbtCompound compound) {
        if (!World.isValid(BlockPos.ofFloored(pos))) { return null; }
        compound.putString("id", Registries.ENTITY_TYPE.getId(entityType).toString());
        Entity entity1 = EntityType.loadEntityWithPassengers(compound, world, entity -> {
            entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, entity.getYaw(), entity.getPitch());
            return entity;
        });
        if (entity1 instanceof MobEntity) {
            ((MobEntity) entity1).initialize(world, world.getLocalDifficulty(entity1.getBlockPos()), SpawnReason.COMMAND, null, null);
        }
        world.spawnNewEntityAndPassengers(entity1);
        return entity1;
    }

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(SpeechChallenge.TRIGGER_ID, (server, player, handler, buf, responseSender) -> {
            String word = buf.readString();
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(word.toUpperCase()).formatted(Formatting.BOLD, Formatting.DARK_RED)));
            }
            ((ChallengePlayerDataOwner) player).speechchallenge$startTimer();
        });
    }
}
