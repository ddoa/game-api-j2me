/*
 * Voorbeeldspel (eigenlijk non-game) voor de studenten van course 4I, ICA, najaar 2005
 *
 */
package vissenkom;

import phonegame.*;
import phonegame.utils.*;

/**
 * Een Controler die met enige regelmaat een aardbei in de Vissenkom dropt.
 * De vis is daar verzot op!
 * 
 * @author Paul Bergervoet
 * @version 1.0
 */

public class Controler
		implements IAlarmListener
{
	private Vissenkom mygame;

	/**
	 * Maak een Controler
	 * 
	 * @param vg referentie naar het spel zelf
	 */
	public Controler(Vissenkom vg)
	{ 	// referentie onthouden
	    mygame = vg;
		// plaatje is verplicht, nu dus dummy
	    // timertje zetten voor eerste aardbei
	    mygame.setTimer(2, 1, this);
	}
	
	/**
	 * Als timer afloopt plaatsen we een nieuwe aardbei.
	 * De methode van de AlarmListener interface
	 * 
	 * @see phonegame.IAlarmListener#alarm(int)
	 */
	public void alarm(int id)
	{	// aardbei maken
	    Strawberry s = new Strawberry(mygame);
	    // random positie kiezen, maar niet op muur, dus findTilesAt moet 0 opleveren!
	    // we gaan door tot dat zo is.
	    int x = 0;
	    int y = 0;
	    // 
	    do
	    {	// x moet minstens aardbei-breedte van de rechterrand blijven
	        x = mygame.getRandomX(s.getFrameWidth());
	    	y = mygame.getRandomY(s.getFrameHeight());
	    }
	    while (mygame.findTilesAt(x, y, s.getFrameWidth(), s.getFrameHeight()) != 0);
	    // pos invullen
	    s.setPosition(x, y);
	    // aardbei plaatsen
	    mygame.addGameItem(s);
	    // timer voor volgende aardbei
	    mygame.setTimer(Tools.random(50), 1, this);
	}
}
