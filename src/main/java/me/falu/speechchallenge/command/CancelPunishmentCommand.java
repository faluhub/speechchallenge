package me.falu.speechchallenge.command;

import com.mojang.brigadier.CommandDispatcher;
import me.falu.speechchallenge.owner.ChallengePlayerDataOwner;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CancelPunishmentCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager
                .literal("cancelpunishment")
                .executes(context -> execute(context.getSource().getPlayerOrThrow()))
                .then(CommandManager
                        .argument("player", EntityArgumentType.player())
                        .executes(context -> execute(EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int execute(ServerPlayerEntity player) {
        ((ChallengePlayerDataOwner) player).speechchallenge$cancelTimer();
        return 1;
    }
}
