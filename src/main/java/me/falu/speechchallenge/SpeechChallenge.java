package me.falu.speechchallenge;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeechChallenge {
    public static final String MOD_ID = "speechchallenge";
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final Identifier TRIGGER_ID = new Identifier(MOD_ID, "trigger_word");
    public static final Identifier CRASH_ID = new Identifier(MOD_ID, "crash_game");

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }
}
