package me.falu.speechchallenge;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import me.falu.speechchallenge.audio.MicrophoneHandler;
import me.falu.speechchallenge.audio.SpeechRecognizer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.vosk.Model;

import java.nio.file.Path;
import java.util.Optional;

public class SpeechChallengeClient implements ClientModInitializer {
    public static SimpleOption<String> MIC_DEVICE;
    public static final int SAMPLE_RATE = 16_000;
    public static KeyBinding CANCEL_BUTTON;
    private static SpeechRecognizer RECOGNIZER;
    private static MicrophoneHandler MICROPHONE;
    private static String LAST_RESULT = "";
    private static Thread LISTEN_THREAD;

    @SuppressWarnings("BusyWait")
    private static void listenThreadTask() {
        while (true) {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (RECOGNIZER == null) {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("No model loaded. Check your logs.").formatted(Formatting.RED), true);
                    }
                    Thread.sleep(10_000);
                    initializeSpeechRecognizer();
                } else if (MICROPHONE == null) {
                    Thread.sleep(10_000);
                    initializeMicrophone();
                } else {
                    String result = RECOGNIZER.recognize(MICROPHONE.read());
                    if (!result.isEmpty() && !result.equals(LAST_RESULT)) {
                        SpeechChallenge.log(result);
                        LAST_RESULT = result;
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage(LAST_RESULT);
                            for (String word : LAST_RESULT.trim().split(" ")) {
                                if (word.contains("e")) {
                                    ClientPlayNetworking.send(SpeechChallenge.TRIGGER_ID, PacketByteBufs.create().writeString(word));
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                SpeechChallenge.LOGGER.error("Error while listening to microphone input", e);
            }
        }
    }

    public static void initializeSpeechRecognizer() {
        SpeechChallenge.log("Loading acoustic model...");
        Path modelPath = FabricLoader.getInstance().getConfigDir().resolve("model");
        try {
            RECOGNIZER = new SpeechRecognizer(new Model(modelPath.toAbsolutePath().toString()), SAMPLE_RATE);
            SpeechChallenge.log("Loaded acoustic model.");
        } catch (Exception e) {
            SpeechChallenge.LOGGER.error("Error while loading acoustic model", e);
        }
    }

    public static void initializeMicrophone() {
        try {
            MICROPHONE = new MicrophoneHandler();
            MICROPHONE.start();
            SpeechChallenge.log("Initialized microphone.");
        } catch (Exception e) {
            SpeechChallenge.LOGGER.error("Error while initializing microphone handler", e);
        }
    }

    public static void stop() {
        LISTEN_THREAD.interrupt();
        MICROPHONE.stop();
        RECOGNIZER = null;
        MICROPHONE = null;
        LISTEN_THREAD = null;
    }

    @Override
    public void onInitializeClient() {
        initializeMicrophone();
        MIC_DEVICE = new SimpleOption<>(
                "options.micDevice",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.literal(value),
                new SimpleOption.LazyCyclingCallbacks<>(
                        () -> Lists.newArrayList(MICROPHONE.getDevices()),
                        Optional::of, Codec.STRING
                ),
                MICROPHONE.getCurrentName(),
                value -> MICROPHONE.setDevice(value)
        );
        initializeSpeechRecognizer();
        LISTEN_THREAD = new Thread(SpeechChallengeClient::listenThreadTask);
        LISTEN_THREAD.start();
        CANCEL_BUTTON = KeyBindingHelper.registerKeyBinding(new KeyBinding("Cancel Doom", GLFW.GLFW_KEY_F8, "Speech Challenge"));
        ClientPlayNetworking.registerGlobalReceiver(SpeechChallenge.CRASH_ID, (client, handler, buf, responseSender) -> client.close());
    }
}
