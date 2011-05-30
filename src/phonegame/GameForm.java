/*
 * File: 	GameForm.java
 * Created: Jan 24, 2005
 */
package phonegame;

import java.io.IOException;

import javax.microedition.lcdui.*;

import phonegame.utils.Tools;

/**
 * This class allows you to easily create input forms on which you can add various GUI widgets (textboxes, etc). Please
 * be aware of the fact that you can't control the (exact) position and appearance of the widgets on the form, this is
 * done by the device itself.
 * <p>
 * Tip: GameForms may come in handy if you want to extend your game with functions that don't fit in your normal
 * gameworld.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */

public final class GameForm
{
    private Form form;

    private List list;

    private Display display = GameEngine.display; // obtain access to the display

    private IMenuListener menuListener;

    private IMenuListener listBoxListener;

    /**
     * Constructs an instance of this class
     */
    public GameForm(String title)
    {
        form = new Form(title);
    }

    /**
     * Adds a scrolling banner to the form.
     * 
     * @param text
     *                the text that needs to be displayed
     */
    public final void addBanner(String text)
    {
        form.setTicker(new Ticker(text));
    }

    /**
     * Adds a textbox to the form.
     * 
     * @param label
     *                the label in front of the textbox
     * @param text
     *                the initial text of the textbox
     * @param maxSize
     *                the maximum number of character that is allowed to fit in the textbox
     * @return the ID of the newly inserted textbox. You need this number if you want to retrieve the contents of the
     *            textbox or if you want to delete the textbox.
     */
    public final int addTextBox(String label, String text, int maxSize)
    {
        return form.append(new TextField(label, text, maxSize, TextField.ANY));
    }

    /**
     * Adds a number of radiobuttons to the form.
     * 
     * @param label
     *                the label displayed above the group
     * @param items
     *                the items that should be listed in the radiogroup
     * @return the ID of the newly inserted radiogroup. You need this number if you want to retrieve the currently
     *            selected item of the radiogroup or if you want to delete the radiogroup.
     */
    public final int addRadioGroup(String label, String[] items)
    {
        return form.append(new ChoiceGroup(label, ChoiceGroup.EXCLUSIVE, items, null));
    }

    /**
     * Adds a number of checkboxes to the form
     * 
     * @param label
     *                the label displayed above the group
     * @param items
     *                the items that should be listed in the checkboxgroup
     * @return the ID of the newly inserted checkboxgroup. You need this number if you want to retrieve the currently
     *            selected items in the checkboxgroup or if you want to delete the checkboxgroup.
     */
    public final int addCheckboxGroup(String label, String[] items)
    {
        return form.append(new ChoiceGroup(label, ChoiceGroup.MULTIPLE, items, null));
    }

    /**
     * Adds a label to the form.
     * 
     * @param label
     *                the labeltext
     * @param text
     *                the text value of the label
     * @return the ID of the newly inserted label. You need this number if you want to retrieve the contents of the
     *            label or if you want to delete the label.
     */
    public final int addLabel(String label, String text)
    {
        return form.append(new StringItem(label, text));
    }

    /**
     * Adds an image to the form.
     * 
     * @param label
     *                the label displayed next to the image
     * @param pathname
     *                the name of the image that you wnat to display
     * @return the ID of the newly inserted image. You need this number if you want to delete the image.
     * @throws GameException
     *                 if the specified image is not found at the given path
     */
    public final int addImage(String label, String pathname)
    {
        Image image;
        try
        {
            image = Image.createImage(pathname);
        } catch (IOException e)
        {
            throw new GameException("The image "+pathname+" could not be found.");
        }
        return form.append(new ImageItem(label, image, ImageItem.LAYOUT_CENTER, label));
    }

    /**
     * Adds a progressbar to the form.
     * 
     * @param label
     *                the label displayed inside the progessbar
     * @param readOnly
     *                true, if you want to create a pure progressbar (that the user can't control). False if you want to
     *                create a type of "slidebar" that allows the user to interact with the value of the bar.
     * @param maxValue
     *                the maximum value/lenght of the bar
     * @param startValue
     *                the initial value of the bar
     * @return the ID of the newly inserted progressbar. You need this number if you want to retrieve the contents of
     *            the progressbar or if you want to delete the progressbar.
     */
    public final int addProgressBar(String label, boolean readOnly, int maxValue, int startValue)
    {
        return form.append(new Gauge(label, readOnly, maxValue, startValue));
    }

    /**
     * Gets the contents of the banner.
     * 
     * @return the text that is currently scrolling in the banner
     * @throws GameException
     *                 if there is no banner found on the current form
     */
    public final String getBannerValue() throws GameException
    {
        if (form.getTicker() != null)
        {
            return form.getTicker().getString();
        } else
        {
            throw new GameException("Banner not found!, there is no banner on this GameForm");
        }
    }

