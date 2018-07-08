package no.larsvidar.gadgetstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import no.larsvidar.gadgetstore.data.StoreContract.InventoryEntry;

public class StoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //*** Variables ***
    public static final int STORE_LOADER = 0;
    StoreCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        //Set opp Add Product button
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        //Assign List>View to variable
        ListView inventoryListView = findViewById(R.id.inventory_list);

        //Setting up Empty View
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        //Create new StoreCursorAdapter
        mCursorAdapter = new StoreCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        //Item Click Listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create intent for EditActivity.
                Intent intent = new Intent(StoreActivity.this, EditActivity.class);

                //Make URI for the specific product that was pressed, and set it on the intent.
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(currentInventoryUri);

                //Launch EditActivity
                startActivity(intent);
            }
        });

        //Start the loader.
        getLoaderManager().initLoader(STORE_LOADER, null, this);
    }

    private void deleteAllProducts() {
        getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        makeToast("All products deleted!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu from menu_store.xml
        getMenuInflater().inflate(R.menu.menu_store, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Making switch-statement in case more menu items are added later.
        switch (item.getItemId()) {
            case R.id.menu_store_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Create projection
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_NUMBER
        };

        //Execute query on the background thread.
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Updating cursor
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Empty cursor.
        mCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Are you sure you want to delete ALL products?");
        alertBuilder.setPositiveButton("Delete everything!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //User pressed Delete button.
                deleteAllProducts();
                makeToast("Database is now empty!");
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //User clicked the Cancel button
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Show the AlertDialog
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }
}
