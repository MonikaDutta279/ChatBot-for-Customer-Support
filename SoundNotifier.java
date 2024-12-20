import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundNotifier {

    public static void playSound(String soundFile) {
        try {
            File sound = new File(soundFile);  // Put your .wav file here
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(sound);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