    /**
     * Gets the contents of a certain textbox on the form.
     * 
     * @param componentNr
     *                the ID of the textbox on the form
     * @return a string containing the content of the textbox
     * @throws GameException
     *                 if the textbox with the given number is not found
     */
    public final String getTextBoxValue(int componentNr) throws GameException
    {
        try
        {
            TextField textField = (TextField) form.get(componentNr);
            return textField.getString();
        } catch (Exception cc)
        {
            // reformat exception (IndexOutOfBounds or ClassCast) to one more friendly
            throw new GameException("TextBox not found!, there is no textbox on this GameForm "
                    + " that matches with the given componentNr");
        }
    }

    /**
     * Gets the contents of a certain label on the form.
     * 
     * @param componentNr
     *                the ID of the label on the form
     * @return a string containing the content of the label
     * @throws GameException
     *                 if the label with the given number is not found
     */
    public final String getLabelValue(int componentNr) throws GameException
    {
        try
        {
            StringItem label = (StringItem) form.get(componentNr);
            return label.getLabel();
        } catch (Exception cc)
        {
            // reformat exception (IndexOutOfBounds or ClassCast) to one more friendly
            throw new GameException("Label not found!, there is no label on this GameForm "
                    + " that matches with the given componentNr");
        }
    }

    /**
     * Gets the currently selected item of a certain radiogroup on the form.
     * 
     * @param componentNr
     *                the ID of the radiogroup on the form
     * @return an integer defining the currently selected item
     * @throws GameException
     *                 if the radiogroup with the given number is not found
     */
    public final int getRadioGroupValue(int componentNr) throws GameException
    {
        try
        {
            ChoiceGroup check = (ChoiceGroup) form.get(componentNr);
            return check.getSelectedIndex();
        } catch (Exception cc)
        {
            // reformat exception (IndexOutOfBounds or ClassCast) to one more friendly
            throw new GameException("RadioGroup not found!, there are no radiobuttons on this GameForm "
                    + " that match with the given componentNr");
        }
    }

    /**
     * Gets the currently selected items of a certain checkboxgroup on the form.
     * 
     * @param componentNr
     *                the ID of the checkboxgroup on the form
     * @return an array of boolean that indicate if a checkbox is either checked (=true) or unchecked (=false).
     * @throws GameException
     *                 if the checkboxgroup with the given number is not found
     */
    public final boolean[] getCheckboxGroupValue(int componentNr) throws GameException
    {
        try
        {
            ChoiceGroup check = (ChoiceGroup) form.get(componentNr);
            boolean[] itemsChecked = new boolean[check.size()];

            check.getSelectedFlags(itemsChecked);
            return itemsChecked;
        } catch (Exception cc)
        {
            // reformat exception (IndexOutOfBounds or ClassCast) to one more friendly
            throw new GameException("CheckboxGroup not found!, there are no checkboxes on this GameForm "
                    + " that match with the given componentNr");
        }
    }

    /**
     * Gets the value of a certain progressbar on the form.
     * 
     * @param componentNr
     *                the ID of the progressbar on the form
     * @return a integer containing the current value of the progressbar
     * @throws GameException
     *                 if the progressbar with the given number is not found
     */
    public final int getProgressBarValue(int componentNr) throws GameException
    {
        Gauge progressBar;
        try
        {
            progressBar = (Gauge) form.get(componentNr);
            return progressBar.getValue();
        } catch (Exception cc)
        {
            // reformat exception (IndexOutOfBounds or ClassCast) to one more friendly
            throw new GameException("ProgressBar not found!, there is no progressbar on this GameForm "
                    + " that matches with the given componentNr");
        }
    }

    /**
     * Sets the value of a certain progressbar on the form.
     * 
     * @param componentNr
     *                the ID of the progressbar on the form
     * @param value
     *                the new value of the progressbar
     * @throws GameException
     *                 if the progressbar with the given number is not found
     */
    public final void setProgressBarValue(int componentNr, int value) throws GameException
    {
        Gauge progressBar;
        try
        {
            progressBar = (Gauge) form.get(componentNr);
            progressBar.setValue(value);
        } catch (Exception cc)
        {
            // reformat exception (IndexOutOfBounds or ClassCast) to one more friendly
            throw new GameException("ProgressBar not found!, there is no progressbar on this GameForm "
                    + " that matches with the given componentNr");
        }
    }

    /**
     * Deletes the banner of the current GameForm.
     */
    public final void deleteBanner()
    {
        form.setTicker(null);
    }

    /**
     * Deletes a certain component (textbox, progressbar, etc) of this GameForm.
     * 
     * @param componentNr
     *                the ID of a certain component on the form
     * @throws GameException
     *                 if the component with the given number was not found
     */
    public final void deleteComponent(int componentNr) throws GameException
    {
        try
        {
            form.delete(componentNr);
        } catch (IndexOutOfBoundsException iob)
        {
            // reformat IndexOutOfBounds exception to one more friendly
            throw new GameException("Invalid componentNr, the component with number: " + componentNr
                    + " was not found on this GameForm");
        }
    }

