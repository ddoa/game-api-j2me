/*
 * Voorbeeldspel (eigenlijk non-game) voor de studenten van course 4I, ICA, najaar 2005
 *
 */

package vissenkom;

import phonegame.*;

/**
 * Een Bullet wordt door de Vis afgevuurd en verdwijnt weer als ie buiten 
 * de wereldgrenzen komt. Doet verder (nog) niks, dus 'pretty useless'!
 * 
 * @author Paul Bergervoet
 * @version 1.0
 */

public class Bullet extends MoveableGameItem
{
	private Vissenkom mygame;
	
	/**
	 * Maak een Bullet op de opgegeven plaats, met kennis van het spel zelf ivm verwijderen
	 * 
	 * @param x De x-positie van de kogel
	 * @param y De y-positie van de kogel
	 * @param vg referentie naar het spel
	 */
	public Bullet(int x, int y, Vissenkom vg)
	{ 	mygame = vg;
		setImage("/images/fire_bullet1.png", 9, 20);
	    // plaatsen op de opgegeven positie
	    setPosition(x, y);
	    // snelheid 4, naar rechts
	    setDirectionSpeed(0, 4);
	    startMoving();
	}   
	
	/**
	 * De kogel verwijdert zichzelf als ie buiten de wereld geraakt.
	 * 
	 * @see phonegame.MoveableGameItem#outsideWorld()
	 */
	public void outsideWorld() 
	{	mygame.deleteGameItem(this);
	}
}
