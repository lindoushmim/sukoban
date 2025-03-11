package VueControleur;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import modele.*;


/** Cette classe a deux fonctions :
 *  (1) Vue : proposer une représentation graphique de l'application (cases graphiques, etc.)
 *  (2) Controleur : écouter les évènements clavier et déclencher le traitement adapté sur le modèle (flèches direction Pacman, etc.))
 *
 */
public class VueControleur extends JFrame implements Observer {
    private Jeu jeu; // référence sur une classe de modèle : permet d'accéder aux données du modèle pour le rafraichissement, permet de communiquer les actions clavier (ou souris)
    private int sizeX; // taille de la grille affichée
    private int sizeY;
    private int largeurFenetre;
    private int hauteurFenetre;
    private final Dimension screenSize;
    int maxWindowSizeX;
    int maxWindowSizeY;

    // icones affichées dans la grille
    private ImageIcon icoHero;
    private ImageIcon icoVide;
    private ImageIcon icoMur;
    private ImageIcon icoBloc;
    private ImageIcon icoObjectifBloc;
    private ImageIcon icoSokoban;
    private ImageIcon icoEnnemi;
    private ImageIcon icoPiege;
    private JLabel imageLabel;
    private JLabel[][] tabJLabel; // cases graphique (au moment du rafraichissement, chaque case va être associée à une icône, suivant ce qui est présent dans le modèle)
    //private JButton scoreButton;
    private JLabel scoreLabel;
    private JButton niveau1Button;
    private JButton niveau2Button;
    private JButton niveau3Button;
    private JButton pauseButton;
    private JButton quitterButton;
    private JButton demarrerButton;

    private JPanel messagePanel;
    private JLabel messageBienvenu;


    private JLabel vieLabel;


    public VueControleur(Jeu _jeu) {
        sizeX = _jeu.SIZE_X;
        sizeY = _jeu.SIZE_Y;
        jeu = _jeu;
        chargerLesIcones();
        placerLesComposantsGraphiques();
        ajouterEcouteurClavier();
        jeu.addObserver(this);
        mettreAJourAffichage();
        largeurFenetre = getSize().width;
        hauteurFenetre = getSize().height;
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        maxWindowSizeX = (int) screenSize.getWidth();
        maxWindowSizeY = (int) screenSize.getHeight();
        niveau2Button.setEnabled(false);
        niveau3Button.setEnabled(false);
    }


    /*
    private void ajouterEcouteurClavier() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jeu.jeuGagnerFonc() || jeu.getNiveeau() < 4) { // à changer
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_LEFT : jeu.deplacerHeros(Direction.gauche); break;
                        case KeyEvent.VK_RIGHT : jeu.deplacerHeros(Direction.droite); break;
                        case KeyEvent.VK_DOWN : jeu.deplacerHeros(Direction.bas); break;
                        case KeyEvent.VK_UP : jeu.deplacerHeros(Direction.haut); break;
                    }
                }
            }
        });
    }
     */

