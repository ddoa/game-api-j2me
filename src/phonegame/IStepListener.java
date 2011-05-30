/*
 * File: 	IStepListener.java
 * Created: Apr 25, 2005
 */
package phonegame;

/**
 * Use this interface if you want to receive step events
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */
public interface IStepListener
{

    /**
     * Every cycle this method is triggered by the GameEngine.
     * 
     * @param stepnr
     *                the number of steps executed so far.
     */
    public void stepAction(int stepnr);
}