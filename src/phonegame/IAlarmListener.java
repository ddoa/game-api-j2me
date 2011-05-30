/*
 * File: 	IAlarmListener.java
 * Created: Jan 29, 2005
 */
package phonegame;

/**
 * Use this interface if you want to receive timer/alarm events
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */
public interface IAlarmListener
{
    /**
     * Callback is done when the alarm timer is ended.
     * 
     * @param id
     *                The id-number of the timer, that was specified when the timer was set 
     */
    public void alarm(int id);
}