    private void ajouterEcouteurClavier() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!(jeu.getBoolNiveau(jeu.getNiveeau()-1))) { //si le niveau om l on est n  a pas été gagner on peut se deplacer
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_LEFT : jeu.deplacerHeros(Direction.gauche); break;
                        case KeyEvent.VK_RIGHT : jeu.deplacerHeros(Direction.droite); break;
                        case KeyEvent.VK_DOWN : jeu.deplacerHeros(Direction.bas); break;
                        case KeyEvent.VK_UP : jeu.deplacerHeros(Direction.haut); break;
                    }
                }
            }
        });
    }


    private void chargerLesIcones() {
        icoVide = chargerIcone("Images/Vide3.png");
        icoMur = chargerIcone("Images/Mur3.png");
        icoBloc = chargerIcone("Images/caisse.png");
        icoObjectifBloc = chargerIcone("Images/objectif.png");
        icoHero = chargerIcone("Images/joueur.png");
        icoPiege = chargerIcone("Images/piege.png");

        icoSokoban = chargerIcone("Images/Mur.png");
        icoEnnemi = chargerIcone("Images/NES - Gyromite Robot Gyro - Tileset.png");
    }

    private ImageIcon chargerIcone(String urlIcone) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(urlIcone));
        } catch (IOException ex) {
            Logger.getLogger(VueControleur.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return new ImageIcon(image);
    }

    private void setMessageVisible(boolean visible) {
        messagePanel.setVisible(visible);
    }

    private void placerLesComposantsGraphiques() {
        setTitle("Sokoban");
        //setSize(maxWindowSizeX, maxWindowSizeY); ca devrai fonctionner
        setSize(1440, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // permet de terminer l'application à la fermeture de la fenêtre


        messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel welcomeLabel = new JLabel("Bienvenue chez Sokoban !");
        messagePanel.add(welcomeLabel);
        add(messagePanel, BorderLayout.CENTER);

        JComponent grilleJLabels = new JPanel(new GridLayout(sizeY, sizeX)); // grilleJLabels va contenir les cases graphiques et les positionner sous la forme d'une grille

        imageLabel = new JLabel(icoSokoban);
        tabJLabel = new JLabel[sizeX][sizeY];
        scoreLabel = new JLabel("Score : " + jeu.getScoreTotal());
        vieLabel = new JLabel("Vie(s):"+jeu.getVie());

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                JLabel jlab = new JLabel();
                tabJLabel[x][y] = jlab; // on conserve les cases graphiques dans tabJLabel pour avoir un accès pratique à celles-ci (voir mettreAJourAffichage() )
                grilleJLabels.add(jlab);
            }
        }

        niveau1Button = new JButton("Niveau 1");
        niveau2Button = new JButton("Niveau 2");
        niveau3Button = new JButton("Niveau 3");
        //scoreButton = new JButton("Score");
        pauseButton = new JButton("Pause");
        quitterButton = new JButton("Quitter");


        niveau1Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!jeu.getJeuEnCours() && !jeu.finDeJeu()) {
                    setMessageVisible(false);
                    JOptionPane.showMessageDialog(null, "Démarrez le niveau 1 !");
                    jeu.demarrerPartie();
                    niveau2Button.setEnabled(false);
                    niveau3Button.setEnabled(false);
                    requestFocusInWindow();
                }
            }
        });

        niveau2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (jeu.getBoolNiveau(0)) {
                    JOptionPane.showMessageDialog(null, "Démarrez le niveau 2 !");
                    jeu.chargerProchainNiveau();
                    requestFocusInWindow();
                }
            }
        });

        niveau3Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (jeu.getBoolNiveau(1)) {
                    JOptionPane.showMessageDialog(null, "Démarrez le niveau 3 !");
                    jeu.chargerProchainNiveau();
                    requestFocusInWindow();
                }
            }
        });


        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (jeu.getJeuEnCours()){ // si le jeu est en cours et qu on met pause
                    jeu.mettreEnPause();
                } else { // le jeu etait deja arreter et on reprendre le jeu
                    jeu.reprendrePartie();
                }
            }
        });

        quitterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionfinJeu();
            }
        });

        JPanel boutonsPanel = new JPanel();
        boutonsPanel.add(niveau1Button);
        boutonsPanel.add(niveau2Button);
        boutonsPanel.add(niveau3Button);
        JPanel boutonsPanelOptions = new JPanel();
        boutonsPanelOptions.add(scoreLabel);
        boutonsPanelOptions.add(pauseButton);
        boutonsPanelOptions.add(quitterButton);
        add(boutonsPanel, BorderLayout.SOUTH);
        boutonsPanelOptions.add(vieLabel);
        add(boutonsPanelOptions, BorderLayout.NORTH);
        add(grilleJLabels);
    }


    /**
     * Il y a une grille du côté du modèle ( jeu.getGrille() ) et une grille du côté de la vue (tabJLabel)
     */

    private void mettreAJourAffichage() {
        //System.out.println("SCORE : " + jeu.getScore() + " CASEOBJECTIF " + jeu.getNbCaseObjectif() + " NIVEAU " + jeu.getNiveeau());
        //System.out.println("gagner : " + jeu.jeuGagnerFonc());
        //System.out.println("VIE : " + jeu.getVie());
        //System.out.println("GAGNER OU PAS  : " + jeu.finDeJeu());
        //System.out.println("GAGNER OU PAS niveau 0 : " + jeu.getBoolNiveau(0));
        //System.out.println("GAGNER OU PAS niveau 1 : " + jeu.getBoolNiveau(1));
        //System.out.println("GAGNER OU PAS niveau 2 : " + jeu.getBoolNiveau(2));

        scoreLabel.setText("Score : " + jeu.getScoreTotal());
        vieLabel.setText("Vie(s): " + jeu.getVie());

        int xNew = largeurFenetre;
        int yNew = hauteurFenetre;;

/*
        if (jeu.getJeuEnCours() && jeu.getBoolNiveau(jeu.getNiveeau()-1)) { // si on a gagner un niveau
            JOptionPane.showMessageDialog(this, "Bravo ! Vous avez gagné ce niveau !");
        }

 */

            for (int x = 0; x < jeu.SIZE_X; x++) {
                for (int y = 0; y < jeu.SIZE_Y; y++) {
                    Case c = jeu.getGrille()[x][y];

                    if (c != null) {
                        Entite e = c.getEntite();

                        if (e != null) {
                            if (c.getEntite() instanceof Heros) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                tabJLabel[x][y].setIcon(icoHero);
                            } else if (c.getEntite() instanceof Bloc) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                //System.out.println(xNew + " " + yNew);
                                tabJLabel[x][y].setIcon(icoBloc);
                            } else if (c.getEntite() instanceof Fantome) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                tabJLabel[x][y].setIcon(icoEnnemi);
                            }
                        } else {
                            if (jeu.getGrille()[x][y] instanceof Mur) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                tabJLabel[x][y].setIcon(icoMur);
                            } else if (jeu.getGrille()[x][y] instanceof Vide) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                tabJLabel[x][y].setIcon(icoVide);
                            } else if (jeu.getGrille()[x][y] instanceof Trou) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                tabJLabel[x][y].setIcon(icoPiege);
                            } else if (jeu.getGrille()[x][y] instanceof ObjectifBloc) {
                                tabJLabel[x][y].setSize(xNew,yNew);
                                tabJLabel[x][y].setIcon(icoObjectifBloc);
                            }
                        }
                    }
                }

        }
    }

    public void mettreJourBouton(){
        if (jeu.getBoolNiveau(0) && jeu.getNiveeau()==1) {
            niveau2Button.setEnabled(true);
            niveau1Button.setEnabled(false);
        }

        if (jeu.getBoolNiveau(1) && jeu.getNiveeau()==2) {
            niveau3Button.setEnabled(true);
            niveau1Button.setEnabled(false);
            niveau2Button.setEnabled(false);
        }
    }

    private String demanderNomJoueur() {
        return JOptionPane.showInputDialog(this, "Entrez votre nom pour enregistrer son score:");
    }

    public void enregistrerScore(){
        String nomJoueur = demanderNomJoueur();
        int score = jeu.getScoreTotal();
        Score scoreObj = new Score(nomJoueur, score);
        scoreObj.enregistrerScore();
        //jeu.enregistrerScore(nomJoueur,score); pas besoin des deux lignes en haut normalement mais ne fonctionne pas sinon
        //afficherEcranFinDeJeu("Votre score a été enregistré !"); // ca exit aussi
    }

    private void afficherEcranFinDeJeu(String message) {
        JOptionPane.showMessageDialog(this, message, "Fin de jeu", JOptionPane.PLAIN_MESSAGE);
        //jeu.terminerPartie(); // Appel de la méthode terminerPartie() du jeu
    }

    public void actionfinJeu(){
        afficherEcranFinDeJeu("Le jeu est terminé !");
        enregistrerScore();
        jeu.quitterPartie();
    }

    private void verifierFinDeJeu() {
        if (jeu.finDeJeu()) {
            actionfinJeu();
        }
    }

    private void jeuReussi(){
        jeu.niveauReussi();

        if (jeu.getJeuEnCours() && jeu.getBoolNiveau(jeu.getNiveeau() - 1)) {
            JOptionPane.showMessageDialog(this, "Bravo ! Vous avez gagné ce niveau !");
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        mettreAJourAffichage();
        jeu.detecterBlocSurCaseObjectif();
        jeu.mettreAjourVie();
        jeuReussi();

        mettreJourBouton();
        verifierFinDeJeu();

        /*

        // récupérer le processus graphique pour rafraichir
        // (normalement, à l'inverse, a l'appel du modèle depuis le contrôleur, utiliser un autre processus, voir classe Executor)


        SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mettreAJourAffichage();
                    }
                });
        */

    }
}