    /**
     * Remove all components currently on the GameForm
     */
    public final void deleteAllComponents() throws GameException
    {
        for (int i = 0; i < form.size(); i++)
        {
            form.delete(i);
        }
    }

    /**
     * Display a messagebox/alert to the user on the screen.
     * 
     * @param title
     *                the title of the messagebox
     * @param message
     *                the message that you want to display
     * @param type
     *                the type of messagebox that needs to be displayed, valid values are:
     * 
     * <p/>
     *  - &lt;u&gt;AlertType.ALARM&lt;/u&gt;
     *    An ALARM AlertType is a hint to alert the user to an event for which the 
     *    user has previously requested to be notified. 
     * <p/>
     *  - &lt;u&gt;AlertType.CONFIRMATION&lt;/u&gt;
     *    A CONFIRMATION AlertType is a hint to confirm user actions. 
     * <p/>
     *  - &lt;u&gt;AlertType.ERROR&lt;/u&gt; 
     *    An ERROR AlertType is a hint to alert the user to an erroneous operation. 
     * <p/>
     *  - &lt;u&gt;AlertType.INFO&lt;/u&gt; 
     *    An INFO AlertType typically provides non-threatening information to the user. 
     * <p/>
     *  - &lt;u&gt;AlertType.WARNING&lt;/u&gt; 
     *    A WARNING AlertType is a hint to warn the user of a potentially 
     *    dangerous operation. 
     * 
     * @param timeVisible
     *                the time in milliseconds that you want the messagebox to be visible
     */
    public final void showMessageBox(String title, String message, AlertType type, int timeVisible)
    {
        Alert alert = new Alert(title, message, null, type);
        alert.setTimeout(timeVisible);
        display.setCurrent(alert);
    }

    /**
     * Display a (fullscreen) listbox on the screen.
     * <p>
     * Note: Unfortunately we cannot place a listbox on a (Game)Form since the listbox is a displayable object on it's
     * own. Therefore this method is called <tt>showListbox</tt> instead of <tt>addListbox</tt>.
     * 
     * @param title
     *                the title of the listbox
     * @param items
     *                the items that should be displayed in the listbox
     * @param selectedItem
     *                number of an item that is selected by default
     * @param menuItems
     *                a list of items that you want to display in the phone menu (or <tt>null</tt> if you don't want to
     *                add a menu)
     * @param listener
     *                reference to an object that can handle menu events
     * @throws GameException
     *                 if the selectedItem is not found in the items array.
     */
    public final void showListbox(String title, String[] items, int selectedItem, String[] menuItems,
            IMenuListener listener) throws GameException
    {
        list = new List(title, List.IMPLICIT, items, null);

        // create command menu if specified
        if (listener != null && menuItems != null)
        {
            listBoxListener = listener;
            Tools.makeMenu(menuItems, ((CommandListener) form), ((Displayable) list));
        }

        try
        {
            list.setSelectedIndex(selectedItem, true);
        } catch (IndexOutOfBoundsException iob)
        {
            // reformat IndexOutOfBounds exception to one more friendly
            throw new GameException("The item you are trying to select doesn't exists in the items array");
        }
        display.setCurrent(list);
    }

    /**
     * Get the currently seelcted item in the listbox.
     * 
     * @return an integer containing the currently selected item
     */
    public final int getListBoxValue()
    {
        return list.getSelectedIndex();
    }

    /**
     * Create a menu with the given menuitem names.
     * 
     * @param menuItems
     *                the names that will appear in the phone's menu
     * @param listener
     *                reference to an object that can handle menu events
     * @throws GameException
     *                 if the one of the two parameters is equal to <tt>null</tt>
     */
    public final void makeMenu(String[] menuItems, IMenuListener listener) throws GameException
    {
        if ( menuItems.length > 0 )
        {
            menuListener = listener;
            Tools.makeMenu(menuItems, ((CommandListener) form), ((Displayable) form));
        } else
        {
            throw new GameException("Can't create menu, list of menu items is empty.");
        }
    }

    /**
     * Obtain a reference to the underlying LCDUI form class (required by the GameEngine.setGameForm method).
     * 
     * @return the private form class that can be used in the <tt>Display.setCurrent</tt> method.
     */
    final Form getDisplay()
    {
        return form;
    }

    /**
     * Private form class to hide specific J2ME Form programming methods (like addCommand or append) for the students.
     * 
     * @author Tim Prijn & Richard Kettelerij
     * @version 1.0
     */
    private final class Form extends javax.microedition.lcdui.Form implements CommandListener
    {
        /**
         * Constructs an instance of this class
         */
        Form(String title)
        {
            super(title);
        }

        /**
         * Redirect command event to the (more user-friendly) menuListener
         * 
         * @see javax.microedition.lcdui.CommandListener#commandAction( javax.microedition.lcdui.Command,
         *         javax.microedition.lcdui.Displayable)
         */
        public final void commandAction(Command cmd, Displayable display)
        {
            menuListener.menuAction(cmd.getLabel());
        }
    }
}