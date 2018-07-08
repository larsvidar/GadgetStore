package no.larsvidar.gadgetstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
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

/**
 * Create new product, or update existing product
 */
public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //*** Variables ***
    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentInventoryUri;

    //Assigning EditText Views to variables
    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierNumberEditText;

    //Variable to keep track of changes in product.
    private boolean mHasProductChanged = false;

    //Variable to keep track of Quantity
    private int mQuantity = 0;

    //OnTouchListener listening for any user interaction on a view.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mHasProductChanged = true;
            return false;
        }
    };

    /**
     * OnCreate method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //Creating new intent
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        //Determining if we should be in add or update mode.
        if (mCurrentInventoryUri == null) {
            setTitle(getString(R.string.add_product_mode));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_product_mode));
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        //Assign all relevant Edit Views to variables
        mProductNameEditText = findViewById(R.id.edit_product_name);
        mProductPriceEditText = findViewById(R.id.edit_product_price);
        mProductQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierNumberEditText = findViewById(R.id.edit_supplier_number);

        //Set number in Quntity Edit View.
        mProductQuantityEditText.setText(Integer.toString(mQuantity));

        //Set up OnTouchListener for each EditView.
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierNumberEditText.setOnTouchListener(mTouchListener);

        //Assign Button views to variables
        Button addQuantityButton = findViewById(R.id.edit_button_add_quantity);
        Button subtractQuantityButton = findViewById(R.id.edit_button_subtract_quantity);

        //Set up OnTouchListener for buttons
        addQuantityButton.setOnTouchListener(mTouchListener);
        subtractQuantityButton.setOnTouchListener(mTouchListener);

        //Set Add Button ClickListener
        addQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
                mProductQuantityEditText.setText(Integer.toString(mQuantity));
            }
        });

        //Set Subtract Button Click Listener
        subtractQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0) {
                    mQuantity--;
                    mProductQuantityEditText.setText(Integer.toString(mQuantity));
                }
            }
        });

        //Make dial-button open phone-app
        Button callSupplierButton = findViewById(R.id.edit_call_button);
        callSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialNumber(getBaseContext(), mSupplierNumberEditText.getText().toString());
            }
        });

    }

    /**
     * Method for saving new or updated product to database
     */
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
                makeToast(getString(R.string.add_product_fail));
            } else {
                //Otherwise the save was successful.
                makeToast(getString(R.string.add_product_success));
            }

        } else {
            //Update existing product
            int updatedRows = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            //Show toast
            if (updatedRows == 0) {
                //If no rows where affected, something went wrong with the update.
                makeToast(getString(R.string.edit_product_fail));
            } else {
                //Otherwise the save was successful.
                makeToast(getString(R.string.edit_product_success));
            }
        }
    }

    /**
     * Method for making Toast messages
     * @param text to be displayed
     */
    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Creates options menu in app bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add menu to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    /**
     * Hides delete-option when adding a new product.
     * @param menu
     * @return
     */
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

    /**
     * Method for determening which option has been pressed
     * @param item
     * @return
     */
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
                    makeToast(iae.getMessage());
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
                //Warn user that there are unsaved changes.
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Triggered when back is pressed on phone.
     */
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

    /**
     * Shows warning dialog when trying to exit when there are unsaved changes.
     * @param discardButtonClickListener
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //Create an alert dialog for unsaved changes.
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.dialog_unsaved_changes));
        alertBuilder.setPositiveButton(getString(R.string.dialog_unsaved_changes_positive), discardButtonClickListener);
        alertBuilder.setNegativeButton(getString(R.string.dialog_unsaved_changes_negative), new DialogInterface.OnClickListener() {
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

    /**
     * Shows warning when deleting product
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.dialog_delete_product));
        alertBuilder.setPositiveButton(getString(R.string.dialog_delete_product_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //User pressed Delete button.
                deleteProduct();
            }
        });
        alertBuilder.setNegativeButton(getString(R.string.dialog_delete_product_negative), new DialogInterface.OnClickListener() {
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

    /**
     * Method for deleting specific product
     */
    private void deleteProduct() {
        //Check if there is an existing product
        if (mCurrentInventoryUri != null) {
            //Delete the product
            int deletedRows = getContentResolver().delete(mCurrentInventoryUri, null, null);

            //Show toast
            if (deletedRows == 0) {
                //If no rows were deleted, there was a problem.
                makeToast(getString(R.string.dialog_delete_product_confirmation_fail));
            } else {
                //Otherwise the deletion was successful.
                makeToast(getString(R.string.dialog_delete_product_confirmation_success));
            }
        }
        //Close activity
        finish();
    }

    /**
     * Method for starting Dial intent
     * @param context
     * @param number to be called
     */
    public void dialNumber(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        context.startActivity(intent);
    }

    /**
     * OnCreateLoader
     * @param id
     * @param args
     * @return
     */
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

    /**
     * OnLoadFinish
     * @param loader
     * @param data
     */
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
            mQuantity = data.getInt(productQuantityColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierNumber = data.getString(supplierNumberColumnIndex);

            //Update Edit Views
            mProductNameEditText.setText(productName);
            mProductPriceEditText.setText(Integer.toString(productPrice));
            mProductQuantityEditText.setText(Integer.toString(mQuantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierNumberEditText.setText(supplierNumber);
        }
    }

    /**
     * OnLoaderReset
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Clear all data from loader.
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierNumberEditText.setText("");

        mQuantity = 0;
    }
}
