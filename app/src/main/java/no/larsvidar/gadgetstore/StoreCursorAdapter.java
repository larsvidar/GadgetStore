package no.larsvidar.gadgetstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import no.larsvidar.gadgetstore.data.StoreContract.InventoryEntry;

public class StoreCursorAdapter extends CursorAdapter {

    //Constructor
    public StoreCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Inflate each list item using store_item.xml.
        return LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Assign relevant views to variables
        TextView productNameView = view.findViewById(R.id.product_name);
        TextView productInfoView = view.findViewById(R.id.product_info);

        //Find the relevant product attribute columns
        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
        int supplierNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NUMBER);

        //Get product attributes from cursor
        String productName = cursor.getString(productNameColumnIndex);
        String productPrice = cursor.getString(productPriceColumnIndex);
        String productQuantity = cursor.getString(productQuantityColumnIndex);
        String supplierName = cursor.getString(supplierNameColumnIndex);
        String supplierNumber = cursor.getString(supplierNumberColumnIndex);

        ///Update TextViews with the current attributes
        productNameView.setText(productName);
        productInfoView.setText("$" + productPrice + " ");
        productInfoView.append(productQuantity + " items");
    }
}
