/*
 * File: 	GameItem.java
 * Created: Jan 17, 2005
 */
package phonegame;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * class GameItem represents an actor in the game that is not moving.
 * GameItem is an abstract class. You have to make concrete classes
 * yourself, by extending the GameItem class.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */

public abstract class GameItem
{
    /**
     * The state of this item. State can (currently) be ACTIVE or DELETED.
     * The state is used by the GameEngine, no need to do anything with it yourself
     */
    private int giState;

    /**
     * The state for active items, items that are not DELETED.
     */
    public static final int ACTIVE = 2;

    /**
     * The state of deleted items. Items with this state will be automatically removed
     * at the end of the cycle.
     */
    public static final int DELETED = 3;

    /**
     * The image of this item.
     * An image may consist of a strip of frames. You can select a frame for display,
     * or play the frames as an animation.
     */
    private Image image = null;

    /**
     * The number of the frame that is currently on display
     */
    private int currentFrame; 
    
    /**
     * The number of frames in the image
     */
    private int nrOfFrames;

    /**
     * Width of the frame
     */
    private int frameWidth = 0; 
    
    /**
     * Height of the frame
     */
    private int frameHeight = 0;

    /**
     * Current x-position of this item.
     */
    private int xPosition = 0;
    
    /**
     * Current y-position of this item.
     */
    private int yPosition = 0;

    /**
     * boolean indicating if this item is visible.
     */
    private boolean visible = true;

    /**
     * Get the state of the GameItem.
     * 
     * @return state, ACTIVE or DELETED
     */
    public final int getGiState()
    {
        return giState;
    }

    /**
     * Check if item is active, that is: not deleted.
     * 
     * @return boolean, true if item is active.
     */
    public final boolean isActive()
    {
        return giState == ACTIVE;
    }

    /**
     * sets the state of the item.
     * To be used by gamepackage only!
     * 
     * @param s, the new state
     */
    final void setGiState(int s)
    {
        giState = s;
    }

    /**
     * Define a fixed image that can be used to display this item.
     * The image has just one frame.
     * 
     * @param path
     *                relative location to the image file
     * @throws GameException
     *                 if the specified image is not found at the given path
     */
    public void setImage(String path) throws GameException
    {
        try
        {
            image = Image.createImage(path);
            setFrame(0);
        } catch (IOException e)
        {
            throw new GameException("The image "+path+" could not be found.");
        }

        // set image dimensions
        frameWidth = image.getWidth();
        frameHeight = image.getHeight();
        nrOfFrames = 1;
    }

    /**
     * Define a animating image that can be used to display this item.
     * <p/>
     * Please note that this method can only handle horizontal filmstrips. 
     * This means that the frames in the image are to be placed horizontally 
     * next to each other, and not stacked like in a vertical filmstrip.
     * <p/>
     * By default the frames will be played as an animation when displaying
     * the item. Override the method animate() to do something else.
     * 
     * @param path
     *                relative location to the image file
     * @param frameWidth
     *                the width a single frame in the animation/filmstrip
     * @param frameHeight
     *                the height a single frame in the animation/filmstrip
     * @throws GameException
     *                 if the specified image is not found at the given path
     */
    public void setImage(String path, int frameWidth, int frameHeight) throws GameException
    {
        try
        {
            image = Image.createImage(path);
            setFrame(0);
        } catch (IOException e)
        {
            throw new GameException("The image "+path+" could not be found.");
        }

        // set image dimensions
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        nrOfFrames = image.getWidth() / frameWidth;
    }

    /**
     * Returns the image of this item.
     * 
     * @return a image object or <tt>null</tt> if there isn't one
     */
    public Image getImage()
    {
        return image;
    }

    /**
     * The width of one frame in case of an animating image or the width of the complete image 
     * in case of a static image.
     * 
     * @return an integer containing the width
     */
    public int getFrameWidth()
    {
        return frameWidth;
    }

    /**
     * The height of one frame of the item's image. This equals the height of the complete image 
     * because frame strips are horizontal.
     * 
     * @return an integer containing the height
     */
    public int getFrameHeight()
    {
        return frameHeight;
    }

