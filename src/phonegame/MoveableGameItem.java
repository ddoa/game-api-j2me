/*
 * File: 	MoveableGameItem.java
 * Created: Jan 23, 2005
 */
package phonegame;

import phonegame.utils.Tools;

/**
 * Class MoveableGameItem represents a moveable actor in the game.
 * MoveableGameItem is an abstract class. Create subclasses to make
 * your MoveableGameItems.
 * <br/>
 * The game engine does collision detection for MoveableGameItems. This is
 * very time consuming, so make sure you only extend this class when the
 * items are really moving!
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */
public abstract class MoveableGameItem extends GameItem
{
   /**
     * Utility constant: pi/2
     */
    private static final double HALF_PI = Math.PI/2;

    /**
     * Utility constant: 2*pi
     */
    private static final double TWOPI = 2*Math.PI;

    /**
     * Speed of the item in pixels. This is zero at the start.
     * <p/>
     * Note: This variable is not really an int, but a MathFloat fp number!
     */
    private double speedInPixels = 0; 

    /**
     * sine of direction (inverted, to change math coordinates to java)
     *
     * Easier to work with sin&cos of angle, than with angle itself, so the direction
     * is not kept in a variable
     * 
     * Note: at start sindir is set to zero and cosdir to 1, meaning that the
     * Item will move right if speed only is set.
     */
    private double sindir = 0;

    /**
     * cosine of direction
     */
    private double cosdir = 1;

    /**
     * Boolean indicating if the object is moving. Not the speed!
     */
    private boolean move = false;

    /**
     * Boolean indicating if the object is moving. Not the speed!
     */
    private boolean collisionDetection = true;

    /**
     * Friction in the speed
     */
    private double friction = 1.0;

    /**
     * startposition of item: x
     */
    private int startXPosition;

    /**
     * startposition of item: y
     */
    private int startYPosition;

    /**
     * boolean indicating if start position has not been set.
     * (First call of setPosition will set the start position and clear this boolean)
     */
    private boolean startPosition = true;

    /**
     * Previous x position, that is the position where the item was before a move
     * Needed for collision detection
     */
    private int prevX;

    /**
     * Previous y position, that is the position where the item was before a move
     * Needed for collision detection
     */
    private int prevY;
  
    /**
     * Sets the position of the item inside the game.
     * <br/>
     * There will be no collision detection when an Item's position is set in this way.
     * 
     * @param x
     *            the horizontal position
     * @param y
     *            the vertical position
     */
    public void setPosition(int x, int y)
    {
        super.setPosition(x, y);
        setPrevious();
        if (startPosition)
        {
            startXPosition = x;
            startYPosition = y;
            startPosition = false;
        }
    }
    
    /**
     * Ask if this MoveableGameItem is receiving collision events.
     * 
     * @return boolean, true if the engine must detect collisions
     */
    public boolean hasCollisionDetection()
    {	
        return collisionDetection;
    }
    
    /**
     * Turn collision detection on/off for this MoveableGameItem.
     * <br/>
     * Note: By default, collision detection is turned on. Turning collision detection
     * off speeds up the game.
     * 
     * @param cd a boolean, true means on, of course!
     */
    public void setCollisionDetection(boolean cd)
    {	
        collisionDetection = cd;
    }
    
    /**
     * Set the speed of the object. Speed is measured in pixels per cycle.
     * <br/>
     * Setting the speed does not change the direction.
     * If at startup no direction is set, the Item will move to the right.
     * <br/>
     * Normally you will enter an integer number, the number of pixels an Item will move
     * per cycle.
     * <br/>
     * You can also enter the speed of an item as a double value. In this way you can slowly 
     * change the speed of an Item, like from 6.0,to 5.9, 5.8 etc.
     * Changing speed with integer values will often result in quick changes.)
     * 
     * @param speed
     *            double, the speed in pixels per cycle. You may also enter an integer value.
     */
    public void setSpeed(double speed)
    {
        speedInPixels = speed;
    }

    /**
     * This method sets the friction of the movement. The number you enter is the part of the
     * speed the Item loses in every cycle of the game loop.
     * <br/>
     * If you want a GameItem to slow down gradually, use numbers like 0.05, just greater than zero. 
     * This causes the GameItem to lose 5 per cent of it's speed every cycle.
     * 
     * @param fpfriction
     *            double, the firction of the movement.
     */
    public void setFriction(double fpfriction)
    {
        friction = 1-fpfriction;
    }

    /**
     * Sets the direction of motion. Value is in degrees: 0 is to the right,
     * radians run counterclockwise, so pi/2 is up, etc. 
     * 
     * @param radian
     *            value usually between 0 and 2*pi
     */
    public void setDirectionRadians(double radian)
    {
        sindir = -Math.sin(radian);
        cosdir = Math.cos(radian);
    }

