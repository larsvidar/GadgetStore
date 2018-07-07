package no.larsvidar.gadgetstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.larsvidar.gadgetstore.data.StoreContract.InventoryEntry;

public class StoreProvider extends ContentProvider {

    //*** Variables ***

    // URI matcher codes
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    // Uri matcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Static initializer
    static {
        //Adding URI codes to URI Matcher.
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY,  StoreContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    //Database helper object.
    private StoreDbHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        //Initialing the database helper object.
        mDatabaseHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        //Get readable database
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        //Cursor to hold the result
        Cursor cursor;

        //Checking URI matcher
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                //Queries the inventory table directly.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                //Setting up to query a specific ID
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                //Queries a specific ID
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Query error: Can not query unknown uri " + uri);
        }

        //Set notification URI on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Return cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Database insertion not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        //***** Validating inputs *****


        //Validate ProductName input
        //Checking that ProductName is not  null
        String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Please type in a Product name");
        }


        //Validate ProductPrice input
        //Checking that ProductPrice is not  null
        String productPrice = values.getAsString(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice == null) {
            throw new IllegalArgumentException("Please type in a Product price");
        }

        //Checking that ProductPrice is a number
        int price;
        try {
            price = Integer.parseInt(productPrice);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(productPrice + "is not a valid number for Price");
        }

        //Checking that ProductPrice is not negative
        if (price < 0) {
            throw new IllegalArgumentException("You can not set a negative price");
        }


        //Validate Product Quantity input
        //Checking that ProductQuantity is not  null
        String productQuantity = values.getAsString(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (productQuantity == null) {
            throw new IllegalArgumentException("Please type in quantity");
        }

        //Checking that ProductPrice is a number
        int quantity;
        try {
            quantity = Integer.parseInt(productQuantity);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(productPrice + "is not a valid number for Quantity");
        }

        //Checking that ProductPrice is not negative
        if (quantity < 0) {
            throw new IllegalArgumentException("You can not set a negative quantity");
        }


        //Validate SupplierName
        //Checking that SupplierName is not  null
        String supplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Please type in a Supplier name");
        }

        //Validate SupplierNumber
        //Checking that SupplierNumber is not  null
        String supplierNumber = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NUMBER);
        if (supplierNumber == null) {
            throw new IllegalArgumentException("Please type in a Supplier phone number");
        }


        //Get writable database
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        //Insert new product
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        //If database insertion failed, return null
        if (id == -1) {
            return null;
        }

        //Notify that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        //Return new URI with id.
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Get writable database
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        //Variable for deleted rows
        int deletedRows;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                //Delete all selected rows.
                deletedRows = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                //Delete specific id row.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                deletedRows = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Could not delete " + uri);
        }

        //If rows were deleted, notify that data has changed.
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Return number of deleted rows.
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //***** Validating inputs *****


        //Validate ProductName input if key is present
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {

            //Checking that ProductName is not  null
            String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Please type in a Product name");
            }
        }


        //Validate ProductPrice input if key is present
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {

            //Checking that ProductPrice is not  null
            String productPrice = values.getAsString(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice == null) {
                throw new IllegalArgumentException("Please type in a Product price");
            }

            //Checking that ProductPrice is a number
            int price;
            try {
                price = Integer.parseInt(productPrice);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(productPrice + "is not a valid number for Price");
            }

            //Checking that ProductPrice is not negative
            if (price < 0) {
                throw new IllegalArgumentException("You can not set a negative price");
            }
        }


        //Validate Product Quantity input if key is present
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {

            //Checking that ProductQuantity is not  null
            String productQuantity = values.getAsString(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (productQuantity == null) {
                throw new IllegalArgumentException("Please type in quantity");
            }

            //Checking that ProductQuantity is a number
            int quantity;
            try {
                quantity = Integer.parseInt(productQuantity);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(productQuantity + "is not a valid number for Quantity");
            }

            //Checking that ProductPrice is not negative
            if (quantity < 0) {
                throw new IllegalArgumentException("You can not set a negative quantity");
            }
        }


        //Validate SupplierName if key is present
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {

            //Checking that SupplierName is not  null
            String supplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Please type in a Supplier name");
            }
        }

        //Validate SupplierNumber if key is present
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NUMBER)) {

            //Checking that SupplierNumber is not  null
            String supplierNumber = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NUMBER);
            if (supplierNumber == null) {
                throw new IllegalArgumentException("Please type in a Supplier phone number");
            }
        }

        //Check if there are any values to update
        if (values.size() == 0) {
            return 0;
        }

        //Get writable database
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        //Update database and get number of affected rows
        int updatedRows = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        //Notify listener if any rows were updated
        if (updatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Return number of updated rows.
        return updatedRows;
    }
}