    /**
     * Selects the next frame in the frame sequence for drawing. 
     * The frame list is considered to be circular, i.e. if nextFrame() is called at the 
     * end of the list, this method will advance to the first frame in the list.
     */
    public void nextFrame()
    {
        if (currentFrame < nrOfFrames - 1)
            currentFrame++;
        else
            currentFrame = 0;
    }

    /**
     * Selects the previous frame in the frame sequence. 
     * The frame list is considered to be circular, i.e. if prevFrame() is called at the 
     * start of the list, this method will advance to the last frame in the list.
     */
    public void prevFrame()
    {
        if (currentFrame > 0)
            currentFrame--;
        else
            currentFrame = nrOfFrames - 1;
    }

    /**
     * Selects the frame with the given index in the frame list. 
     * The GameItem will be painted with this frame.
     * 
     * @param index
     *                the index of of the desired entry in the frame sequence
     */
    public void setFrame(int index)
    {
        currentFrame = index;
    }

    /**
     * Gets the current index in the frame sequence.
     * 
     * @return the index of of the current frame
     */
    public int getFrame()
    {
        return currentFrame;
    }

    /**
     * Gets the number of frames in the image.
     * 
     * @return an integer containing the number of frames or 0 if no image is provided
     */
    public int getFrameCount()
    {
        return nrOfFrames;
    }

    /**
     * Sets the position of the item inside the game. 
     * There will be no collision detection.
     * 
     * @param x
     *                the horizontal position
     * @param y
     *                the vertical position
     */
    public void setPosition(int x, int y)
    {
        xPosition = x;
        yPosition = y;
    }

    /**
     * Sets the horizontal position of the item inside the game. 
     * The y-pos will not change. There will be no collision detection.
     * 
     * @param x
     *                the horizontal position
     */
    void setX(int x)
    {
        xPosition = x;
    }

    /**
     * Sets the vertical position of the item inside the game. 
     * The x-pos will not change. There will be no collision detection.
     * 
     * @param y
     *                the vertical position
     */
    void setY(int y)
    {
        yPosition = y;
    }

    /**
     * Gets the horizontal position of this item (calculated from the upper-left corner).
     * 
     * @return the horizontal position
     */
    public int getX()
    {
        return xPosition;
    }

    /**
     * Gets the vertical position of this item (calculated from the upper-left corner).
     * 
     * @return the vertical position
     */
    public int getY()
    {
        return yPosition;
    }

    /**
     * Sets the visibility of this item.
     * 
     * @param visible
     *                true to make the item visible, false to make it invisible
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Gets the visibility of this item.
     * 
     * @return true if the item is visible, false if it is invisible.
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Display a standard animation by looping through the frames. This method is called
     * in every cycle of the game loop and just selects the next frame to be displayed.
     * By default it calls nextFrame() to do an animation, looping through the frames.
     * <p/>
     * Override this method to create your own custom animation. If you do not want any
     * animation, override this method with an empty body { }.
     */
    protected void animate()
    {
        nextFrame(); // default behavior
    }

    /**
     * Paints this item if it is visible. The upper-left corner of the item is rendered at it's current position (as
     * defined by setPosition)
     * 
     * @param g
     *                the Graphics object provided by the Canvas
     * @param offsetX
     *                the horizontal offset used by the viewport
     * @param offsetY
     *                the vertical offset used by the viewport
     */
    final void paint(Graphics g, int offsetX, int offsetY)
    {
        if (visible)
        {
            // calculate x and y postion based on the viewport offset and the currentFrame
            int x = xPosition - offsetX;
            int y = yPosition - offsetY;
            int xFrame = x - currentFrame * frameWidth;

            // select the exact region in the canvas that is allowed to change
            g.setClip(x, y, frameWidth, frameHeight);
            // draw the selected part of the filmstrip
            g.drawImage(image, xFrame, y, Graphics.TOP | Graphics.LEFT);
        }
    }
}