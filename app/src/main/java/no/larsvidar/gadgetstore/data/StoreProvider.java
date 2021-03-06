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
import android.util.Log;

import no.larsvidar.gadgetstore.R;
import no.larsvidar.gadgetstore.data.StoreContract.InventoryEntry;

/**
 *  ContentProvider for GadgetStore app
 */
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
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    //Database helper object.
    private StoreDbHelper mDatabaseHelper;

    /**
     * OnCreate method
     * @return true
     */
    @Override
    public boolean onCreate() {
        //Initialing the database helper object.
        mDatabaseHelper = new StoreDbHelper(getContext());
        return true;
    }

    /**
     * Method for querying the database.
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
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
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //Queries a specific ID
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.query_error) + " " + uri);
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
                throw new IllegalStateException(getContext().getString(R.string.get_type_error_1)
                        + " " + uri + " " + getContext().getString(R.string.get_type_error_2) + " " + match);
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
                throw new IllegalArgumentException(getContext().getString(R.string.insert_error) + " " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        /***** Validating inputs *****/

        //Validate ProductName input
        //Checking that ProductName is not  null
        String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (productName == null || productName.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_name_null));
        }

        //Validate ProductPrice input
        //Checking that ProductPrice is not  null
        String productPrice = values.getAsString(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice == null || productPrice.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_price_null));
        }

        //Checking that ProductPrice is a number
        int price;
        try {
            price = Integer.parseInt(productPrice);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException();
        }

        //Checking that ProductPrice is not negative
        if (price < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_price_negative));
        }

        //Validate Product Quantity input
        //Checking that ProductQuantity is not  null
        String productQuantity = values.getAsString(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (productQuantity == null || productQuantity.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_quantity_null));
        }

        //Checking that ProductQuantity is a number
        int quantity;
        try {
            quantity = Integer.parseInt(productQuantity);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException();
        }

        //Checking that ProductPrice is not negative
        if (quantity < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_quantity_negative));
        }

        //Validate SupplierName
        //Checking that SupplierName is not  null
        String supplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null || supplierName.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_supplier_name_null));
        }

        //Validate SupplierNumber
        //Checking that SupplierNumber is not  null
        String supplierNumber = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NUMBER);
        if (supplierNumber == null || supplierNumber.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.validate_error_supplier_number_null));
        }

        /***** Insert to database *****/
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
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.delete_error) + " " + uri);
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
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateProduct(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.update_error) + " " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        /***** Validating inputs *****/
        //Validate ProductName input if key is present
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {

            //Checking that ProductName is not  null
            String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (productName == null || productName.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_name_null));
            }
        }

        //Validate ProductPrice input if key is present
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {

            //Checking that ProductPrice is not  null
            String productPrice = values.getAsString(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice == null || productPrice.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_price_null));
            }

            //Checking that ProductPrice is a number
            int price;
            try {
                price = Integer.parseInt(productPrice);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException();
            }

            //Checking that ProductPrice is not negative
            if (price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_price_negative));
            }
        }

        //Validate Product Quantity input if key is present
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {

            //Checking that ProductQuantity is not  null
            String productQuantity = values.getAsString(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (productQuantity == null || productQuantity.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_quantity_null));
            }

            //Checking that ProductQuantity is a number
            int quantity;
            try {
                quantity = Integer.parseInt(productQuantity);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException();
            }

            //Checking that ProductPrice is not negative
            if (quantity < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_product_quantity_negative));
            }
        }

        //Validate SupplierName if key is present
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {

            //Checking that SupplierName is not  null
            String supplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null || supplierName.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_supplier_name_null));
            }
        }

        //Validate SupplierNumber if key is present
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NUMBER)) {

            //Checking that SupplierNumber is not  null
            String supplierNumber = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NUMBER);
            if (supplierNumber == null || supplierNumber.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.validate_error_supplier_number_null));
            }
        }

        /***** Updating database *****/
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
