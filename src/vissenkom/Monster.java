/*
 * Voorbeeldspel (eigenlijk non-game) voor de studenten van course 4I, ICA, najaar 2005
 *
 */

package vissenkom;

import phonegame.*;

// import phonegame.utils.MathFloat;

/**
 * Een Monster dat de Vis lastigvalt door voortdurend naar de Vis te bewegen. Dat heeft overigens geen kwalijke
 * gevolgen...
 * 
 * @author Paul Bergervoet
 * @version 1.0
 */

public class Monster extends MoveableGameItem implements IStepListener
{
    private Vissenkom mygame;

    /**
     * Maak een Monster
     * 
     * @param vg
     *                referentie naar het spel zelf
     */
    public Monster(Vissenkom mg)
    { 	// referentie onthouden
        mygame = mg;
        // image
        setImage("/images/alien.png", 20, 31);
        // startpos, doet er niet zo toe
        setPosition(328, 149);
        // snelheid 5, naar rechts
        setDirectionSpeed(0, 5);
        // begin te bewegen!
        startMoving();
        // meld het Monster aan als steplistener
        mygame.addStepListener(this);
    }

    public void stepAction(int stepnr)
    { 	// Bij stap 1, 5, 9, etc...
        if ((stepnr % 4) == 1)
        { // beweeg in de richting van de speler
            moveTowardsAPoint(mygame.getPlayerX(), mygame.getPlayerY());
        }
    }

    /**
     * Botsing met ander gameitem: doe (nu) niks
     */
    public void collisionOccured(GameItem collidedItem)
    {}

    /**
     * Botsing met tiles (muur): stuiteren!
     */
    public void collisionOccured(int tilePattern, boolean hor, int position)
    {
        //if ( (tilePattern & 1) != 0 )
        bounce(hor, position);
    }

    public void outsideWorld()
    {
        setPosition(340, 240);
    }

}