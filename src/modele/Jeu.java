/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele;

import java.awt.Point;
import java.util.HashMap;
import java.util.Observable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Jeu extends Observable {

    public static final int SIZE_X = 20;
    public static final int SIZE_Y = 10;

    private Son son;
    private Son sonDeplacement;
    private Son sonPiege;
    private Score scoreSave;

    private Heros heros;
    private int nbCaseObjectif; // permettra de savoir il me reste cb de case objectif
    private List<Fantome> fantomes = new ArrayList<>();

    private int score;
    private int scoreTotal;
    private int numeroNiveau;
    private int vie;
    private boolean jeuEnCours;
    private boolean [] niveauGagne;
    private boolean joueurBloquer;

    private HashMap<Case, Point> map = new  HashMap<Case, Point>(); // permet de récupérer la position d'une case à partir de sa référence
    private Case[][] grilleEntites = new Case[SIZE_X][SIZE_Y]; // permet de récupérer une case à partir de ses coordonnées

    public Jeu() {
        nbCaseObjectif = 0;
        score = 0;
        numeroNiveau = 0;
        vie = 5;
        son= new Son("son/vie.wav");
        sonDeplacement = new Son("son/deplacer.wav");
        sonPiege = new Son("son/piege.wav");
        jeuEnCours = false;
        niveauGagne = new boolean[3];
        niveauGagne[0] = false;
        niveauGagne[1] = false;
        niveauGagne[2] = false;
        scoreTotal = 0;
        joueurBloquer = false;
    }

    public Case[][] getGrille() {
        return grilleEntites;
    }

    public Heros getHeros() {
        return heros;
    }

    public void deplacerHeros(Direction d) {
        heros.avancerDirectionChoisie(d);
        jouerSonDeplacement();
        setChanged();
        notifyObservers();
    }

    private void ajouterFantome(Fantome fantome) {
        fantomes.add(fantome);
    }

    public int getScoreTotal() {
        return scoreTotal;
    }

    public void chargerNiveau(String fichier) {
        int xHero = -1, yHero = -1;
        List<Point> blocPositions = new ArrayList<>(); // pour stockes les positions des blocs
        List<Point> objectifPositions = new ArrayList<>(); // pour stocker les positions des objectifs
        List<Point> fantomePositions = new ArrayList<>(); // pour strocker les position des monstres

        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            int y = 0;
            while ((ligne = br.readLine()) != null && y < SIZE_Y) {
                for (int x = 0; x < SIZE_X && x < ligne.length(); x++) {
                    char symbole = ligne.charAt(x);
                    Case caseNouvelle;
                    switch (symbole) {
                        case '#': caseNouvelle = new Mur(this); break;
                        case ' ': caseNouvelle = new Vide(this); break;
                        case '.': caseNouvelle = new Trou(this); break;
                        case '@':
                            xHero = x;
                            yHero = y;
                            caseNouvelle = new Vide(this); // je place le héros après avoir parcouru tout le fichier, en attendant, je crée par défaut une case vide
                            break;
                        case '$':
                            blocPositions.add(new Point(x, y));
                            caseNouvelle = new Vide(this);
                            break;
                            /*
                        case '+':
                            fantomePositions.add(new Point(x, y));
                            caseNouvelle = new Vide(this);
                            break;
                            */

                        default: caseNouvelle = new Vide(this); break;
                    }
                    addCase(caseNouvelle, x, y);
                }
                y++;
            }

            heros = new Heros(this, grilleEntites[xHero][yHero]);
            for (Point position : fantomePositions) {
                Fantome fantome = new Fantome(this, grilleEntites[position.x][position.y]);
                fantomes.add(fantome);
            }
            for (Point blocPosition : blocPositions) {
                Case caseBloc = grilleEntites[blocPosition.x][blocPosition.y];
                Bloc b = new Bloc(this, caseBloc);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier : " + e.getMessage());
        }
    }

    private void addCase(Case e, int x, int y) {
        grilleEntites[x][y] = e;
        map.put(e, new Point(x, y));
    }

    private void ajouterCaseObjectif() {
        for (int x = 0; x < SIZE_X - 1; x++) {
            for (int y = 0; y < SIZE_Y - 1; y++) {
                Case caseCourante = grilleEntites[x][y]; // on peut aussi utiliser la fonction caseALaPosition
                if (caseCourante != null && caseCourante.getEntite() instanceof Bloc) {
                    Entite entite = caseCourante.getEntite();
                    if (entite instanceof Bloc) { // comme ca on est sur de creer autant de nombre de caseObjectif que de blocs
                        Bloc bloc = (Bloc) entite;
                        do {
                            Random random = new Random();
                            int newX = random.nextInt(SIZE_X);
                            int newY = random.nextInt(SIZE_Y);

                            if (newX >= 0 && newX < SIZE_X && newY >= 0 && newY < SIZE_Y) {
                                if (grilleEntites[newX][newY] instanceof Vide) {
                                    grilleEntites[newX][newY] = new ObjectifBloc(this); // on remplace une case vide par une case objectifBloc
                                    nbCaseObjectif +=1;
                                    break; // sort de la boucle si les coordoné sont valides
                                }
                            }
                        } while (true);
                    }
                }
            }
        }
    }


    /** Si le déplacement de l'entité est autorisé (pas de mur ou autre entité), il est réalisé
     * Sinon, rien n'est fait.
     */
    public boolean deplacerEntite(Entite e, Direction d) {
        boolean retour = true;

        Point pCourant = map.get(e.getCase());

        Point pCible = calculerPointCible(pCourant, d);

        if (contenuDansGrille(pCible)) {
            Entite eCible = caseALaPosition(pCible).getEntite();
            if (eCible != null) {
                eCible.pousser(d);
            }

            // si la case est libérée
            if (caseALaPosition(pCible).peutEtreParcouru()) { // normalement une caseObjectif peut etre parcouru alors pk joueur bloc
                e.getCase().quitterLaCase();
                caseALaPosition(pCible).entrerSurLaCase(e);

            } else {
                retour = false;
            }

        } else {
            retour = false;
        }

        return retour;
    }

    private Point calculerPointCible(Point pCourant, Direction d) {
        Point pCible = null;

        switch(d) {
            case haut: pCible = new Point(pCourant.x, pCourant.y - 1); break;
            case bas : pCible = new Point(pCourant.x, pCourant.y + 1); break;
            case gauche : pCible = new Point(pCourant.x - 1, pCourant.y); break;
            case droite : pCible = new Point(pCourant.x + 1, pCourant.y); break;
        }
        return pCible;
    }

    /** Indique si p est contenu dans la grille
     */
    private boolean contenuDansGrille(Point p) {
        return p.x >= 0 && p.x < SIZE_X && p.y >= 0 && p.y < SIZE_Y;
    }

    private Case caseALaPosition(Point p) {
        Case retour = null;

        if (contenuDansGrille(p)) {
            retour = grilleEntites[p.x][p.y];
        }

        return retour;
    }

    public void niveauReussi() {
        if( score == nbCaseObjectif){
            niveauGagne[numeroNiveau-1]=true;
        }
    }

    public void initialisationNiveau(String s) {
        nbCaseObjectif = 0;
        score = 0;
        nbCaseObjectif = 0;
        jeuEnCours = true;
        chargerNiveau(s);
        ajouterCaseObjectif();
    }

    // quand on demarre la partie c est direcment sur le niveau 1
    public void demarrerPartie() {
        if(!niveauGagne[0] && !niveauGagne[1] && !niveauGagne[2]){
            String fichierNiveau = "./niveau/niveau1.txt";
            initialisationNiveau(fichierNiveau);
            numeroNiveau++;
            setChanged();
            notifyObservers();
        }
    }

    public void chargerProchainNiveau() {
        if (niveauGagne[0] && !niveauGagne[1] && !niveauGagne[2]) { // niveau 2
            String fichier = "./niveau/niveau2.txt";
            initialisationNiveau(fichier);
        }

        if (niveauGagne[0] && niveauGagne[1] && !niveauGagne[2]) { // niveau 3
            String fichier = "./niveau/niveau3.txt";
            initialisationNiveau(fichier);
        }
        numeroNiveau++;
        //ajouterFantomes();
        setChanged();
        notifyObservers();
    }

    public boolean getBoolNiveau(int indice){
        return niveauGagne[indice];
    }

    public void mettreEnPause() {
        jeuEnCours = false;
    }

    public void reprendrePartie() {
        jeuEnCours = true;
        setChanged();
        notifyObservers();
    }

    public boolean getJeuEnCours(){
        return jeuEnCours;
    }

    public void quitterPartie() {
        System.exit(0);
    }

    public void terminerPartie(){
        jeuEnCours = false;
        System.exit(0);
    }

    public void supprimerCaseObjectifSiBlocSurLui(Point positionBloc) {
        Case caseSousBloc = caseALaPosition(positionBloc);
        grilleEntites[positionBloc.x][positionBloc.y] = new Vide(this); // on met une case vide à la place d une CaseObjectif
    }

    public void detecterBlocSurCaseObjectif(){ // detecte si y a une entité sur un blocEntité et en fonction ajuste son comportement
        Point pointCase = null;
        for (int x = 0; x < SIZE_X - 1; x++) {
            for (int y = 0; y < SIZE_Y - 1; y++) {
                pointCase = new Point(x, y ); // recupere le point
                if(caseALaPosition(pointCase) instanceof ObjectifBloc){ // si la case est du type objectif
                    Entite eCible = caseALaPosition(pointCase).getEntite();  // on regarde mnt si y a pas de bloc sur la case objectif
                    if (eCible != null) { // y a une entité sur la case
                        if (eCible instanceof Bloc){ // si c est un bloc alors +1 au score et mettre le booleen à faux
                            jouerSonBlocObjectif();
                            score ++;
                            scoreTotal++;
                            supprimerCaseObjectifSiBlocSurLui(pointCase);
                        }
                        if(eCible instanceof Heros)
                            //heros.getCase().quitterLaCase(); // si c est le joueur il peut se deplacer sur cette case ne fonctionne pas
                            joueurBloquer = true;
                    }
                }
            }
        }
    }

    public int getNiveeau(){
        return numeroNiveau;
    }

    public int getScore(){
        return score;
    }

    public int getNbCaseObjectif(){
        return nbCaseObjectif;
    }


    // fonctionnalité pour ajouter des entités qui se deplacer aléatoirement à finir
    public void ajouterFantomes() {
        if (numeroNiveau == 3) {
            // à faire deplacer les fantomes aleatoirement
            deplacerFantomesAleatoirement();
            setChanged();
            notifyObservers();
        }
    }

    private void deplacerFantomesAleatoirement() {
        for (Fantome fantome : fantomes) {
            Random random = new Random();
            int direction = random.nextInt(4);
            Direction dir;
            switch (direction) {
                case 0:
                    dir = Direction.haut;
                    break;
                case 1:
                    dir = Direction.bas;
                    break;
                case 2:
                    dir = Direction.gauche;
                    break;
                case 3:
                    dir = Direction.droite;
                    break;
                default:
                    dir = Direction.haut;
            }
            fantome.avancerDirectionChoisie(dir);
        }
    }

    // partie pour gerer la vie
    public int getVie(){
        return vie;
    }

    public void mettreAjourVie(){ // si le joueur marche sur un piege il perd une vie
        Point pointCase = null;
        for (int x = 0; x < SIZE_X - 1; x++) {
            for (int y = 0; y < SIZE_Y - 1; y++) {
                pointCase = new Point(x, y );

                if(caseALaPosition(pointCase) instanceof Trou){ // si la case est du type piege
                    Entite eCible = caseALaPosition(pointCase).getEntite();  // on regarde mnt si y a pas de bloc sur la case piege
                    if (eCible instanceof Heros ) { // y a une entité sur la case
                        vie --;
                        jouerSonPiege();
                    }
                }
            }
        }
    }

    // pour detecter la fin de la partie
    public boolean finDeJeu(){
        return (niveauGagne[2] || vie==0 || joueurBloquer); // si j ai gagner le niv3 ou bien que mort avant
    }

    // permet de savoir si on a gagner le niveau
    public boolean jeuGagnerFonc(){
        return nbCaseObjectif==score;
    }

    // pour le son
    public void jouerSonDeplacement() {
        sonDeplacement.jouerSon();
    }

    public void jouerSonBlocObjectif() {
        son.jouerSon();
    }

    public void jouerSonPiege() {
        sonPiege.jouerSon();
    }

    // pour le score
    public void enregistrerScore(String nomJoueur, int scoreJoueur) {
            this.scoreSave = new Score(nomJoueur, scoreJoueur);
            this.scoreSave.enregistrerScore();
    }

}

