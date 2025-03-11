package modele;

public class ObjectifBloc extends Case {

    private boolean blocSurCase;
    public ObjectifBloc(Jeu _jeu) {
        super(_jeu);
        blocSurCase = false;
    }

    @Override
    public boolean peutEtreParcouru() {
        return true;
    }

    public boolean getBlocSurCase(){
        return blocSurCase;
    }

    public void setBlosSurCase(boolean e){
        blocSurCase = e;
    }
    
}
