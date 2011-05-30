/*
 * File: 	Tools.java
 * Created: Jan 22, 2005
 */
package phonegame.utils;

import java.util.Random;
import javax.microedition.lcdui.*;

/**
 * This class provides miscellaneous functions used in the game.
 * 
 * @author Tim Prijn & Richard Kettelerij
 * @version 2.0, October 11, 2005
 */
public final class Tools
{
    private static final Random random = new Random();

    /**
     * Scale an image to the given width and height.
     * <p>
     * <b>Be aware that resizing an image is very CPU intensive, therefore use this method with care. </b>
     * 
     * @param src
     *                the source image that needs to be resized
     * @return a new image object that contains the resized image
     */
    public static final Image resizeImage(Image src, int width, int height)
    {
        // check if it is absolutely necessary to resize the image
        if (src.getWidth() != width && src.getHeight() != height)
        {
            int srcW = src.getWidth();
            int srcH = src.getHeight();
            // create temporary image object
            Image tmp = Image.createImage(width, srcH);
            Graphics g = tmp.getGraphics();

            // calculate new width
            int delta = (srcW << 16) / width;
            int pos = delta / 2;

            // perform resize operation (horizontally)
            for (int x = 0; x < width; x++)
            {
                g.setClip(x, 0, 1, srcH);
                g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
                pos += delta;
            }

            // create temporary image object
            Image dst = Image.createImage(width, height);
            g = dst.getGraphics();

            // calculate new height
            delta = (srcH << 16) / height;
            pos = delta / 2;

            // perform resize operation (vertically)
            for (int y = 0; y < height; y++)
            {
                g.setClip(0, y, width, 1);
                g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
                pos += delta;
            }
            return dst;
        } else
        {
            return src;
        }
    }

    /**
     * Generates a random integer between 0 (zero) and the given range, not including range.
     * 
     * @param range
     *                the maximum number that may be returned
     * @return a pseudo-random integer
     */
    public static final int random(int range)
    {
        return (random.nextInt() << 1 >>> 1) % range;
    }

    /**
     * Returns a random number between 0 and 100
     * 
     * @return A random number between 0 and 100%
     */
    public static final int randomPercent()
    {
        return random(100);
    }
    
    public static final int round(double d)
    {
        if ( d < 0 )
            return (int)(d-0.5);
        else
            return (int)(d+0.5);
    }
    
    /**
     * Calculate the arcsin of a given x.<br/>
     * For great accuracy, the absolute value of x must be smaller than sqrt(2)/2
     * 
     * @param x
     * @return double, the arcsin in radians
     */
    public static final double arcsin(double x)
    {
        double res = x;		// first term of Taylor series is x itself.
        double it = res;		// running term
        // five extra terms is accurate for |x| < sqrt(2)/2
        for (int n=1; n<6; n++)
        {
            // calculate next term from previous
            it = (it * x * x * (2*n-1) * (2*n-1)) / ((2*n+1) * (2*n));
            // add term to result
            res = res + it;
        }
        return res;
    }

    /**
     * Create a menu with the given menuitem names.
     * 
     * @param menuItems
     *                the names that will appear in the phone's menu
     * @param listener
     *                the commandlistener that will handle the commandevents
     * @param display
     *                the Displayable object were te commands will be added to.
     */
    public static final void makeMenu(String[] menuItems, CommandListener listener, Displayable display)
    {
        // create command for every menuitem in the string array
        for (int i = 0; i < menuItems.length; i++)
        {
            Command cmd = new Command(menuItems[i], Command.ITEM, 2);
            display.addCommand(cmd);
        }
        // let the given listener handle the command events
        display.setCommandListener(listener);
    }

    /**
     * Take an array of existing objects and expand it's size by a given number of elements.
     * 
     * @param oldArray
     *                The array to expand.
     * @param expandBy
     *                The number of elements to expand the array by.
     * @return A new array (which is a copy of the original with space for more elements.
     */
    public static final Object[] expandArray(Object[] oldArray, int expandBy)
    {
        Object[] newArray = new Object[oldArray.length + expandBy];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    /**
     * Take a 2D array of existing objects and expand it's size by a given number of elements.
     * 
     * @param oldArray
     *                The array to expand.
     * @param expandBy
     *                The number of elements to expand the array by.
     * @return A new array (which is a copy of the original with space for more elements.
     */
    public final static Object[][] expandArray(Object[][] oldArray, int expandBy)
    {
        Object[][] newArray = new Object[oldArray.length + expandBy][];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }
}