    /**
     * Sets the direction of motion. Value is in degrees: 0 is to the right,
     * degrees run counterclockwise, so 90 is up, etc. You can enter values
     * smaller than 0 or greater than 360: 370 degrees = 10 degrees etc.
     * 
     * @param degrees
     *            value usually between 0 and 360
     */
    public void setDirection(int degrees)
    {
        setDirectionRadians( Math.toRadians(degrees % 360) );
    }

    /**
     * Temp method for testing purposes
     * @return 
     *
    public String dirString()
    {
        return "Direction: " + MathFloat.toString(cosdir, 2, 2) + ", " + MathFloat.toString(sindir, 2, 2);
    }
     */

    /**
     * Set the direction and the speed in which the item needs to move.
     * 
     * 
     * @param degrees
     *            degrees Degrees value between 0 and 360
     * @param speed
     *            The speed in pixels per cycle
     */
    public void setDirectionSpeed(int degrees, double speed)
    {
        setDirection(degrees);
        setSpeed(speed);
    }

    /**
     * Set both x- and y-speed. Used by methods that set thsese speeds separately.
     * @param xspeed x-speed as double, in pixels
     * @param yspeed x-speed as double, in pixels
     */
    private void setXYSpeed(double xspeed, double yspeed)
    {	speedInPixels = Math.sqrt(xspeed*xspeed + yspeed*yspeed);
    	if ( speedInPixels > 0 )
    	{	sindir = yspeed/speedInPixels;
    		cosdir = xspeed/speedInPixels;
    	}
    }

    /**
     * Sets the speed in horizontal (x) direction, with a double value. 
     * The vertical speed will not change.
     * A positive x-speed means that the object is moving right.
     * <br/>
     * Use this method only if you want to do special calculations on speed, 
     * that only affect one direction, like falling
    * 
    * @param xspeed the horizontal speed in pixels, as a double.
     */
    public void setXSpeed(double xspeed)
    {
        setXYSpeed(xspeed, speedInPixels*sindir);
    }
    
    /**
     * Sets the speed in vertical (y) direction, with a double value. 
     * The horizontal speed will not change.
     * A positive y-speed means that the object is moving down.
     * <br/>
     * Use this method only if you want to do special calculations on speed, 
     * that only affect one direction, like falling
     * 
     * @param yspeed the vertical speed in pixels, as a double.
     */
    public void setYSpeed(double yspeed)
    {
        setXYSpeed(speedInPixels*cosdir, yspeed);
    }

    
    /**
     * Gets the current direction in radians.
     * 
     * @return double, direction in radians
     */
    public double getDirectionRadians()
    {	// calculate arcsin of the smaller value of cos and sin
        if ( Math.abs(cosdir) < Math.abs(sindir) )
        {
            if ( sindir < 0 )				// == -sindir>0, boven dus
                return Math.PI/2 - Tools.arcsin(cosdir);
            else
                return Math.PI + Math.PI/2 + Tools.arcsin(cosdir);
        } else
        {
            if ( cosdir > 0 )			// rechterkant
                return Tools.arcsin(-sindir);
            else
                return Math.PI - Tools.arcsin(-sindir);
        }
    }
    
    /**
     * Gets the current direction in degrees.
     * <br/>
     * Note: You can get small errors because of rounding. Also you may get
     * the value -60, when you had set the value originally to 300! So watch it
     * when you make tests like 'if (getDirection() == nnn )' !
     * 
     * @return integer, direction in degrees
     */
    public int getDirection()
    {		// trick with 360 to avoid negative results...
        return (360+Tools.round(Math.toDegrees(getDirectionRadians())))%360;
    }
    
    /**
     * Gets the speed.
    * 
     * @return speed in pixels, as a double.
     */
    public double getSpeed()
    {	
        return speedInPixels;
    }
    
    /**
     * Gets the speed in horizontal (x) direction.
     * 
     * @return x-speed in pixels, as a double.
     */
    public double getXSpeed()
    {	
        return speedInPixels*cosdir;
    }
    
    /**
     * Gets the speed in vertical (y) direction. A positive y-speed
     * means that the object is moving down.
     * 
     * @return y-speed in pixels, as a double.
     */
    public double getYSpeed()
    {	
        return speedInPixels*sindir;
    }
    
    /**
     * Gets the friction.
     * 
     * @return friction in pixels, as a double.
     */
    public double getFriction()
    {	
        return 1-friction;
    }
    
    /**
     * Find out if item moves to the left. Utility method for collision detection.
     * 
     * @return true if item is moving left, false if moving right, or not moving!
     */
    public final boolean movesLeft()
    {	// look at position, rather than cosdir: player may not have speed!
        return (getX()<prevX);
    }

