/*
 * Voorbeeldspel (eigenlijk non-game) voor de studenten van course 4I, ICA, najaar 2005
 *
 */

package vissenkom;

import phonegame.*;

/**
 * De vis is de speler in dit merkwaardige spelletje
 * 
 * @author Paul Bergervoet
 * @version 1.0
 */

public class Vis extends GamePlayer
{
    private int points;

    private Vissenkom mygame;

	/**
	 * Maak een Vis
	 * 
	 * @param vg referentie naar het spel zelf
	 */
    public Vis(Vissenkom thegame)
    {
        points = 0;
        mygame = thegame;
        setImage("/images/fish.png", 36, 22);
        setPosition(250, 250);
    }

    /**
     * Animatie-methode: doe nu niks (Vis heeft ook maar een enkel frame,
     * bij frames voor de vier richtingen kun je bij het bewegen het juiste frame
     * zetten.)
     * 
     * @see nl.han.ica.propedeuse.gameapi.GameItem#animate()
     */
    protected void animate()
    {
        // do nothing
    }

    /**
     * Negen knopjes-afhandelaars. Ze moeten er alle negen staan, maar mogen niks doen.
     */

    /**
     * Vier pijltoetsen: verplaats de Vis
     */
    public void moveUp()
    { 
        movePlayer(getX(), getY() - 8);
    }

    public void moveDown()
    { 
        movePlayer(getX(), getY() + 8);
    }

    public void moveLeft()
    { 	// controleer of de Vis de wereld niet uitzwemt. Check linkerkant is genoeg!
        if (getX()-8 >= mygame.getMinX() )
        {	
            movePlayer(getX() - 8, getY());
        } // else: doe niks, move gaat niet door
    }

    public void moveRight()
    {
        movePlayer(getX() + 8, getY());
    }

    /**
     * De select-button: schieten!
     */
    public void fire()
    { 	// maak nieuwe kogel, 36 pixels naar rechts vanaf eigen positie van de Vis
        Bullet b = new Bullet(getX() + 36, getY(), mygame);
        // plaats kogel in spel
        mygame.addGameItem(b);
    }

    /**
     * Vier toetsen van het numerieke keyboard: niks doen
     */
    public void pressedButtonA()
    { //System.out.println("A");
    }

    public void pressedButtonB()
    { //System.out.println("B");
    }

    public void pressedButtonC()
    { //System.out.println("C");
    }

    public void pressedButtonD()
    { //System.out.println("D");
    }

    /**
     * Botsing met ander gameitem: Strawberry opeten! Lekker! 
     * Botsing met Monster: er gebeurt niks.
     * 
     * Dit is de eerste methode van de CollisionListener interface
     */
    public void collisionOccured(GameItem collidedItem)
    { 	// isteen aardbei?
        if (collidedItem instanceof Strawberry)
        { // scoren!
            points = points + ((Strawberry)collidedItem).getPoints();
            // aardbei weg
            mygame.deleteGameItem(collidedItem);
            // game zet puntenaantal op het GameDashboard
            mygame.setPoints(points);
            //System.out.println("hap");
        } else if (collidedItem instanceof Monster)
        {
            //System.out.println("au, au, au");
        }
    }

    /**
     * Botsing met tiles (muur): He?? Vis zwemt gewoon door de muur!!
     * 
     * Dit is de tweede methode van de CollisionListener interface
     */
    public void collisionOccured(int tilePattern, boolean hor, int position)
    { 
    }

}