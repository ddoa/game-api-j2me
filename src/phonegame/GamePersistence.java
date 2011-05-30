/*
 * File: 	GamePersistence.java 
 * Created: Jan 31, 2005
 */
package phonegame;

import javax.microedition.rms.*;

/**
 * This class provides access to the persistence layer of the phone. Use this class if you want to permanently store
 * some type of information (e.g. highscores or other gamedata) on the phone.
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */

public class GamePersistence
{
    private static final String storageEntry = "GameAPI";

    private static RecordStore storage = null;

    /**
     * Store the given data on the phone, note that any previously saved information will be overriden.
     * 
     * @param data
     *                an array of bytes representing the data that will be saved
     * @throws GameException
     *                 if the save operation fails
     */
    public static final void save(byte[] data) throws GameException
    {
        try
        {
            try
            {
                // delete any previous saved data
                RecordStore.deleteRecordStore(storageEntry);
            } catch (RecordStoreNotFoundException rse)
            {}

            // open and/or create a new recordstore
            storage = RecordStore.openRecordStore(storageEntry, true);
            try
            {
                // try set the existing record (if it still exists)
                storage.setRecord(1, data, 0, data.length);
            } catch (InvalidRecordIDException ir)
            {
                // add the data to a new record (if there arn't any previous records)
                storage.addRecord(data, 0, data.length);
            }
        } catch (RecordStoreException rse)
        {
            throw new GameException("Save operation failed, an unkown error occured");
        } finally
        {
            try
            {
                // close the recordstore
                if (storage != null)
                    storage.closeRecordStore();
            } catch (RecordStoreNotOpenException e)
            {} catch (RecordStoreException e)
            {}
        }
    }

    /**
     * Retrieve the data stored on the phone
     * 
     * @return the taht that is saved on the phone, or <tt>null</tt> if no data is found
     * @throws GameException
     *                 if the load operation fails
     */
    public static final byte[] load() throws GameException
    {
        byte[] result = null;
        try
        {
            // open and/or create a new recordstore
            storage = RecordStore.openRecordStore(storageEntry, true);

            // get the latest saved entry
            if (storage.getNumRecords() > 0)
                result = storage.getRecord(storage.getNumRecords());
        } catch (RecordStoreFullException fe)
        {
            throw new GameException("Load operation failed, disk is full!");
        } catch (RecordStoreNotFoundException nfe)
        {
            throw new GameException("Load operation failed, cannot read source data since "
                    + "there isn't any data stored. Please use the save() function first");
        } catch (RecordStoreException e)
        {
            throw new GameException("Load operation failed, an unkown error occured");
        } finally
        {
            try
            {
                // close the recordstore
                if (storage != null)
                    storage.closeRecordStore();
            } catch (RecordStoreNotOpenException e)
            {} catch (RecordStoreException e)
            {}
        }
        return result;
    }

}