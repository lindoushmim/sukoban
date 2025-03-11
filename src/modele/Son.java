package modele;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Son {
    private String fichier;

    public Son(String fichierNom) {
        fichier = fichierNom;
    }

    public void jouerSon(){
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fichier));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
