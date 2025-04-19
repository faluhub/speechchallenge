package me.falu.speechchallenge.audio;

import com.google.gson.JsonParser;
import me.falu.speechchallenge.SpeechChallenge;
import org.jetbrains.annotations.Nullable;
import org.vosk.Model;
import org.vosk.Recognizer;

public class SpeechRecognizer {
    public final Recognizer recognizer;

    public SpeechRecognizer(@Nullable Model model, int rate) throws Exception {
        if (model != null) {
            this.recognizer = new Recognizer(model, rate);
        } else {
            throw new Exception("Acoustic model not loaded.");
        }
    }

    public String recognize(byte[] data) {
        if (data != null && this.recognizer.acceptWaveForm(data, data.length)) {
            String result = this.recognizer.getResult();
            return JsonParser
                    .parseString(result)
                    .getAsJsonObject()
                    .get("text")
                    .getAsString()
                    .replaceAll("the", "")
                    .trim()
                    .replaceAll(" +", " ");
        }
        return "";
    }
}
