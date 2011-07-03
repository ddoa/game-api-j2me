/*
 * File: 	GameEngine.java 
 * Created: Jan 17, 2005 
 */
package phonegame;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import phonegame.utils.*;

/**
 * The GameEngine forms the core of the game by controlling the gameloop and the painting operations.
 * All games must extend this base class.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 * 						December 30, 2007 drawing tiles has been sped up
 */

public abstract class GameEngine extends MIDlet implements Runnable
{
    /**
     * maximum frames per second, that is cycles of the game loop.
     * needed to prevent CPU hogging
     */
    private static final int MAX_FPS = 10;

    /**
     * time in milliseconds per cycle of the game loop
     */
    static final int MS_PER_FRAME = 1000 / MAX_FPS;

    /**
     * the display of the phone
     */
    static Display display;

    /**
     * the gamewindow of the game (game will be painted in this window)
     */
    private GameWindow window;

    /**
     * the player. Must be present or game won't respond to key actions.
     */
    private GamePlayer player;

    /**
     * Vector of all gameItems that are active
     * List is ordered: (non-moving) GameItems, then MoveableGameItems, then Player
     * This will ensure right painting order.
     */
    private Vector gameItems;
    
    /**
     * Index of first MovableGameItem in the Vector
     * Non moveable GameItems are inserted at this position, i.e. before the Moveables
     */
    private int firstMovingItemIndex;

    /**
     * Index of Player in the Vector
     * MoveableGameItems are inserted at this position, i.e. before the Player
     */
    private int playerIndex;

    /**
     * Vector of new gameItems
     * New GameItems are kept aside until the end of a cycle, because they
     * would mess up the action if this would happen live. At the end of a cycle
     * they are moved to the Vector of active items
     */
    private Vector newItems;

    /**
     * Vector of all outstanding alarms
     */
    private Vector alarmItems;

    /**
     * Vector of all outstanding alarms
     */
    private Vector stepItems;

    /**
     * Thread for game loop
     */
    private Thread gameLoop;

    /**
     * the flag for interrupt of the game loop (menu, incoming call)
     */
    private boolean gameSuspended = false;

    /**
     * Object handling menu events (if any)
     */
    private IMenuListener menuListener;

    /**
     * 
     */
    private int cycleNumber;

    /**
     * the dashboard (if any)
     */
    private GameDashboard dashboard;

    /**
     * Left edge of game world
     */
    private int minX;

    /**
     * Top edge of game world
     */
    private int minY;
    
    /**
     * Right edge of game world
     */
    private int maxX;
    
    /**
     * Bottom edge of game world
     */
    private int maxY;

    /**
     * The constant for placing the player horizontally centered
     */
    public static final int PLAYER_HCENTER = 1;

    /**
     * The constant for placing the player vertically centered
     */
    public static final int PLAYER_VCENTER = 2;

    /**
     * The constant for placing the player on the left side of the screen
     */
    public static final int PLAYER_LEFT = 4;

     /**
      * The constant for placing the player on the right side of the screen
      */
    public static final int PLAYER_RIGHT = 8;

    /**
     * The constant for placing the player on the top side of the screen
     */
    public static final int PLAYER_TOP = 16;

    /**
     * The constant for placing the player on the bottom side of the screen
     */
    public static final int PLAYER_BOTTOM = 32;

    /**
     * The constant for fixing the viewport, it won't adjust to player
     */
    public static final int PLAYER_FIXED = 64;

    /**
     * Constructs a new GameEngine.
     * 
     */
    public GameEngine()
    {
        // list of GameItems
        gameItems = new Vector();
        newItems = new Vector();
        firstMovingItemIndex = 0;
        playerIndex = 0;
        // initialize gameloop
        gameLoop = null;
        gameSuspended = false;
        cycleNumber = 0;
        // Vectors for Listeners
        alarmItems = new Vector();
        stepItems = new Vector();
        // Window contains the screen
        window = new GameWindow();
        setBounds(0, 0, window.screenWidth, window.screenHeight);
        display = Display.getDisplay(this);
        display.setCurrent(window);
    }

    /**
     * The GameLoop, keeps the game running...
     * Don't call this method yourself! Use startGame() to start the game.
     */
    public void run()
    {
        long cycleStartTime;
        long timeSinceStart;

        // Updates the viewPort for the first time 
        // (use stepcounter to see, so the viewport will also be set when starting a new level
        if ( cycleNumber == 0 )
        {	window.updateViewPortFirstTime();
        }
        while ( Thread.currentThread() == gameLoop )
        { 	// System.out.println("=====STEP: "+cycleNumber);
            cycleStartTime = System.currentTimeMillis();
            try
            {	// execute the various game operations
                // moveItems en playerAction will check tile collisions: on move only!!!
            	moveItems();
            	window.handleKey();
            	detectItemCollisions(); 
            	alarmEvents();
            	stepItems();
            	cleanUpItemList();
    		} catch ( Exception e)
    		{	System.out.println(e.getMessage());
    		    e.printStackTrace();
    		}
            // paint it!
            window.serviceRepaints();
            window.repaint();

            timeSinceStart = ( System.currentTimeMillis() - cycleStartTime);
            //System.out.println("=====used step time in step "+cycleNumber+": "+timeSinceStart);
            //System.out.println("No of items: "+gameItems.size()+" fmgi: " + firstMovingItemIndex+" pi: "+playerIndex);
            if (timeSinceStart < MS_PER_FRAME)
            {
                try
                {
                    // pause thread for the time left..
                    Thread.sleep(MS_PER_FRAME - timeSinceStart);
                } catch (InterruptedException e)
                {}
            }
            cycleNumber++;
        }
    }

    /**
     * Start the game loop, or restart it after a pause.
     * <br/>
     * Note: Game time (stepnumber and alarms) are not affected. 
     */
    public void startGame()
    {	if ( gameLoop == null )
    	{	// cleanUp: activite all items at startup, so the game will start immediately
        	cleanUpItemList();
        	// Create Thread for the game loop
        	gameLoop = new Thread(this);
        	gameLoop.start();
        	// System.out.println("w: "+window.screenWidth+" h: "+window.screenHeight);
    	}
    }

    /**
     * Stop the game loop.
     */
    public void stopGame()
    {
        gameLoop = null;
        // stop key repeats (bug fix: keys appeared out of nowhere after stop-start)
        window.clearKey();
        // also clear gamePaused: if menuAction 'pause' leads to stopGame(),
        // you don't want a stopped game to start again after an incoming call is ended!!
        gameSuspended = false;
    }
    
    /**
     * Suspend a game as a result of MIDP action (incoming call, menu, ...).
     * To be used by system only, game programmers must use stopGame!
     */
    private void suspendGame()
    {
        if ( gameLoop != null )
        {
            stopGame();
            gameSuspended = true;		// after stopGame!
        }
    }
    
    /**
     * Exit the game (and phone emulator, if running on a PC)
     */
    public void exitGame()
    {
        stopGame();
        GameSound.stopBackgroundSound(false);
        notifyDestroyed();
    }
    
    /**
     * Reset the stepcounter and clear all alarms.
     * <br/>
     * Note: Use this method when you start a new level or restart the game. Watch out
     * for the correct order: First reset the GameTime, then add new Items, then restart
     * the game again.
     */
    public void resetGameTime()
    {
        alarmItems.removeAllElements();
        cycleNumber = 0;
    }
    
    /**
     * Set up the game. This method will set the viewport right and make GameItems
     * visible. This will also be done when you start the game. 
     * Use this method only at the end of the constructor of your game,
     * when you don't start the game immediately, for example when you wait for the
     * user to choose 'play' from the menu.
     */
    public void setupGame()
    {
    	cleanUpItemList();
    	window.updateViewPortFirstTime();
    }
    
