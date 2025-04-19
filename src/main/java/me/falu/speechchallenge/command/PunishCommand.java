package me.falu.speechchallenge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.falu.speechchallenge.SpeechChallengeServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;

public class PunishCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager
                .literal("punish")
                .executes(context -> execute(context.getSource().getPlayerOrThrow(), new Random().nextInt(100)))
                .then(CommandManager
                        .argument("value", IntegerArgumentType.integer(0, 100))
                        .executes(context -> execute(context.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(context, "value")))
                )
        );
    }

    private static int execute(ServerPlayerEntity player, int value) {
        SpeechChallengeServer.punishPlayer(player, value);
        return 1;
    }
}
