/*
 * Voorbeeldspel (eigenlijk non-game) voor de studenten van course 4I, ICA, najaar 2005
 *
 */

package vissenkom;

import phonegame.*;

/**
 * Een Strawberry is een lekker hapje voor de doodnormale Vis.
 * Doet verder helemaal niets!
 * 
 * @author Paul Bergervoet
 * @version 1.0
 */

public class Strawberry extends GameItem
{
    private int points;
    
	public Strawberry(Vissenkom spel)
	{ 	setImage("/images/strawberry.png", 15, 17);
	    points = 50;
	}   
	
	public void animate()
	{ }
	
	public int getPoints()
	{
	    return points;
	}
	
}
