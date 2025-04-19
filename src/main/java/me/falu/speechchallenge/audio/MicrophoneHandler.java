package me.falu.speechchallenge.audio;

import me.falu.speechchallenge.SpeechChallenge;
import me.falu.speechchallenge.SpeechChallengeClient;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MicrophoneHandler {
    private static final int AUDIO_BUFFER_SIZE = 4096;
    private final AudioFormat format;
    private final Map<String, TargetDataLine> lines;
    private String currentName;
    private TargetDataLine currentLine;

    public MicrophoneHandler() throws Exception {
        this.lines = new HashMap<>();
        this.format = new AudioFormat(SpeechChallengeClient.SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, this.format);
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isLineSupported(lineInfo)) {
                String name = mixerInfo.getName();
                TargetDataLine line = (TargetDataLine) mixer.getLine(lineInfo);
                this.lines.put(name, line);
                if (this.currentLine == null) {
                    this.currentName = mixerInfo.getName();
                    this.currentLine = line;
                }
            }
        }
        this.currentLine.open(this.format);
    }

    public void start() {
        this.currentLine.start();
    }

    public void stop() {
        this.currentLine.stop();
        this.currentLine.flush();
        this.currentLine.close();
    }

    public byte[] read() {
        byte[] buffer = new byte[AUDIO_BUFFER_SIZE];
        int count = this.currentLine.read(buffer, 0, buffer.length);
        return count > 0 ? buffer : null;
    }

    public String getCurrentName() {
        return this.currentName;
    }

    public Set<String> getDevices() {
        return this.lines.keySet();
    }

    public void setDevice(String device) {
        if (this.lines.containsKey(device)) {
            this.currentLine.stop();
            this.currentLine.flush();
            this.currentLine.close();
            this.currentLine = this.lines.get(device);
            try { this.currentLine.open(this.format); }
            catch (Exception e) {
                SpeechChallenge.LOGGER.error("Error while changing microphone device", e);
            }
            this.currentLine.start();
        }
    }
}
