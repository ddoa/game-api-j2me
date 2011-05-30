/*
 * File: 	IMenuListener.java
 * Created: Jan 22, 2005
 */
package phonegame;

/**
 * Use this interface if you want to receive menu events
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */
public interface IMenuListener 
{
 	/**
   	* Define an action associated with the given menuitem.
   	* 
   	* @param menuItemLabel label of the item that is chosen
   	*/
    public abstract void menuAction(String menuItemLabel);
}
