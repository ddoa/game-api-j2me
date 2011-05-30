/*
 * Voorbeeldspel (eigenlijk non-game) voor de studenten van course 4I, ICA, najaar 2005
 *
 */
package vissenkom;

import phonegame.*;

/**
 * De Vissenkom is het spel zelf (extends GameEngine!)
 * 
 * @author Paul Bergervoet
 * @version 1.0
 */
public class Vissenkom extends GameEngine implements IMenuListener {
    // de GameItems waarmee we beginnen

    private Vis player;
    private Monster monster1;
    private Controler cnt;
    // geeft score weer
    private GameDashboard db;
    private static final String exitMenuItem = "Exit";
    private static final String pauseMenuItem = "Pause";
    private static final String playMenuItem = "Play";
    private static final String[] vissenmenu = {playMenuItem, pauseMenuItem, exitMenuItem};

    /**
     * Constructor van spel: maak de start layout.
     */
    public Vissenkom() {	// Constructor van superklasse (GameEngine) moet aangeroepen worden
        super();
        // grenzen van de wereld
        setBounds(0, 0, 600, 600);
        // mooi blauwgroen.... uhhh.
        setBackgroundColor(34, 204, 255);

        // Dashboard maken en plaatsen
        db = new GameDashboard();
        db.setForegroundColor(255, 255, 255);
        db.setBackgroundColor(255, 0, 0);
        db.setSize(100, 16);
        db.addItem("Pts", "0!");
        addGameDashboard(db);

        makeMenu(vissenmenu, this);
        // tiles neerleggen
        buildEnvironment();

        // maak een monster
        monster1 = new Monster(this);
        this.addGameItem(monster1);

        // maak de controler voor het neerzetten van Strawberry's
        cnt = new Controler(this);

        // maak de Vis, dit is de speler dus 'addPlayer' en niet 'addGameItem'
        player = new Vis(this);
        this.addPlayer(player);
        // zorg dat de Vis altijd ongeveer in het midden van de viewport blijft
        this.setPlayerPositionOnScreen(PLAYER_HCENTER | PLAYER_VCENTER);
        this.setPlayerPositionTolerance(0.3, 0.3);

        // GameSound.playSound("/res/sounds/LightWorld.mid", false, true);
        // setup game: zet viewport goed e.d.
        setupGame();
    }

    /** 
     * Acties op het menu afhandelen (dat is nu alleen exit).
     * 
     * @see phonegame.IMenuListener#menuAction(java.lang.String)
     */
    public void menuAction(String label) {
        if (label.equals(exitMenuItem)) {
            exitGame();
        } else if (label.equals(pauseMenuItem)) {
            stopGame();
        } else if (label.equals(playMenuItem)) {
            startGame();
        }
    }

    /**
     * Geef x-positie van speler
     * 
     * @return xpos, een int
     */
    public int getPlayerX() {
        return player.getX();
    }

    /**
     * Geef y-positie van speler
     * 
     * @return ypos, een int
     */
    public int getPlayerY() {
        return player.getY();
    }

    /**
     * Zet het puntenaantal op het dashboard
     * 
     * @param p het totaal aantal punten
     */
    public void setPoints(int p) {
        db.setItemValue("Pts", p + "!");
    }

    /**
     * bouw achtergrond van wereld (tiles) op.
     */
    private void buildEnvironment() {   // get path to tile images
        String[] imagePaths = {"/images/tile1.png",
            "/images/tile2.png",};
        // create map
        byte[][] map = {
            {2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 1, 2, 1, 2, 1, 1},
            {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
            {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
            {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1},
            {1, 2, 1, 2, 1, 2, 1, 2, 0, 0, 0, 0, 0, 0, 0, 1, 2}
        };

        // add map
        this.setTileImages(imagePaths, 10, 10);
        this.addEnvironmentMap(map, 200, 200);
    }
}