    /**
     * Find out if item moves up. 
     * 
     * @return true if item is moving up, false if moving down, or not moving!
     */
    public final boolean movesUp()
    {
        return (getY()<prevY);
    }

    /**
     * Resets the gameitem to its original starting position.
     * <br/>No collision detection!
     */
    public void jumpToStartPosition()
    {
        setPosition(startXPosition, startYPosition);
    }

    /**
     * Jumps to a position accoring to the specified coordinates.
     * Deleted, because it is the same as setPosition
     * 
     * @param xPos
     *            the X position
     * @param yPos
     *            the Y position
    public void jumpToPosition(int xPos, int yPos)
    {
        setPosition(xPos, yPos);
    }
     */

    /**
     * Jumps to a random position in the world
     * Deleted, since it is useles, because MoveableGameItem doesn't know the bounds
     * of the world. Programmers can call GameEngine.GetRandomX(w) and getRandomY(h) instead.
     * 
    public void jumpToRandomPosition()
    {
        setPosition(Tools.random(100), Tools.random(100));
    }
     */

    /**
     * The item moves towards a defined point with the current speed.
     * 
     * @param otherXPosition
     *            X position of the point
     * @param otherYPosition
     *            Y position of the point
     */
    public void moveTowardsAPoint(int otherXPosition, int otherYPosition)
    {
        int dx = otherXPosition - getX();
        int dy = otherYPosition - getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        sindir = dy/distance;
        cosdir = dx/distance;
    }

    /**
     * Set previous position to the current x- and y-values.
     * To be used by subclasses (gamePlayer!) to record previous pos before a move.
     * The previous position is needed in collision detection.
     */
    final void setPrevious()
    {
        prevX = getX();
        prevY = getY();
    }
    
    /**
     * Gets the previous x-pos, at start of move utility method for collision
     * detection
     * 
     * @return the previous x (at start of move)
     */
    public final int getPrevX()
    {
        return prevX;
    }

    /**
     * Gets the previous y-pos, at start of move utility method for collision
     * detection.
     * 
     * @return the previous y (at start of move)
     */
    public final int getPrevY()
    {
        return prevY;
    }

    /**
     * Gets the fraction of the x-move that has been done up to a collision with
     * a tile utility method for collision detection, to find out if horizontal
     * collision happens before vertical collision. Therefore: package scope.
     * 
     * @return the fraction of the x-move, a MathFloat number between 0 and 1
     */
    final double getXFactor(int colx)
    {
        if (getX() == prevX)
        {	// the calculation is not quite right (since (colx-prevx)/0 is not equal to 1.... 
            // but it is ok for collision detection, because >=1 means 'no collision'
            return 1;
        } else
        {
            return ( (double)colx - prevX)/(getX() - prevX);
        }
    }

    /**
     * Gets the fraction of the y-move that has been done up to a collision with
     * a tile utility method for collision detection, to find out if horizontal
     * collision happens before vertical collision. Therefore: package scope.
     * 
     * @return the fraction of the y-move, a MathFloat number between 0 and 1
     */
    final double getYFactor(int coly)
    {
        if (getY() == prevY)
        {
            return 1;
        } else
        {
            return ( (double)coly - prevY)/(getY() - prevY);
        }
    }

    /**
     * Determines the y that matches the partial x movement when a horizontal
     * collision occurs utility method for collision detection, especially when
     * bouncing or stopping the object Therefore: package scope.
     * 
     * @param xfactor,
     *            a Mathfloat indicating the factor of the x-move up to the
     *            vertical collision
     * @return the corresponing y-pos, an int
     */
    final int getMatchingY(double xfactor)
    {
        return prevY + Tools.round(xfactor*(getY() - prevY));
    }

    /**
     * Determines the x that matches the partial y movement when a vertical
     * collision occurs utility method for collision detection, especially when
     * bouncing or stopping the object Therefore: package scope.
     * 
     * @param yfactor,
     *            a Mathfloat indicating the factor of the y-move up to the
     *            vertical collision
     * @return the corresponing x-pos, an int
     */
    final int getMatchingX(double yfactor)
    {
        return prevX + Tools.round(yfactor*(getX() - prevX));
    }

    /**
     * Undo last move. Typically used after a tile collision has happened
     */
    public void undoMove()
    { // just go back, no collision detection
        setPosition(prevX, prevY);
    }

    /**
     * Acting on a collision: move as far as you can The speed will not be
     * changed. If you want to stop at the collision, you will have to set the
     * speed to 0 yourself. You get the parameters from the
     * collisionOccurred-call.
     * 
     * @param horizontal
     *            boolean indicating if the collision is horizontal or vertical
     * @param pos
     *            the collision position (xpos when collision is horizontal,
     *            ypos when vertical)
     */
    public void moveUpto(boolean horizontal, int pos)
    {
        if (horizontal)
        {
            setPosition(pos, getMatchingY(getXFactor(pos)));
            // System.out.println("bounce, x: " + pos + ", y:" + getMatchingY(getXFactor(pos)));
        } else
        {
            setPosition(getMatchingX(getYFactor(pos)), pos);
            // System.out.println("bounce, x: " + getMatchingX(getYFactor(pos)) + ", y:" + pos);
        }
    }