    /**
     * Get the number of the current step (cycle) in the game loop.
     * 
     * @return The number of the current cycle (first cycle is nr 0)
     */
    public int getStepnr()
    {
        return cycleNumber;
    }

    /**
     * Clean up the list of GameItems and Timers after moves and collisions
     * <ul>
     * <li>remove deleted items, including timers</li>
     * <li>make new items active</li>
     * </ul>
     */
    private final void cleanUpItemList()
    {
        int teller = 0;
        while (teller < gameItems.size())
        {
            GameItem item = (GameItem) gameItems.elementAt(teller);
            if (item.getGiState() == GameItem.DELETED)
            {
                gameItems.removeElement(item);
                deleteTimersForItem(item);
                if ( item instanceof IStepListener )
                {
                    removeStepListener( (IStepListener)item );
                }
                if ( teller < firstMovingItemIndex )
                {	// it was a static item
                    firstMovingItemIndex--;
                }
                if ( teller < playerIndex )
                {	// it was not the player... uhhh this will happen nearly all of the time, but no messing around!
                    playerIndex--;
                }
            } else
            {	// advance teller if item stays in
                teller++;
            }
        }
        // move new items to active list
        int nrnew = newItems.size();					// bloody fool!!: size changes when you delete items!
        for (teller = 0; teller < nrnew; teller++)
        {	// always take the first item, we will delete them from the list as soon as we're done with them
            GameItem it = (GameItem) newItems.elementAt(0);
            if ( it instanceof GamePlayer )
            {	// add to the end, indexes stay the same
                gameItems.addElement(it);
            } else if ( it instanceof MoveableGameItem )
            {	// add before player, increase playerindex
                gameItems.insertElementAt(it, playerIndex);
                playerIndex++;
            } else // non-moving GameItem
            {	// add before first MoveableGameItem, increase both indexes
                gameItems.insertElementAt(it, firstMovingItemIndex);
                playerIndex++;
                firstMovingItemIndex++;
            }
            newItems.removeElementAt(0);
        }
    }

    /**
     * Generates the 'move' events for every moveable item in the game
     */
    private final void moveItems()
    {
        for (int i = firstMovingItemIndex; i < gameItems.size(); i++)
        {  // MoveableGameItem guaranteed, since we start at firstMovingItemIndex in ordered list!
            // if (gameItems.elementAt(i) instanceof MoveableGameItem)
            // {	
                MoveableGameItem it = (MoveableGameItem) gameItems.elementAt(i);
            	if (it.isActive() && it.getSpeed() > 0)
                {
                    it.move();
                    // after move: player move implies viewport update (no side effects)
                    if (it instanceof GamePlayer)
                    {	window.updateViewPort = true;
                    }
                    // check tile collisions on move
                    if ( it.hasCollisionDetection() )
                	{	
                        window.checkForTileCollisions(it);
                	}
                    // check if item has passed world boundaries
                    if (it.getX() >= maxX || it.getX()+it.getFrameWidth() <= minX 
                            || it.getY() >= maxY || it.getY()+it.getFrameHeight() <= minY)
                    {
                        it.outsideWorld();
                    }
                    
                }
            // }
        }
    }
    
    private final void detectItemCollisions()
    {
        for (int i = gameItems.size()-1; i >=firstMovingItemIndex ; i--)
        {  	
            MoveableGameItem it = (MoveableGameItem) gameItems.elementAt(i);
            if (it.isActive() && it.hasCollisionDetection())
            {
                window.checkForItemCollisions(it, i);
            }
        }
    }

    /**
     * Generates the 'step' events for every item in the game
     */
    private final void stepItems()
    {	// NOTE: voor de GameItems onder de steplisteners wordt nu niet gekeken of
        // ze actief zijn. In de beurt dat ze verwijderd worden doen ze hun step dus nog!
        for (int i = 0; i < stepItems.size(); i++)
        {  
            IStepListener it = (IStepListener) stepItems.elementAt(i);
            it.stepAction(cycleNumber);
        }
    }

    /**
     * Checks whether alarm events need to be triggered
     * 
     * Note: alarms for GameItems that have been deleted in this cycle will still be executed!
     */
    private final void alarmEvents()
    {
        if (alarmItems.size() > 0)
        {
            int i = 0;
            while (i < alarmItems.size())
            {
                GameTimer tm = (GameTimer) alarmItems.elementAt(i);
                if (cycleNumber == tm.getEndCycleNumber())
                {
                    IAlarmListener list = tm.getListener();
                    list.alarm(tm.getID());
                    alarmItems.removeElementAt(i); // i will point to next Timer, due to remove
                } else
                {
                    i++; // Timer i stays in, move to next
                }
            }
        } // else
            // cycleNumber = 0; // no overflow of int can occur
    }

    /**
     * Delete all timers of an item
     * 
     * @param item 
     */
    private void deleteTimersForItem(GameItem item)
    {
        if (alarmItems.size() > 0)
        {
            int i = 0;
            while (i < alarmItems.size())
            {
                GameTimer tm = (GameTimer) alarmItems.elementAt(i);
                if (item == tm.getListener())
                {	// i will point to next Timer, due to remove
                    alarmItems.removeElementAt(i); 
                } else
                {	// Timer i stays in, move to next
                    i++; 
                }
            }
        }
    }

    /**
     * Add a timer for a GameItem to the timerlist
     * 
     * @param time
     *             the number of steps that must be taken
     * @param id
     *             the identifier of the timer object. If you have several timers, you can
     * 				give them different id's. The id will be given when the timer rings.
     * @param listener
     *             the object that needs to receive the call. This object must implement
     * 				the IAlarmListener interface.
     */
    public void setTimer(int time, int id, IAlarmListener listener)
    {
        alarmItems.addElement(new GameTimer(cycleNumber + time, id, listener));
    }
    
    /**
     * Add a IStepListener object to the game.
     * 
     * This object will receive a call to it's spepAction method in every cycle of the game loop.
     * 
     * @param listener The IStepListener that must be added
     */
    public void addStepListener( IStepListener listener)
    {
        stepItems.addElement(listener);
    }

    /**
     * Remove an IStepListener object from the List
     * 
     * This object will no longer receive step events
     * 
     * @param listener The IStepListener that must be removed
     */
    public void removeStepListener( IStepListener listener)
    {
        for (int i = 0; i < stepItems.size(); i++)
        {  
            IStepListener it = (IStepListener) stepItems.elementAt(i);
            if ( it == listener )
            {
                stepItems.removeElementAt(i);
                return;
            }
        }
    }

    /**
     * Create a menu with the given menuitem names
     * 
     * @param menuItems
     *                the names that will appear in the phone's menu
     * @param listener
     *                reference to an object that can handle menu events
     * @throws GameException
     *                if the list of menu items is empty
     */
    protected void makeMenu(String[] menuItems, IMenuListener listener) throws GameException
    {
        if ( menuItems.length > 0 )
        {
            menuListener = listener;
            Tools.makeMenu(menuItems, ((CommandListener) window), ((Displayable) window));
        } else
        {
            throw new GameException("Can't create menu, list of menu items is empty.");
        }
    }

    /**
     * Add a player to the game. Note that the game can contain only one player, however you can change from player at
     * any given time.
     * 
     * @param player
     *                reference to an object that represents the player of the game
     */
    protected void addPlayer(GamePlayer player)
    {
        this.player = player;
        newItems.addElement(player); // the player is also a gameitem so add it to the list
        player.setGiState(GameItem.ACTIVE);
        window.setViewportLimits();
    }

