/*
 * File: 	GameException.java
 * Created: Jan 23, 2005
 */
package phonegame;

/**
 * Custom exception class to handle exceptions thrown by the GameAPI.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */

public class GameException extends RuntimeException
{
    private String error;

    /**
     * Constructs an instance of this class.
     * 
     * @param error
     *                a description of the problem that has occured
     */
    public GameException(String error)
    {
        super(error);
        this.error = error;
    }

    /**
     * Gets a description of the problem that has occured.
     * 
     * @return string containing the error message
     */
    public String getError()
    {
        return error;
    }
}