package no.larsvidar.gadgetstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import no.larsvidar.gadgetstore.data.StoreContract.InventoryEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //*** Variables ***
    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentInventoryUri;
    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierNumberEditText;

    private boolean mHasProductChanged = false;
    private int mQuantity = 0;

    //OnTouchListener listening for any user interaction on a view.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mHasProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        //Determining if we should be in add or update mode.
        if (mCurrentInventoryUri == null) {
            setTitle("Add product");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit product");
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        //Assign all relevant Edit Views to variables
        mProductNameEditText = findViewById(R.id.edit_product_name);
        mProductPriceEditText = findViewById(R.id.edit_product_price);
        mProductQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierNumberEditText = findViewById(R.id.edit_supplier_number);

        //Button click listeners
        Button addQuantityButton = findViewById(R.id.edit_button_add_quantity);
        Button subtractQuantityButton = findViewById(R.id.edit_button_subtract_quantity);

        addQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
            }
        });

        subtractQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0) {
                    mQuantity--;
                }
            }
        });
    }

    private void saveProduct() {
        //Read data from input fields
        String productNameString = mProductNameEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierNumberString = mSupplierNumberEditText.getText().toString().trim();

        //Check if input fields are empty
        if (mCurrentInventoryUri == null
                && TextUtils.isEmpty(productNameString)
                && TextUtils.isEmpty(productPriceString)
                && TextUtils.isEmpty(productQuantityString)
                && TextUtils.isEmpty(supplierNameString)
                && TextUtils.isEmpty(supplierNumberString)) {
            //Return early since all fields are empty.
            return;
        }

        //Create ContentValues
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, Integer.parseInt(productPriceString));
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(productQuantityString));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NUMBER, supplierNumberString);

        //Check if this is a new product, or updating an existing product.
        if (mCurrentInventoryUri == null) {
            //Add new product
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            //Show toast
            if (newUri == null) {
                //If newUri is null, something went wrong with the save.
                makeToast("Something went wrong when saving new product!");
            } else {
                //Otherwise the save was successful.
                makeToast("Product saved successfully!");
            }

        } else {
            //Update existing product
            int updatedRows = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            //Show toast
            if (updatedRows == 0) {
                //If no rows where affected, something went wrong with the update.
                makeToast("Something went wrong when updating new product!");
            } else {
                //Otherwise the save was successful.
                makeToast("Product updated successfully!");
            }
        }
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add menu to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //Hide menu if in Add Product mode
        if (mCurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.edit_menu_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Actions for each menu item.
        switch (item.getItemId()) {
            //Save option clicked
            case R.id.edit_menu_save:
                try {
                    //Save product to database
                    saveProduct();
                    //Exit activity
                    finish();
                    return true;
                } catch (NumberFormatException nfe){
                    makeToast("Please enter a valid number");
                } catch (IllegalArgumentException iae) {
                    makeToast(String.valueOf(iae));
                }
            break;
            case R.id.edit_menu_delete:
                //Show delete confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                //Navigate back to parent activity if there are no changes
                if (!mHasProductChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                //If there are changes, show warning dialog.
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //User clicked Discard button.
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                };
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Allow back press if there are no unsaved changes.
        if  (!mHasProductChanged) {
            super.onBackPressed();
            return;
        }

        //Warn the user if there are unsaved changes
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //User clicked Discard.
                finish();
            }
        };

        //Show warning dialog for unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //Create an alert dialog for unsaved changes.
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to discard unsaved changes?");
        alertBuilder.setPositiveButton("Exit", discardButtonClickListener);
        alertBuilder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    //Closes the dialog and keeps editing
                    dialog.dismiss();
                }
            }
        });

        //Show the AlertDialog
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to delete this product?");
        alertBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //User pressed Delete button.
                deleteProduct();
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

    private void deleteProduct() {
        //Check if there is an existing product
        if (mCurrentInventoryUri != null) {
            //Delete the product
            int deletedRows = getContentResolver().delete(mCurrentInventoryUri, null, null);

            //Show toast
            if (deletedRows == 0) {
                //If no rows were deleted, there was a problem.
                makeToast("There was a problem deleting the product.");
            } else {
                //Otherwise the deletion was successful.
                makeToast("Product deleted successfully!");
            }
        }

        //Close activity
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Make a projection with all product attributes.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_NUMBER };

        //Send to database in the background
        return new CursorLoader(this, mCurrentInventoryUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Return if the cursor is empty
        if (data == null || data.getCount() < 1) {
            return;
        }

        //Read data from cursor
        if (data.moveToFirst()) {
            //Read relevant product attributes from cursor
            int productNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierNumberColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NUMBER);

            //Extract values from cursor
            String productName = data.getString(productNameColumnIndex);
            int productPrice = data.getInt(productPriceColumnIndex);
            int productQuantity = data.getInt(productQuantityColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierNumber = data.getString(supplierNumberColumnIndex);

            //Update Edit Views
            mProductNameEditText.setText(productName);
            mProductPriceEditText.setText(Integer.toString(productPrice));
            mProductQuantityEditText.setText(Integer.toString(productQuantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierNumberEditText.setText(supplierNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Clear all data from loader.
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierNumberEditText.setText("");
    }
}
