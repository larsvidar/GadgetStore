package no.larsvidar.gadgetstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import no.larsvidar.gadgetstore.data.StoreContract.InventoryEntry;

/**
 * Database helper for GadgetStore app
 */
public class StoreDbHelper extends SQLiteOpenHelper {

    //Variables for database
    private static final String DATABASE_NAME = "gadgetstore.db";
    private static final int DATABASE_VERSION = 1;

    //Constructor
    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * THis method is called when the database is created.
     * @param database to set up.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        //Creating the SQL statement for creating inventory table.
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE "
                + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_SUPPLIER_NUMBER + " TEXT NOT NULL);"; //Saving phone number as text to accommodate different ways of writing phone numbers.

        //Execute the SQL statement
        database.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * Database upgrade method
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Nothing needs to be done here.
    }
}