    /**
     * Acting on a collision: bounce Move up to the wall you bounce against and
     * reverse speed! You get the parameters from the collisionOccurred-call.
     * 
     * @param horizontal
     *            boolean indicating if the collision is horizontal or vertical
     * @param pos
     *            the collision position (xpos when collision is horizontal,
     *            ypos when vertical)
     */
    public void bounce(boolean horizontal, int pos)
    {
        moveUpto(horizontal, pos);
        if (horizontal)
        {
            reverseHorizontalDirection();
        } else
        {
            reverseVerticalDirection();
        }
    }

    /**
     * Start moving this item.
     */
    public void startMoving()
    {
        move = true;
    }

    /**
     * Stop moving this item.
     */
    public void stopMoving()
    {
        move = false;
    }

    /**
     * Reverses the horizontal direction of the items movement.
     */
    public final void reverseHorizontalDirection()
    {
        cosdir = -cosdir; // assumption: - is ok for FP-numbers!
    }

    /**
     * Reverses the vertical direction of the items movement.
     */
    public final void reverseVerticalDirection()
    {
        sindir = -sindir; // assumption: - is ok for FP-numbers!
    }

    /**
     * Move a MoveableGameItem.
     * This method is executed with every cycle of the game loop. It moves
     * the GameItem to its new position (according to speed and direction).
     * Note that you have to call startMoving() first, or the Iten won't move at all.
     */
    
    final void move()
    {
        if (move)
        {
            prevX = getX();
            prevY = getY();
            if (speedInPixels > 0)
            {
                speedInPixels = friction * speedInPixels;
            }

            double dx = cosdir * speedInPixels;
            double dy = sindir * speedInPixels;
            setX(getX() + Tools.round(dx));
            setY(getY() + Tools.round(dy));
        }
    }

    /**
     * This method is triggered when an collision between a MoveableGameItem (i.e. the player) and some other gameitem
     * (i.e. a moveable enemy or a static wall) occurs.
     * <p>
     * <b>NOTE: </b> <br/>Just the collision is detected, there is no direction or collision position. Therefore you
     * can't bounce properly.
     * <p>
     * <b>NOTE 2: </b> <br/>JCollisions between items are detected always, that means that invisible items can also 
     * have collisions. 
     * 
     * @param collidedItem
     *              the item that collided with <u>this</u> gameitem. The collidedItem parameter may be any GameItem:
     * 				a non-moving item, a MoveableGameItem or the GamePlayer.
     */
    public void collisionOccured(GameItem collidedItem)
    {
        
    }

    /**
     * This method is triggered when an collision between a MoveableGameItem (i.e. the player) and a tile (a block in
     * the environment map) or row/column of tiles occurs. Please note that only MoveableGameItems can receive this type
     * of events. <p/>The tilePattern parameter tells you what tile types were hit in one time. In the bit pattern bit
     * n (counting from the back) is switched on if a tile of type n has been hit. So if the tilePattern is 9 (binary:
     * 1001), tile types 4 and 1 have been hit. Use the bitwise operators on ints to test. <p/><b>NOTE: </b> <br/>In a
     * single move many tile collisions can occur. Therefore you can receive several calls of this method on just one
     * move. Just use the standard handing methods mentioned below to handle this event. If you want to jump at such a
     * collision, set a Timer for the next turn, and jump then.
     * 
     * @param tilePattern
     *                bit pattern indicating the tile types you have run into.
     * @param horizontal
     *                a boolean indicating if the collision was horizontal.
     * @param position
     *                the position of the collision: x when horizontal, y when vertical You pass these two parameters on
     *                when using bounce(horizontal, position) or moveUpto(horizontal, position).
     * 
     * @see phonegame.MoveableGameItem#undoMove()
     * @see phonegame.MoveableGameItem#bounce(boolean, int)
     * @see phonegame.MoveableGameItem#moveUpto(boolean, int).
     */
    public void collisionOccured(int tilePattern, boolean horizontal, int position)
    {
        
    }
    
    /**
     * This method is executed every time the gameitem moves outside the world.
     * (as defined in the minX, minY, maxX and maxY properties of the GameEngine)
     * <br/>
     * By default, nothing is done. If you want to take action when a GameItem
     * moves outside the world, then you must override this method. 
     * For example, you can remove a bullet that has not hit anything and just
     * flies away. 
     */
    public void outsideWorld()
    {}

}