package modele;

public class Trou extends Case {
    public Trou(Jeu _jeu) { super(_jeu); }

    @Override
    public boolean peutEtreParcouru() {
        return true;
    }
}