    /**
     * Sets the position of the player on the screen. In many games, the game world
     * is bigger than the screen. When you specify the player position, you can make
     * the screen (viewport) move along with the player. The parameter tells how this
     * must be done. For example, you can specify HCENTER or VCENTER, if you want 
     * to keep the player more or less in the middle of the viewport, 
     * or BOTTOM if you want the player in the bottom of the screen. 
     * <br/>
     * Note: if you position the player at one of the edges of the screen, like BOTTOM,
     * this means that you can not move out of the screen that way, but you can move up
     * a bit. How much that is, is specified by setPlayerPositionTolerance.
     * @param pos
     *                one of the following combinations: <br>
     * <ul>
     * <li>
     * <p>PLAYER_FIXED
     * <br/>
     * Viewport won't move. If your world is bigger than the screen, the player can
     * move out of sight on all sides!</p>
     * <li>
     * <p>PLAYER_TOP, PLAYER_VCENTER, PLAYER_BOTTOM
     * <br/>
     * Player is at top, center or bottom of viewport. Horizontal positioning is not
     * controled, you can move out of view at the sides!</p>
     * <li>
     * <p>PLAYER_LEFT, PLAYER_HCENTER, PLAYER_RIGHT
     * <br/>
     * Player is at left, center or right of viewport. Vertical positioning is not
     * controled, you can move out of view at the top or bottom!</p>
     * <li>
     * <p> PLAYER_TOP | PLAYER_LEFT, PLAYER_TOP | PLAYER_HCENTER, PLAYER_TOP | PLAYER_RIGHT
     * <br/>
     * PLAYER_VCENTER | PLAYER_LEFT, PLAYER_VCENTER | PLAYER_HCENTER, PLAYER_VCENTER | PLAYER_RIGHT
     * <br/>
     * PLAYER_BOTTOM | PLAYER_LEFT, PLAYER_BOTTOM | PLAYER_HCENTER, PLAYER_BOTTOM | PLAYER_RIGHT
     * <br/>
     * Combinations of vertical and horizontal positioning. 
     * The following image illustrates this type of positioning: 
     * <br/>
     * <img src="playerpos.jpg"></p>
     * </ul>
     */
    public final void setPlayerPositionOnScreen(int pos)
    {
        window.posInViewPort = pos;
        window.setViewportLimits();
    }
    
    /**
     * Set the tolerance of the positioning. When tolerance is zero, the viewport moves
     * immediately when the player moves. When tolerance is 1, you can move to the edge of
     * the screen before the viewport moves. Values in between result in a smaller or bigger
     * delay before the viewport moves.
     * <br/>
     * Example: In a left-to right platform game, you may position the player at LEFT, VCENTER.
     * If you set the horizontal tolerance at 0.3, you may move to the right 30% of the screen
     * before the viewport moves along. If you set vertical tolerance at 0.8, you can move 80%
     * of the way up, before the viewport moves up also.
     * 
     * @param ht horizontal tolerance, a value between 0 and 1
     * @param vt vertical tolerance, a value between 0 and 1
     */
    public final void setPlayerPositionTolerance(double ht, double vt)
    {
        window.htolerance = ht;
        window.vtolerance = vt;
        window.setViewportLimits();
    }
    /**
     * Add a GameItem to the game.
     * 
     * @param item
     *                the GameItem to be added
     */
    public void addGameItem(GameItem item)
    {
        newItems.addElement(item);
        item.setGiState(GameItem.ACTIVE);
    }

    /**
     * Delete a GameItem from the game.
     * 
     * @param item
     *                the item to remove
     */
    public void deleteGameItem(GameItem item)
    {
        item.setGiState(GameItem.DELETED);
    }

    /**
     * Delete all GameItems from the game, including the player. To be used when
     * you move from a level to the next one.
     */
    public final void deleteAllGameItems()
    {
        for (Enumeration e = gameItems.elements() ; e.hasMoreElements() ;)
        {	// note: loop ok, because delete just changes the state of the item.
            deleteGameItem( (GameItem) e.nextElement() );
        }
    }

    /**
     * Get all instances of the specified type (GameItem) that are currently in the game.
     * 
     * @param type
     *                the classname of the gameitem (without the package name and without .java!)
     * @return a list of gameitems of the specified type
     */
    public final Vector getItemsOfType(String type)
    {
        Vector result = new Vector();
        for (Enumeration e = gameItems.elements() ; e.hasMoreElements() ;)
        {	GameItem gi = (GameItem) e.nextElement();
            // remove package name from the getName() string
            String fullQualifiedName = gi.getClass().getName();
            int lastIndex = fullQualifiedName.lastIndexOf('.');
            String className = fullQualifiedName.substring(lastIndex + 1);

            // check if we found an instance of the given type
            if (className.equals(type))
            {
                result.addElement(gi);
            }
        }
        return result;
    }

