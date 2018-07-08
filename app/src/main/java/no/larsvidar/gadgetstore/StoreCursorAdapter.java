package no.larsvidar.gadgetstore;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    public void bindView(View view, final Context context, Cursor cursor) {
        //Assign relevant views to variables
        TextView productNameView = view.findViewById(R.id.product_name);
        TextView productPriceView = view.findViewById(R.id.product_price);
        final TextView productQuantityView = view.findViewById(R.id.product_quantity);
        TextView supplierNameView = view.findViewById(R.id.supplier_name);
        TextView supplierNumberView = view.findViewById(R.id.supplier_number);

        //Find the relevant product attribute columns
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
        int supplierNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NUMBER);

        //Get product attributes from cursor
        final int id = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(productNameColumnIndex);
        String productPrice = cursor.getString(productPriceColumnIndex);
        String productQuantity = cursor.getString(productQuantityColumnIndex);
        String supplierName = cursor.getString(supplierNameColumnIndex);
        final String supplierNumber = cursor.getString(supplierNumberColumnIndex);

        ///Update TextViews with the current attributes
        productNameView.setText(productName);
        productPriceView.setText(productPrice);
        productQuantityView.setText(productQuantity);
        supplierNameView.setText(supplierName);
        supplierNumberView.setText(supplierNumber);


        //Make OnClickListener for Sale-button.
        Button saleButton = view.findViewById(R.id.store_sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get value from quantity Text View, and subtract 1.
                int newQuantity = Integer.parseInt(productQuantityView.getText().toString()) -1;

                //Check if quantity has reached 0.
                if (newQuantity >= 0) {
                    //If not 0, subtract 1 from database,
                    if (decreaseQuantity(id, newQuantity, context) != 0) {
                        //If successful, update quantity Text View, and show success toast.
                        productQuantityView.setText(Integer.toString(newQuantity));
                        makeToast(context, "Sale successfully registered");
                    } else {
                        //If not, show error.
                        makeToast(context, "Error: Sale did not register!");
                    }
                //If quantity has reach 0, show Out of stock toast.
                } else {
                    makeToast(context, "This item is out of stock!");
                }
            }
        });
    }


    public int decreaseQuantity(int id, int currentQuantity, Context context) {
        //Making ContentValues for ProductQuantity only.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);
        //Constructing URI for pressed item.
        Uri newUri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, Integer.toString(id));
        //Updating sale in database
        int rows = context.getContentResolver().update(newUri, values, null, null);
        //Return number of affected rows.
        return rows;
    }

    public void makeToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
