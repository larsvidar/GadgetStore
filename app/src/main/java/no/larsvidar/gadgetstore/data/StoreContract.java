package no.larsvidar.gadgetstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API contract for GadgetStore app
 */
public final class StoreContract {

    //Empty constructor
    private StoreContract() {
    }

    //Path variables
    public static final String CONTENT_AUTHORITY = "no.larsvidar.gadgetstore";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Class for inventory database table constant values.
     */
    public static final class InventoryEntry implements BaseColumns {

        //Content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //MIME type constants
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY
                + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY
                + "/" + PATH_INVENTORY;

        //Name of the inventory table.
        public static final String TABLE_NAME = "inventory";

        //Constants for each column i database table.
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRODUCT_PRICE = "product_price";
        public static final String COLUMN_PRODUCT_QUANTITY = "product_quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_NUMBER = "supplier_number";
    }
}
