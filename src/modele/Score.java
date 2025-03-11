package modele;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Score {
    private String nomJoueur;
    private int score;

    public Score(String nomJoueur, int score) {
        this.nomJoueur = nomJoueur;
        this.score = score;
    }

    public void enregistrerScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(nomJoueur + ", " + score);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("n arrive pas à sauvegarder le score " + e.getMessage());
        }
    }
}