    /**
     * Find out if there are any instances of the specified type (GameItem) currently in the game.
     * 
     * @param type
     *                the class name of the gameitem (without the package name and without .java!)
     * @return boolean, true if any items are present, false otherwise.
     */
    public final boolean existItemsOfType(String type)
    {
        for (int i = 0; i < gameItems.size(); i++)
        {
            if ( ((GameItem)gameItems.elementAt(i)).isActive() )
            {	// remove package name from the getName() string
                String fullQualifiedName = gameItems.elementAt(i).getClass().getName();
                int lastIndex = fullQualifiedName.lastIndexOf('.');
                String className = fullQualifiedName.substring(lastIndex + 1);

                // check if we found an instance of the given type
                if (className.equals(type))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the boundaries of this game.
     * If you want the game world to be as big as the screen, you need not use theis method.
     * By default the bounds will be set to the size of the screen. That is: minX=0, 
     * maxX=screenWidth, minY=0, maxY=screenHeight.
     * 
     * @param minX
     *                the minimal horizontal coordinate
     * @param minY
     *                the minimal vertical coordinate
     * @param maxX
     *                the maximal horizontal coordinate
     * @param maxY
     *                the maximal vertical coordinate
     */
    public void setBounds(int minX, int minY, int maxX, int maxY)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    /**
     * Gets left edge of the game world.
     * 
     * @return int x-position of left edge, as specified by setBounds.
     */
    public int getMinX()
    {	return minX;
    }

    /**
     * Gets right edge of the game world.
     * 
     * @return int, x-position of right edge, as specified by setBounds.
     */
    public int getMaxX()
    {	return maxX;
    }

    /**
     * Gets top edge of the game world.
     * 
     * @return int y-position of top edge, as specified by setBounds.
     */
    public int getMinY()
    {	return minY;
    }

    /**
     * Gets bottom edge of the game world.
     * 
     * @return int y-position of bottom edge, as specified by setBounds.
     */
    public int getMaxY()
    {	return maxY;
    }
    
    /**
     * Get random x between left edge of world and right edge minus the specified width.
     * In this way you can find a random position for an item of the given width and the
     * item will fit completely into the world.
     * 
     * @param width
     * @return a random x-position
     */
    public int getRandomX(int width)
    {	return minX+Tools.random(maxX-minX-width);
    }

    /**
     * Get random y between top edge of world and bottom edge minus the specified height.
     * In this way you can find a random position for an item of the given height and the
     * item will fit completely into the world.
     * 
     * @param height
     * @return a random y-position
     */
    public int getRandomY(int height)
    {	return minY+Tools.random(maxY-minY-height);
    }

    /**
     * Set the position of the viewport manually. You must do this if you set the 
     * player position in viewport to FIXED. If it is not fixed, it is no use setting
     * the viewport, because the viewport will adjust to the player position.
     * 
     * @param x xpos of viewport
     * @param y ypos of viewport
     */
    public void setViewPort(int x, int y)
    {	window.viewPortX = x;
        window.viewPortY = y;
    }

    /**
     * Set the images for all tiles that will be used in the game. Do this only once, at
     * start up of the game. If you make a multi-level game, you can create different levels 
     * using the tiles in this list.
     * <br/>
     * All tiles must have equal sizes, to be specified in this method.
     * <br />
     * There is a maximum to the number of tiles you can use. This maximum is equal to the number
     * of bits in an <i>int</i>. This number depends on the platform, but on most devices it is 32.
     * 
     * @param imagePaths
     *                relative path(s) to the square tile images. Please not that the sequence of the images in this array
     *                define the numbers that you can use in your map (so the first image is identified with a 1, the second
     *                with a 2, etc).
     * @param tileWidth
     *                the width of the tile images that are used (usually 16 or 10 pixels)
     * @param tileHeight
     *                the height of the tile images that are used (usually 16 or 10 pixels)
     * @throws GameException
     *                 if the specified images are not found at the given paths
     */
    protected final void setTileImages(String[] imagePaths, int tileWidth, int tileHeight)
    		throws GameException
    {
        window.tileWidth = tileWidth;
        window.tileHeight = tileHeight;
        // load tile images using the given path(s)
        window.tileImages = new Image[imagePaths.length];
        for (int i = 0; i < imagePaths.length; i++)
        {
            try
            {
                window.tileImages[i] = Image.createImage(imagePaths[i]);
            } catch (IOException e)
            {
                throw new GameException("The image " + imagePaths[i] + " could not be found");
            }
        }
    }
    
     /**
     * Create an environment, also known as a tiled map, based on a two dimensional byte array. 
     * This method is especially useful if you want to quickly build a level/map (walls, floor, etc) 
     * for a platform- or boardgame.
     * <br/>
     * The images to be used in the map must have been set earlier, using the method
     * GameEngine.setTileImages(String[] imagePaths, int tileWidth, int tileHeight).
     * 
     * @param map
     *                positions the different tiles in the game. A zero means 'no tile'. A number greater than one
     * 				means that there is a tile, the image will bet taken from the list of images.  The first image 
     * 				is identified with a 1, the second with a 2, etc.
     * @param xPosition
     *                the horizontal start position of the map on the screen
     * @param yPosition
     *                the vertical start position of the map on the screen
     */
    protected final void addEnvironmentMap( byte[][] map, int xPosition, int yPosition) 
    {
        window.mapStartX = xPosition;
        window.mapStartY = yPosition;
        window.map = map;
        window.mapHeight = map.length;				// inits moved from 'drawMap' by Paul
        window.mapWidth = map[0].length;
    }
    
    /**
     * Change a tile in the game world.
     * <p/>Notes<p/>
     * The x and y positions are the indices in the tile-array, 
     * not the x and y positions in the world. 
     * <br/>
     * A zero value for the tilenumber removes the tile at the given position
     * <br/>
     * The tilenumber is a byte value. It must be smaller than 256. You need
     * to use a type cast, when you enter an integer number, like (byte)0
     * 
     * @param xindex the x-pos of the tile in the tile array
     * @param yindex the y-pos of the tile in the tile array
     * @param tilenr the new tile number.
     */
    public final void changeTile(int xindex, int yindex, byte tilenr)
    {
        if (xindex>= 0 && xindex<window.mapWidth &&
            yindex>= 0 && yindex<window.mapHeight &&
            tilenr >=0 && tilenr <=window.tileImages.length)
        {
            window.map[yindex][xindex]=tilenr;
        }
    }
  
    /**
     * Find out what tiles are present in an area at given x,y position, with given width
     * and height. 
     * <p/>
     * Note: to find the tile at a certain point, fill out 1 for width and height. A zero-width area 
     * intersects with nothing!
     * 
     * @param x x-position of the area
     * @param y y-position of the area
     * @param w width of the area
     * @param h height of the area
     * @return a bit pattern indicating the tile numbers that are in the given area.
     * See the collision listener interface to learn m ore about these bit patterns
     */
    public final int findTilesAt(int x, int y, int w, int h)
    {
        return window.findTilesAt(x, y, w, h);
    }

    /**
     * Find the GameItem is present in an area at given x,y position, with given width
     * and height. This method will return the first item it finds. It will return null if there is
     * no GameItem.
     * <p/>
     * Note: to find the GameItem at a certain point, fill out 1 for width and height. 
     * A zero-size area  intersects with nothing!
     *  
     * @param x x-position of the area
     * @param y y-position of the area
     * @param w width of the area
     * @param h height of the area
     * @return the found GameItem, or null, if nothong was found.
     */
    public final GameItem findItemAt(int x, int y, int w, int h)
    {
        for (Enumeration e = gameItems.elements() ; e.hasMoreElements() ;)
        {  GameItem gi = (GameItem) e.nextElement();
        	if ( window.isCollision(x, y, w, h, gi.getX(), gi.getY(), gi.getFrameWidth(), gi.getFrameHeight()) )
        	{
        	    return gi;
        	}
        }
        return null;
    }
    /**
     * Add a game "dashboard" (a.k.a status panel) to the game
     * 
     * @param dash
     *                an instance of GameDashboard
     */
    protected void addGameDashboard(GameDashboard dash)
    {
        dashboard = dash;
    }

    /**
     * Sets the background color of the game
     * 
     * @param red
     *                the red RGB value
     * @param green
     *                the green RGB value
     * @param blue
     *                the blue RGB value
     */
    protected void setBackgroundColor(int red, int green, int blue)
    {
        window.red = red;
        window.green = green;
        window.blue = blue;
    }
    
    /**
     * Clear the background image of the game.
     * <br/>
     * The background will now be the background color, or black 
     * if the background color has not been given.
     */
    public void clearBackgroundImage()
    {
        window.backgroundImage = null;
    }

    /**
     * Set the background image of the game.
     * <p/>
     * An image will be fixed to the viewport, it will not scroll when you move the player.
     * Therefore it is best to use a background image when the world is small and the viewport
     * is fixed.
     * <p>
     * <b>Warning! </b> <br>
     * Only use background images on fast/powerfull phones. Most <i>older </i> phones (e.g. the Siemens MC60 and a
     * SonyEricsson T630 on which we've tested it) have a hard time drawing large background images.
     * 
     * @param imagePath
     *                relative path to the image file
     * @param autoSize
     *                true, if you what the background image to be scaled according to the screensize of the (target) phone.
     *                False if you what the image to maintain it's original size
     * @throws GameException
     *                 if the specified image is not found at the given path
     */
    protected void setBackgroundImage(String imagePath, boolean autoSize) throws GameException
    {
        Image backgroundImage = null;

        // load the image which is defined by the given imagePath
        try
        {
            backgroundImage = Image.createImage(imagePath);
        } catch (IOException e)
        {
            throw new GameException("The image "+imagePath+" could not be found.");
        }

        // check if we need to resize the image
        if (autoSize)
        {
            window.autoSizedBackground = true;
            window.backgroundImage = Tools.resizeImage(backgroundImage, window.screenWidth, window.screenHeight);
        } else
        {
            window.autoSizedBackground = false;
            window.backgroundImage = backgroundImage;
        }
    }

    /**
     * Displays the given GameForm on the screen. <br>
     * <u>Important: </u> this method can also be used to display the (original) gameworld, to achieve this you'll need
     * to set the <tt>form</tt> parameter to <tt>null</tt>.
     * 
     * @param form
     *                the GameForm that needs to be displayed, or <tt>null</tt> if you want to show the gameworld.
     */
    protected void showGameForm(GameForm form)
    {
        if (form != null)
            display.setCurrent(form.getDisplay());
        else
            display.setCurrent(window);
    }

    /**
     * Gets the (drawable) width of the phone's screen.
     * 
     * @return the width in pixels
     */
    public final int getScreenWidth()
    {
        return window.screenWidth;
    }

    /**
     * Gets the (drawable) height of the phone's screen.
     * 
     * @return the height in pixels
     */
    public final int getScreenHeight()
    {
        return window.screenHeight;
    }
    
    /**
     * Executed when the application is started. Don't use this method yourself,
     * it is intended for use by the phone device itself, for example to restart
     * the game after an incoming call. Use startGame() instead.
     * 
     * @see javax.microedition.midlet.MIDlet#startApp()
     */
    protected void startApp() throws MIDletStateChangeException
    {   // restart the game after a pause, only if gamewindow is at front
        // not needed: showNotify() in gameWindow will do all of this
        if ( gameSuspended && window.isShown() )
        {	startGame();
        	gameSuspended = false;
        }
        GameSound.resumeBackgroundSound();
    }

    /**
     * Executed when the application is paused. Don't use this method yourself,
     * it is intended for use by the phone device itself, for example to pause
     * the game for an incoming call. Use stopGame() instead.
     * 
     * @see javax.microedition.midlet.MIDlet#pauseApp()
     */
    protected void pauseApp()
    {	
        suspendGame();
        GameSound.stopBackgroundSound(true);
    }

    /**
     * Executed when the application is terminated by the MIDP device.
     * Don't use this method yourself,
     * it is intended for use by the phone device itself. 
     * Use exitGame() instead.
     * 
     * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
     */
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException
    {
        stopGame();
        GameSound.stopBackgroundSound(false);
    }

    /**
     * The GameWindow is a vital part of the engine. It performs the drawing of the screen 
     * and receives the incomming key events.
     * 
     * @author Tim Prijn & Richard Kettelerij
     * @version 1.0
     */
    private class GameWindow extends Canvas implements CommandListener
    {
        /* attributes that are only used in the GameWindow class */
        private static final byte NO_TILE = 0;

        private int viewPortX, viewPortY;

        private int screenWidth, screenHeight;

        private Image offScreenBuffer; // double buffering

        
        /**
         * Boolean <code>newKey</code> indicates if a key has been pressed 
         * that has not yet been handled.
         */
        private boolean newKey;
        
        /**
         * Boolean <code>keyHeld</code> indicates if a key is stell pressed.
         * <br/>
         * GameEngine will perform an action in each cycle of the game loop
         * if either newKey or keyHeld is true, that is: if there is an unhandled
         * keystroke or the already handled key is steill pressed.
         */
        private boolean keyHeld;
        
        /**
         * The <code>gameAction</code> that is associated with the pressed key
         */
        private int spelAction;

        /* attributes that are filled in the GameRunner */
        private Image[] tileImages;

        private Image backgroundImage;

        private int red = 0;
        
        private int green = 0;
        
        private int blue = 0;

        private int tileWidth, tileHeight;

        private int mapStartX, mapStartY;

        private int mapWidth, mapHeight;

        private byte[][] map;

        private boolean autoSizedBackground = false;

        private boolean updateViewPort = false;

        /**
         * The current setting for the viewport/player position
         */
        private int posInViewPort = 0;
        
        private int leftLimit;	
        
        private int rightLimit;

        private int bottomLimit;
        
        private int topLimit;
        
        private double vtolerance = 0.66;

        private double htolerance = 0.66;

        /**
         * Constructs an instance of this class
         */
        public GameWindow()
        {
            // set screen sizes based on canvas
            screenHeight = getHeight();
            screenWidth = getWidth();

            // create offscreen buffer (for doublebuffering)
            offScreenBuffer = Image.createImage(getWidth(), getHeight());
        }

        /**
         * suspend game when a menu or form is put on top of the gamewindow
         * 
         * @see javax.microedition.lcdui.Canvas#hideNotify()
         */
        protected void hideNotify()
        {
            suspendGame();
        }
        
        /**
         * resume game after gamewindow is put back up front on the screen, especially
         * when a menu is removed from the screen.
         * 
         * @see javax.microedition.lcdui.Canvas#showNotify()
         */
        protected void showNotify()
        {
            if ( gameSuspended )
            {	startGame();
            	gameSuspended = false;
            }
        }
        
        /**
         * Execute the requested key operation
         * 
         * @param gameAction
         *                the requested action
         */
		private void performAction(int gameAction) {
			switch (gameAction)
			{
			case UP:
				player.moveUp();
				break;
			case DOWN:
				player.moveDown();
				break;
			case LEFT:
				player.moveLeft();
				break;
			case RIGHT:
				player.moveRight();
				break;
			case FIRE:
				player.fire();
				break;
			case GAME_A:
				player.pressedButtonA();
				break;
			case GAME_B:
				player.pressedButtonB();
				break;
			case GAME_C:
				player.pressedButtonC();
				break;
			case GAME_D:
				player.pressedButtonD();
				break;
			}
			if (player.getMoved())
			{
				updateViewPort = true;
				// System.out.println("Player moved, detect collisions");
				// window.checkForItemCollisions(player);
				if (player.hasCollisionDetection())
				{
					window.checkForTileCollisions(player);
				}
				player.clearMoved();
			}
		}

        /**
         * Is called when a key is pressed. 
         * Switches both booleans on, game loop will execute event.
         * <br/>
         * NOTE: This way of handling keys ensures synchronisation with the game loop,
         * important for collision detection, but it has a drawback: if a player manages
         * to hit two different keys in one cycle, only the last key will be handled...
         * 
         * @param keyCode
         *                represents the pressed key
         */
        protected void keyPressed(int keyCode)
        {
            spelAction = getGameAction(keyCode);
            newKey = true;
            keyHeld = true;
        }

        /**
         * A call to this method is made when a key is released. 
         * Switches keyHeld boolean off
         */
        protected void keyReleased(int keyCode)
        {
            keyHeld = false;
        }
        
        /**
         * Clears the key-booleans. Method stopGame() calls this to make sure
         * there will be no key events after resume game.
         */
        private void clearKey()
        {
            keyHeld = false;
            newKey = false;
        }
        
        private void handleKey() throws GameException
        {
            if (player != null)
            {
            	player.setKeyVars( (newKey || keyHeld), (keyHeld && !newKey));
            	if ( newKey || keyHeld )
            	{
            		performAction(spelAction);
            		newKey = false;
            	}
            } else
            {
                throw new GameException("No GamePlayer present, cannot respond to keyboard events");
            }
        }

        /**
         * Redirect command event to the (more user-friendly) menuListener
         * 
         * @see javax.microedition.lcdui.CommandListener#commandAction( javax.microedition.lcdui.Command,
         *         javax.microedition.lcdui.Displayable)
         */
        public void commandAction(Command cmd, Displayable display)
        {
            menuListener.menuAction(cmd.getLabel());
        }

        /**
         * Render the evironment map (a.k.a tiledmap) on the canvas
         * 
         * @param g
         *                the canvas it's graphics object
         */
        private void drawTileEnvironment(Graphics g)
        {
            if (map != null && tileImages != null)
            {
                int tileType;
                // calculate horizontal index of first tile to be drawn. not below zero!
                int firstXindex = Math.max(divdown(viewPortX-mapStartX,tileWidth),0);
                // calculate last index of tile to be drawn in horizontal direction, stay inside map!
                int lastXindex = Math.min(divdown(viewPortX+screenWidth-mapStartX,tileWidth)+1, mapWidth);
                // likewise in vertical direction
                int firstYindex = Math.max(divdown(viewPortY-mapStartY,tileHeight),0);
                int lastYindex = Math.min(divdown(viewPortY+screenHeight-mapStartY,tileHeight)+1, mapHeight);
                // screenpos of first tile to be drawn
                int yPos = mapStartY + tileHeight*firstYindex;
                int xPos; // inside loop

                for (int Yindex = firstYindex; Yindex < lastYindex; Yindex++)
                {
                    xPos = mapStartX + tileWidth*firstXindex;
                    for (int Xindex = firstXindex; Xindex < lastXindex; Xindex++)
                    {
                        tileType = map[Yindex][Xindex];
                        if (tileType != NO_TILE)
                        {
                            g.drawImage(tileImages[tileType - 1], xPos - viewPortX, yPos - viewPortY, Graphics.TOP
                                    | Graphics.LEFT);
                        }
                        xPos += tileWidth;
                    }
                    yPos += tileHeight;
                }
            }
        }

        /**
         * Draw a background image (if not equal to <tt>null</tt>) or a fixed color on the canvas.
         * 
         * @param g
         *                the canvas it's graphics object
         */
        private void drawBackground(Graphics g)
        {
            // check if we need to set a image or a fixed color on the
            // background
            if (!autoSizedBackground || backgroundImage == null )
            {
                g.setColor(red, green, blue);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            if (backgroundImage != null)
            {
                g.drawImage(backgroundImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            }
        }

        /**
         * Check if the given gameitem lies withi the viewport
         * 
         * @param item
         *                the gameitem that needs to be checked
         * @return true, if it lies in the viewport, false otherwise
         */
        private final boolean isInViewPort(GameItem item)
        {
            return item.getX() + item.getFrameWidth() > viewPortX && item.getY() + item.getFrameHeight() > viewPortY
                    && item.getX() < viewPortX + screenWidth && item.getY() < viewPortY + screenHeight;
        }
        
        /**
         * This method paints the all graphics onto the screen, offScreenGraphics is used for double buffering (just in
         * case).
         * 
         * @param g
         *                the canvas it's graphics object
         */
        public void paint(Graphics g)
        {
            // request offscreen buffer
            Graphics offScreenGraphics = offScreenBuffer.getGraphics();
            // draw the background color/image
            drawBackground(offScreenGraphics);
            // Set viewport coordinates according to player
            updateViewPort();
            // draw the visible parts of tile environment
            drawTileEnvironment(offScreenGraphics);
            
            // NOTE: order of paint: static items, moveable items, player. 
            // paint game items, 
            for (Enumeration e = gameItems.elements() ; e.hasMoreElements() ;)
            {   GameItem gameItem = (GameItem) e.nextElement();
               	// Make sure only items within the viewport are drawn others can't be seen anyway
            	if (isInViewPort(gameItem))
                {
                	gameItem.animate();
                	gameItem.paint(offScreenGraphics, viewPortX, viewPortY);
                }
            }
            // draws the (optional) dashboard
            if (dashboard != null)
                dashboard.paint(offScreenGraphics);

            // put the contents of the offscreen buffer to screen
            g.drawImage(offScreenBuffer, 0, 0, Graphics.TOP | Graphics.LEFT);
        }

        /**
         * This method sets the viewPort for the first time.
         */
        private void updateViewPortFirstTime()
        {
            // if FIXED, do nothing. NOTE: this in fact is not necessary. If no positioning is specified,
            // none of the adjustments below will be executed!
            if ( posInViewPort == PLAYER_FIXED )
                return;
            
            // Adjust y-pos of viewport
            if ( (posInViewPort & PLAYER_TOP) != 0 )
            {	viewPortY = player.getY();
            } else if ( (posInViewPort & PLAYER_VCENTER) != 0 )
            {	viewPortY = player.getY() - (screenHeight - player.getFrameHeight()) / 2;
            } else if ( (posInViewPort & PLAYER_BOTTOM) != 0 )
            {	viewPortY = player.getY() - (screenHeight - player.getFrameHeight());
            } // no default: user must take care of viewport if no positioning is given

            // Adjust x-pos of viewport
            if ( (posInViewPort & PLAYER_LEFT) != 0 )
            {	viewPortX = player.getX();
            } else if ( (posInViewPort & PLAYER_HCENTER) != 0 )
            {	viewPortX = player.getX() - (screenWidth - player.getFrameWidth()) / 2;
            } else if ( (posInViewPort & PLAYER_RIGHT) != 0 )
            {	viewPortX = player.getX() - (screenWidth - player.getFrameWidth());
            } // no default: user must take care of viewport if no positioning is given
            updateViewPort = true;
        }
        
        private final void setViewportLimits()
        {
            /* min and max values for viewportx are calculated as follows (for y similarly)
             * Distance from player to viewport edge in the ideal position equals
             * screenwidth-playerwidth, divided by 2 when player is centered. Call this d.
             * The minimum distance between player and screen edge will be:
             * (1-tolerance) * d. When tolerance == 0, the full d must be this distance,
             * so vieuwport will move immediately, When tolerance == 1, viewport moves
             * only when player crosses the edge, because the distance will be zero.
             * 
             * Limits are represented in such a way, that playerx-leftLimit is a maximum
             * for viewportx (to be enforced by Math.min!!), and playerx-rightLimit is a
             * minimum
             */
            if ( player == null )
                return;		// no player, no positioning!
            double dist = (1-vtolerance) * (screenHeight - player.getFrameHeight());
            if ( (posInViewPort & PLAYER_TOP) != 0 )
            {	topLimit = 0;
            	bottomLimit = player.getFrameHeight() + (int)dist - screenHeight;		// 1e 2 termen max for bottom edge
            } else if ( (posInViewPort & PLAYER_VCENTER) != 0 )
            {	topLimit = -(int)(dist/2);
            	bottomLimit = player.getFrameHeight() + (int)(dist/2) - screenHeight;
            } else if ( (posInViewPort & PLAYER_BOTTOM) != 0 )
            {	topLimit = -(int)dist;
            	bottomLimit = player.getFrameHeight() - screenHeight;
            }
            dist = (1-htolerance) * (screenWidth - player.getFrameWidth());
            if ( (posInViewPort & PLAYER_LEFT) != 0 )
            {	leftLimit = 0;
            	rightLimit = player.getFrameWidth() + (int)dist - screenWidth;		// 1e 2 termen max for right edge
            } else if ( (posInViewPort & PLAYER_HCENTER) != 0 )
            {	leftLimit = -(int)(dist/2);
            	rightLimit = player.getFrameWidth() + (int)(dist/2) - screenWidth;
           } else if ( (posInViewPort & PLAYER_RIGHT) != 0 )
            {	leftLimit = -(int)dist;
            	rightLimit = player.getFrameWidth() - screenWidth;
            }
            //System.out.println("position: "+posInViewPort+" sw: "+screenWidth+" sh: "+screenHeight);
            //System.out.println("left: "+leftLimit+" right: "+rightLimit+" top: "+topLimit+" bot: "+bottomLimit);
        }

        /**
         * This method centers the player in the middle of the viewport Note: Now it centers but there are all kinds of
         * ways to position the player!
         */
        private void updateViewPort()
        {
            /*
             * viewport x not bigger than player x + leftLimit
             * viewport x not smaller than player x + rightLimit
             * likewise vertical
             */

            // If the player has not moved then the viewport does not need to be updated
            if (!updateViewPort)
                return;            
            // Adjust y-pos of viewport
            if ( (posInViewPort & PLAYER_TOP+PLAYER_VCENTER+PLAYER_BOTTOM) != 0 )		
            {  	viewPortY = Math.max(Math.min(viewPortY, player.getY()+topLimit), player.getY() + bottomLimit);
            }	// else do nothing: user didn't specify y-positioning         
            // Adjust x-pos of viewport
            if ( (posInViewPort & PLAYER_LEFT+PLAYER_HCENTER+PLAYER_RIGHT) != 0 )		
            {  	viewPortX = Math.max(Math.min(viewPortX, player.getX()+leftLimit), player.getX() + rightLimit);
            } // else do nothing: user didn't specify x-positioning
            
            // viewport will stay completely within world bounds (though player may not!)
            viewPortX = Math.max(minX, Math.min(viewPortX, maxX-screenWidth));
            viewPortY = Math.max(minY, Math.min(viewPortY, maxY-screenHeight));
            
            // clear indicator
            updateViewPort = false;
        }
       
        /**
         * Determines if two objects collide with each other (using rectangle collision detection)
         * 
         * @param aX
         *                the horizontal position of item A
         * @param aY
         *                the vertical position of item A
         * @param aW
         *                the width of item A
         * @param aH
         *                the height of item A
         * @param bX
         *                the horizontal position of item B
         * @param bY
         *                the vertical position of item B
         * @param bW
         *                the width of item B
         * @param bH
         *                the height of item B
         */
        private final boolean isCollision(int aX, int aY, int aW, int aH, int bX, int bY, int bW, int bH)
        {
            if ( bY + bH <= aY ) 		// Is the bottom of b above the top of a
                return false;
            if ( bY >= aY + aH ) 		// Is the top of b below bottom of a
                return false;
         	if ( bX + bW <= aX ) 	// Is the right of b to the left of a
              	return false;
        	if ( bX >= aX + aW ) 	// Is the left of b to the right of a
                return false;
        	// remaining: hit:
            return true;
        }


        /**
         * Check if a moveable gameitem collides with another (movable or static) gameitem
         * Only collisions with items that are before the currentItem in the Items Vector will
         * be ckecked. In this way there will be no double checking, only a-b, not b-a.
         * 
         * @param gameItem
         *                the item that needs to be check for collisions
         * @param limit
         * 			the index of currentItem in the GameItems Vector
         */
        private final void checkForItemCollisions(MoveableGameItem currentItem, int limit)
        {
            // loop through all other (static and moveable) gameitems that are alive
            for (int i = 0; i < limit; i++)
            {
                GameItem anotherItem = (GameItem) gameItems.elementAt(i);
                if ( anotherItem.isActive() )
                {
                    // check if there is a collision between the current moveable
                    // item and another gameitem
                    if (	isCollision(currentItem.getX(), currentItem.getY(), 
                            currentItem.getFrameWidth(), currentItem.getFrameHeight(), 
                            anotherItem.getX(), anotherItem.getY(), 
                            anotherItem.getFrameWidth(), anotherItem.getFrameHeight())
                       )
                    {
                        // notify both game item of the collision that has occured
                        currentItem.collisionOccured(anotherItem);
                        if ( anotherItem instanceof MoveableGameItem )
                        {
                        	((MoveableGameItem)anotherItem).collisionOccured(currentItem);
                        }
                    }
                }
            }
        }

        /**
         * This method solves the problem that (int) (a/b)*b works as a round down to multiples of b for positive ints
         * a, and as a round up for negative ints. Sorry for the unusual variable names. They arose out of frustration.
         * 
         * @param blaat
         *                a number
         * @param deler
         *                the divisor
         * @return blaat/deler, one down if blaat is negative (i.e. divdown( -6, 10) yields -1)
         */
        private int divdown(int blaat, int deler)
        {
            int schaap = blaat/deler;
            if (blaat<0) schaap--;
            return schaap;
        }
        
        /**
         * Handle a horizontal collision.
         * Determines the yrange of tiles (column) at a given xindex that an object collides into
         * and assembles the bitpattern of tiletypes. 
         * If the pattern is not zero, the collisionOccurred-method will be called on gameItem.
         * 
         * @param gameItem the MoveableGameItem involved
         * @param xindex the x coordinate of the column of tiles
         * @param xf the part of the move thas has been done upto the collision
         * @param xc the x-coordinate of the gameItem at the moment of collision
         */
        private void handleHorizontalCollision(MoveableGameItem gameItem, int xindex, double xf, int xc)
        {	// no collision if xindex outside map
            if ( (xindex < 0) || (xindex >= mapWidth) )
                return;
            // initialize tilepattern at zero
            int collisionPattern = 0;
            // find y that corresponds to the part of the x-displacement until collision
            int ypos = gameItem.getMatchingY(xf);
            // find yindex of tile where top pixel is 
            // limit to range of map. Note: range of xindex has alresdy been checked
            int firsttile = Math.max(0, divdown(ypos-mapStartY, tileHeight));
            // find yindex of tile where lowest pixel is, that is at y+h-1
            int lasttile = Math.min(mapHeight-1, divdown(ypos + gameItem.getFrameHeight()-1-mapStartY, tileHeight));
            // loop through y-range
            for (int yindex = firsttile; yindex <=lasttile; yindex++)
            {	// see if there is a tile at the current position
                if (map[yindex][xindex] != NO_TILE)
                { 	// switch bit of this tile on in pattern
                 	collisionPattern = collisionPattern | (1 << (map[yindex][xindex] - 1));
                }
            }
            if (collisionPattern != 0)
            { 	// notify the current moveable game item of the collision that has occured
                // System.out.println("####colX, pat: " + collisionPattern + ", pos:" + xc);
                gameItem.collisionOccured(collisionPattern, true, xc);
            } //  else
                // System.out.println("####colX, no tiles, pos:" + xc);

        }

        /**
         * Handle a vertical collision.
         * Determines the xrange of tiles (row) at a given yindex that an object collides into
         * and assembles the bitpattern of tiletypes. 
         * If the pattern is not zero, the collisionOccurred-method will be called on gameItem.
         * 
         * @param gameItem the MoveableGameItem involved
         * @param yindex the y coordinate of the row of tiles
         * @param yf the part of the move thas has been done upto the collision
         * @param yc the y-coordinate of the gameItem at the moment of collision
         */
         private void handleVerticalCollision(MoveableGameItem gameItem, int yindex, double yf, int yc)
         {	// comments like handleHorizontalCollision
            if ( (yindex < 0) || (yindex >= mapHeight) )
                return;
            int collisionPattern = 0;
            int xpos = gameItem.getMatchingX(yf);
            int firsttile = Math.max(0, divdown(xpos-mapStartX, tileWidth));
            int lasttile = Math.min(mapWidth-1, divdown(xpos + gameItem.getFrameWidth()-1-mapStartX, tileWidth));
            for (int xindex = firsttile; xindex <= lasttile; xindex++)
            {
                if (map[yindex][xindex] != NO_TILE)
              	{ 
                  	collisionPattern = collisionPattern | (1 << (map[yindex][xindex] - 1));
                }
            }
            if (collisionPattern != 0)
            { 	// notify the current moveable game item of the collision that has occured
                // System.out.println("####colY, pat: " + collisionPattern + ", pos:" + yc);
                gameItem.collisionOccured(collisionPattern, false, yc);
            } // else
                // System.out.println("####colY, no tiles, pos:" + yc);

        }

        /**
         * Check if the given moveable gameitem collides with a tile (or multiple tiles) 
         * pre: MoveableGameItem implements CollisionListener
         * 
         * @param gameItem
         *                the item that needs to be checked for collisions
         */
        private final void checkForTileCollisions(MoveableGameItem gameItem)
        { 	// 
            if (map == null ) 
                return;
            int xTileIndex; // index of column of collided tiles (horizontal collision)
            int yTileIndex; // index of row of collided tiles (vertical collision)
            int collisionX; // Xpos of possible collision (gridline on tile grid)
            int collisionY; // Ypos of possible collision (gridline on tile grid)
            int itemXatCollision; // xpos of item at collision ( =collisionX, -width if collision on right side)
            int itemYatCollision; // ypos of item at collision ( =collisionY, -height if collision at bottom)
            double xFactor; // part of move that is done up to collision
            double yFactor; // part of move that is done up to collision
            boolean moveleft = gameItem.movesLeft();
            boolean moveup = gameItem.movesUp();
            // System.out.println("--------col1, prevx: " + gameItem.getPrevX() + ", y:" + gameItem.getPrevY());
            // System.out.println("col2, left: " + moveleft + ", up:" + moveup);
            // System.out.println("col3, new x: " + gameItem.getX() + ", y:" + gameItem.getY());

            // 1: Find gridlines ( x and y ) where collision occurs (if any).
            // 		Determine corresponding indexes in tilemap
            if (moveleft) // horizontal collision??
            { 	// find index of gridline just left of previous left side of item
                // -1: entire tile left of previous pos of object, we collide into right side
                xTileIndex = divdown(gameItem.getPrevX() - mapStartX, tileWidth) - 1;
                // x of collision is right side of tile (hence '+1')
                collisionX = (xTileIndex + 1) * tileWidth + mapStartX;
                // x of item equals collisionX because collision is on left side
                itemXatCollision = collisionX;
                // possible collision if current x of item is left of collisionX 
            } else
            { 	// find index of gridline just right of previous right side of item
                xTileIndex = divdown(gameItem.getPrevX() + gameItem.getFrameWidth() - 1 - mapStartX, tileWidth) + 1;
                // x of collision is left side of tile
                collisionX = xTileIndex * tileWidth + mapStartX;
                // x of item equals collisionX-width because collision is on right side
                itemXatCollision = collisionX - gameItem.getFrameWidth();
                // possible collision if current right side of item is right of collisionX
            }
            // System.out.println("col4, hor? xtile:" + xTileIndex + ", colX:" + collisionX + ", itX:" + itemXatCollision);
            if (moveup) // vertical collision?? (comments like hor)
            {
                yTileIndex = divdown(gameItem.getPrevY() - mapStartY, tileHeight) - 1;
                collisionY = (yTileIndex + 1) * tileHeight + mapStartY;
                itemYatCollision = collisionY;
            } else
            {
                yTileIndex = divdown(gameItem.getPrevY() + gameItem.getFrameHeight() - 1 - mapStartY, tileHeight) + 1;
                collisionY = yTileIndex * tileHeight + mapStartY;
                itemYatCollision = collisionY - gameItem.getFrameHeight();
            }
            // System.out.println("col5, ver?: ytile:" + yTileIndex + ", colY:" + collisionY+ ", itY:" + itemYatCollision);
            // calculate the part of move that has been done until the collision: (colx - prevx)/(newx - prevx)
            // Note: if factor >=1, the collision position is further away than the move. Therefore it has not
            // been reached and ther is no collision. This property will be used as a collision test.
            xFactor = gameItem.getXFactor(itemXatCollision);
            yFactor = gameItem.getYFactor(itemYatCollision);
            // System.out.println("col6, xf: " + MathFloat.toString(xFactor, 2, 2) + ", yf: "
            //        + MathFloat.toString(yFactor, 2, 2));
            while ( xFactor < 1 || yFactor < 1 )
            {	// handle collision that comes first, that is the lower factor (<1 guaranteed by loop criterion)
               	if (xFactor <= yFactor)
              	{ 	// horizontal collision first
                   	handleHorizontalCollision(gameItem, xTileIndex, xFactor, itemXatCollision);
                    if ( moveleft )
                    {	// move collision gridline to the left for next check
                        xTileIndex--;
                        collisionX = (xTileIndex + 1) * tileWidth + mapStartX;
                        itemXatCollision = collisionX;
                    } else
                    {	// move collision gridline to the right for next check
                        xTileIndex++;
                        collisionX = xTileIndex * tileWidth + mapStartX;
                        itemXatCollision = collisionX - gameItem.getFrameWidth();
                    }
                } else			
                {	// vertical collision first
                    handleVerticalCollision(gameItem, yTileIndex, yFactor, itemYatCollision);
                    // check if there is still a horizontal collision
                    if ( moveup )
                    {	// move collision gridline up for next check
                        yTileIndex--;
                        collisionY = (yTileIndex + 1) * tileHeight + mapStartY;
                        itemYatCollision = collisionY;
                    } else
                    {	// move collision gridline down for next check
                        yTileIndex++;
                        collisionY = yTileIndex * tileHeight + mapStartY;
                        itemYatCollision = collisionY - gameItem.getFrameHeight();
                    }
                }
               	// new xpos and/or ypos may have been changed by the collision handler (undoMove, etc)
               	// Therefore we have to check again if there is a collision, that is: recalculate factors.
                xFactor = gameItem.getXFactor(itemXatCollision);
                yFactor = gameItem.getYFactor(itemYatCollision);
                // System.out.println("col6, xf: " + MathFloat.toString(xFactor, 2, 2) + ", yf: "
                //        + MathFloat.toString(yFactor, 2, 2));
            }
        }
    
        private int findTilesAt(int x, int y, int w, int h)
        {	// this is handle horizontal & vertical collisions combined, 
            // see handleHorizontalCollisions for comments
            if (map == null ) 
                return 0;
            int collisionPattern = 0;
            int ystart = Math.max(divdown(y-mapStartY, tileHeight), 0);
            int yend = Math.min(divdown(y+h-1-mapStartY, tileHeight), mapHeight-1);
            int xstart = Math.max(divdown(x-mapStartX, tileWidth), 0);
            int xend = Math.min(divdown(x+w-1-mapStartX, tileWidth), mapWidth-1);
            if ( xstart <= xend && ystart<=yend)
            {	for (int tileY = ystart; tileY <= yend; tileY++)
            	{	for (int tileX = xstart; tileX <= xend; tileX++)
                	{	
                	    if (map[tileY][tileX] != NO_TILE)
                        { // switch bit of this tile on in pattern
                	        collisionPattern = collisionPattern | (1 << (map[tileY][tileX] - 1));
                     	}
                	}
                }
            }
            return collisionPattern;
        }
    }

    /**
     * The GameTimer handles the so-called 'alarm events' inside the game.
     * 
     * @author Tim Prijn & Richard Kettelerij
     * @version 1.0
     */
    private class GameTimer
    {
        private int alarmSetNumber;

        private int id;

        private IAlarmListener parent;

        /**
         * Constucts an instance of this class
         * 
         * @param time
         * @param id
         * @param listener
         */
        public GameTimer(int time, int id, IAlarmListener listener)
        {
            this.alarmSetNumber = time;
            this.id = id;
            this.parent = listener;
        }

        /**
         * Returns the number of steps at which a requested alarm must be set
         * 
         * @return int
         */
        public int getEndCycleNumber()
        {
            return alarmSetNumber;
        }

        public IAlarmListener getListener()
        {
            return parent;
        }

        public int getID()
        {
            return id;
        }
    }
}