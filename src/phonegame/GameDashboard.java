/*
 * File: 	GameDashboard.java
 * Created: Jan 27, 2005
 */
package phonegame;

import javax.microedition.lcdui.*;
import phonegame.utils.Tools;

/**
 * This class visually represents the "dashboard" in you game. You can use this dashboard to display all sorts of status
 * information like, for example: the amount of health, power, etc.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */

public class GameDashboard
{
    private static final int ITEMS_LENGHT = 10;

    private static final int TEXT_OFFSET = 2;

    private int backRed = 0, backGreen = 0, backBlue = 0;

    private int lineRed = 255, lineGreen = 255, lineBlue = 255;

    private int foreRed = 255, foreGreen = 255, foreBlue = 255;

    private int dashX = 0, dashY = 0;

    private int dashW = 0, dashH = 10;

    private int textX = 2, textY = 2;

    private int itemCounter = 0;

    private int fontStyle;

    private boolean dotted = false;

    private String[][] items;

    private String label, value;

    /**
     * Constructs an empty GameDashboard
     */
    public GameDashboard()
    {
        items = new String[ITEMS_LENGHT][2];
    }

    /**
     * Adds a new display-item to the dashboard. A display-item consists of a label 
     * (the name of the item) and a value. Both are Strings. 
     * <br/>
     * For example, to show points, make the label "Points" and the value at start "0".
     * Later on, you can change the value of a display-item with a given name.
     * 
     * @param label
     *                the label of this item, try to keep this short (e.g use LP instead of Lifepoints).
     * @param value
     *                the value of this item
     */
    public void addItem(String label, String value)
    {
        if (itemCounter == ITEMS_LENGHT - 1)
            items = (String[][]) Tools.expandArray(items, 10);

        items[itemCounter][0] = label;
        items[itemCounter][1] = value;
        itemCounter++;
    }

    /**
     * Deletes a display-item from the dashboard
     * 
     * @param label
     *                the label name of the item (serves as a 'key')
     */
    public void deleteItem(String label)
    {
        for (int i = 0; i < itemCounter; i++)
        {
            if (items[i][0] != null && items[i][0].equals(label))
            {
                items[i][0] = null;
                items[i][1] = null;
            }
        }
    }

    /**
     * Sets the value property of a display-item on the dashboard. 
     * If there is no display-item with the given label, nothing is shown.
     * 
     * @param label
     *                the label name of the item (serves as a 'key')
     */
    public void setItemValue(String label, String value)
    {
        for (int i = 0; i < itemCounter; i++)
        {
            if (items[i][0] != null && items[i][0].equals(label))
                items[i][1] = value;
        }
    }

    /**
     * Gets the value property of a display-item on the dashboard
     * 
     * @param label
     *                the label name of the item (serves as a 'key')
     * @return the current value, as a String
     */
    public String getItemValue(String label)
    {
        String result = null;
        for (int i = 0; i < itemCounter; i++)
        {
            if (items[i][0] != null && items[i][0].equals(label))
            {
                result = items[i][1];
            }
        }
        return result;
    }

    /**
     * Sets the background color of the dashboard. 
     * The default color is black. If you don't want to use a background
     * color set the RGB values to -1 (transparent)
     * 
     * @param red
     *                the red RGB component
     * @param blue
     *                the blue RGB component
     * @param green
     *                the green RGB component
     */
    public void setBackgroundColor(int red, int green, int blue)
    {
        backRed = red;
        backGreen = green;
        backBlue = blue;
    }

    /**
     * Sets the color of the text that is displayed on the dashboard.
     * 
     * @param red
     *                the red RGB component
     * @param blue
     *                the blue RGB component
     * @param green
     *                the green RGB component
     */
    public void setForegroundColor(int red, int green, int blue)
    {
        foreRed = red;
        foreGreen = green;
        foreBlue = blue;
    }

    /**
     * Sets the line color of the dashboard. 
     * The default color is black. If you don't want to use a line color set the
     * RGB values to -1 (transparent)
     * 
     * @param red
     *                the red RGB component
     * @param blue
     *                the blue RGB component
     * @param green
     *                the green RGB component
     * @param dotted
     *                true if the border should be drawn as a dotted line
     */
    public void setLineColor(int red, int green, int blue, boolean dotted)
    {
        lineRed = red;
        lineGreen = green;
        lineBlue = blue;
        this.dotted = dotted;
    }

    /**
     * Sets the font-properties of the text on the dashboard.
     * You can switch on/off: Bold, italic and underline.
     * 
     * @param bold
     *                true, if text should appear <b>bold </b>
     * @param italic
     *                true, if text should appear <i>italic </i>
     * @param underline
     *                true, if text should appear <u>underlined </u>
     */
    public void setFont(boolean bold, boolean italic, boolean underline)
    {
        if (bold)
            fontStyle = Font.STYLE_BOLD;
        if (italic)
            fontStyle |= Font.STYLE_ITALIC;
        if (underline)
            fontStyle |= Font.STYLE_UNDERLINED;
    }

    /**
     * Sets the position of the dashboard on the screen
     * 
     * @param x
     *                the horizontal position
     * @param y
     *                the vertical position
     */
    public void setPosition(int x, int y)
    {
        dashX = x;
        dashY = y;
        textY = dashY + TEXT_OFFSET;
    }

    /**
     * Sets the dimensions of the dashboard
     * 
     * @param w
     *                the width of the dashboard
     * @param h
     *                the height of the dashboard
     */
    public void setSize(int w, int h)
    {
        dashW = w;
        dashH = h;
    }

    /**
     * Draws the dashboard on the screen.
     * <p>
     * Note: If you want to create a more fancy dashboard you'll need to override this method with your own
     * implementation.
     * 
     * @param g
     *                the Graphics object of the GameWindow
     */
    public void paint(Graphics g)
    {
        g.setClip(dashX, dashY, dashW, dashH);
        // draw background (if color are not equal to -1)
        if (backRed != -1 && backGreen != -1 && backGreen != -1)
        {
            g.setColor(backRed, backGreen, backBlue);
            g.fillRect(dashX, dashY, dashW, dashH);
        }

        // draw border (if color are not equal to -1)
        if (lineRed != -1 && lineGreen != -1 && lineBlue != -1)
        {
            if (dotted)
            {
                g.setStrokeStyle(Graphics.DOTTED);
            }
            g.setColor(lineRed, lineGreen, lineBlue);
            g.drawRect(dashX, dashY, dashW - 1, dashH - 1);
        }

        // set text style
        g.setColor(foreRed, foreGreen, foreBlue);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, fontStyle, Font.SIZE_SMALL));

        for (int i = 0; i < itemCounter; i++)
        {
            if (items[i][0] != null )
            {	label = items[i][0];
            	value = items[i][1];

            	// draw the text on the proper position
            	g.drawString(label + ": " + value, textX, textY, Graphics.TOP | Graphics.LEFT);
            	textX += (label.length() + value.length()) * 7;

            	// perform word wrapping
            	if (textX > dashW - 20)
            	{
            	    textX = TEXT_OFFSET;
            	    textY += 10;
            	}
            }
        }

        // reset positions
        textX = TEXT_OFFSET;
        textY = dashY + TEXT_OFFSET;

        // reset styles
        g.setFont(Font.getDefaultFont());
        g.setStrokeStyle(Graphics.SOLID);
    }
}