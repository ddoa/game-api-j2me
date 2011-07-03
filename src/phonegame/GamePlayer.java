/*
 * File: 	GamePlayer.java 
 * Created: Jan 18, 2005 
 */
package phonegame;

/**
 * This class represents the player figure in the game. Descendants of this class need to handle the keyboard events
 * that are responsible for the movement of the player.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */

public abstract class GamePlayer extends MoveableGameItem
{    
    /**
     * Flag will be set at player move
     */
    private boolean hasMoved;
    
    /**
     * Flag indicating if there is a key event in this cycle
     */
    private boolean keyPresent;
    
    /**
     * Flag indicating if the key is the result of a key repeat
     */
    private boolean keyRepeated;

    /**
     * Set the two key vars, every cycle!
     * 
     * @param key -- obvious
     * @param repeat
     */
    final void setKeyVars(boolean key, boolean repeat)
    {
    	keyPresent = key;
    	keyRepeated = repeat;
    }

    /**
     * Ask if player has moved.
     * Intended for use by GameEngine, to see if collision detection is needed.
     * 
     * @return boolean indicating if player has moved
     */
    final boolean getMoved()
    {
        return hasMoved;
    }
    /**
     * Clear the hasMoved flag.
     * Intended for use by GameEngine, after key-event has been handled, and collision detection has been done
     */
    final void clearMoved()
    {
        hasMoved = false;
    }
    
    /**
     * Move the gameplayer to a new position. 
     * When you call this method from any of the methods that handle key-events (moveUp, etc.), 
     * it will trigger collision detection.
     * <br/>
     * <em>Note:</em> If you use the method setPosition(x, y) there will be no collision detection.
     * Also, there is no collision detection when you call MovePlayer outside the key handling methods!
     * Collision detection is time consuming and can only be done at a few specific moments:
     * (1) When any item that has speed is moved by the GameEngine and (2) when a key is pressed 
     * and the Player is moved by this method.
     * 
     * @param x the new x-position of the player
     * @param y the new y-position of the player
     */
    public final void movePlayer(int x, int y)
    {
        setPrevious();
        // don't use setPosition(x, y)! This user method prevents collision detection,
        // by making current en previous position equal!
        setX(x);
        setY(y);
        hasMoved = true;
    }
    
    /**
     * Ask if the player receives a key action in this cycle of the game loop.
     * 
     * @return true if a key has been pressed (or held), false otherwise
     */
    public boolean hasKey()
    {
    	return keyPresent;
    }
    
    /**
     * Ask if the key action follows from a key repeat.
     * <br />
     * Example: if the (human) player presses the up-key and holds it, there will
     * be a lot of calls to the moveUp-method. The first time, this method will
     * return 'false', all the other calls this method will return 'true'.
     * 
     * @return true if the key action is caused by the (human) player holding a key,
     * false otherwise
     */
    public boolean isKeyRepeat()
    {
    	return keyRepeated;
    }
    /**
     * Handle the UP key event. On a phone, this event is usually (but not always!) mapped to key 2 or a special
     * 'softbutton'
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void moveUp();

    /**
     * Handle the DOWN key event. On a phone, this event is usually (but not always!) mapped to key 8 or a special
     * 'softbutton'
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void moveDown();

    /**
     * Handle the LEFT key event. On a phone, this event is usually (but not always!) mapped to key 4 or a special
     * 'softbutton'
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void moveLeft();

    /**
     * Handle the RIGHT key event. On a phone, this event is usually (but not always!) mapped to key 6 or a special
     * 'softbutton'
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void moveRight();

    /**
     * Handle the FIRE key event. On a phone, this event is usually (but not always!) mapped to key 5 or a special
     * 'softbutton'
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void fire();

    /**
     * Handle the generic "A" gamekey event. On a phone, this event is usually (but not always!) mapped to key 1
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void pressedButtonA();

    /**
     * Handle the generic "B" gamekey event. On a phone, this event is usually (but not always!) mapped to key 3
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void pressedButtonB();

    /**
     * Handle the generic "C" gamekey event. On a phone, this event is usually (but not always!) mapped to key 7
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void pressedButtonC();

    /**
     * Handle the generic "D" gamekey event. On a phone, this event is usually (but not always!) mapped to key 9
     * <br/>
     * A concrete player must always define this method, but you may leave it empty.
     */
    public abstract void pressedButtonD();
}