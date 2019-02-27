package com.vinhteam.sendsms;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.provider.ContactsContract.CommonDataKinds.*;
public class ContactPickerMulti extends AppCompatActivity {

    Button ok, okAll;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_picker_multi);

        lv = findViewById(R.id.lv_contacts);
        ok = findViewById(R.id.cmdOK);
        okAll = findViewById(R.id.cmdOKAll);

        Cursor mCursor = getContacts();
        startManagingCursor(mCursor);
        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                mCursor,
                 new String[] { ContactsContract.Contacts.DISPLAY_NAME },
                new int[] { android.R.id.text1 });

        lv.setAdapter(adapter);
        lv.setItemsCanFocus(false);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long[] id = lv.getCheckedItemIds();//  i get the checked contact_id instead of position
                String [] phoneNumber = new String[id.length];
                for (int i = 0; i < id.length; i++) {

                    phoneNumber[i] = getPhoneNumber(id[i]);// getPhoneNumber(id[i]); // get phonenumber from selected id

                }

                Intent pickContactIntent = new Intent();
                pickContactIntent.putExtra("PICK_CONTACT", phoneNumber);// Add checked phonenumber in intent and finish current activity.
                setResult(RESULT_OK, pickContactIntent);
                finish();
            }
        });

        okAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long[] id = lv.getCheckedItemIds();//  i get the checked contact_id instead of position
                String [] phoneNumber = new String[id.length];
                for (int i = 0; i < id.length; i++) {

                    phoneNumber[i] = getPhoneNumberAndName(id[i]);// getPhoneNumber(id[i]); // get phonenumber from selected id

                }

                Intent pickContactIntent = new Intent();
                pickContactIntent.putExtra("PICK_CONTACT", phoneNumber);// Add checked phonenumber in intent and finish current activity.
                setResult(RESULT_OK, pickContactIntent);
                finish();
            }
        });
    }
    private Cursor queryPhoneNumbers(long contactId) {
        ContentResolver cr = getContentResolver();
        Uri baseUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                contactId);
        Uri dataUri = Uri.withAppendedPath(baseUri,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY);

        Cursor c = cr.query(dataUri, new String[] { Phone._ID,
                        Phone.NUMBER,
                        Phone.IS_SUPER_PRIMARY, ContactsContract.RawContacts
                        .ACCOUNT_TYPE, Phone.TYPE,
                        Phone.DISPLAY_NAME }, ContactsContract.Data.MIMETYPE + "=?",
                new String[] { Phone.CONTENT_ITEM_TYPE }, null);
        if (c != null && c.moveToFirst()) {
            return c;
        }
        return null;
    }
    private String getPhoneNumber(long id) {
        String phone = null;
        Cursor phonesCursor = null;
        phonesCursor = queryPhoneNumbers(id);
        if (phonesCursor == null || phonesCursor.getCount() == 0) {
            // No valid number
            //signalError();
            return null;
        } else if (phonesCursor.getCount() == 1) {
            // only one number, call it.
            phone = phonesCursor.getString(phonesCursor
                    .getColumnIndex(Phone.NUMBER));
        } else {
            phonesCursor.moveToPosition(-1);
            while (phonesCursor.moveToNext()) {

                // Found super primary, call it.
                phone = phonesCursor.getString(phonesCursor
                        .getColumnIndex(Phone.NUMBER));
                break;

            }
        }

        return phone;
    }

    private String getPhoneNumberAndName(long id) {
        String phone = null;
        Cursor phonesCursor = null;
        phonesCursor = queryPhoneNumbers(id);
        String name;
        if (phonesCursor == null || phonesCursor.getCount() == 0) {
            // No valid number
            //signalError();
            return null;
        } else if (phonesCursor.getCount() == 1) {
            // only one number, call it.
            int indexName = phonesCursor.getColumnIndex(Phone.DISPLAY_NAME);
            name = phonesCursor.getString(indexName);
            phone = phonesCursor.getString(phonesCursor
                    .getColumnIndex(Phone.NUMBER))+" "
                    + ((name != null)?name:"");
        } else {
            phonesCursor.moveToPosition(-1);
            int indexName = phonesCursor.getColumnIndex(Phone.DISPLAY_NAME);
            while (phonesCursor.moveToNext()) {
                name = phonesCursor.getString(indexName);
                // Found super primary, call it.
                phone = phonesCursor.getString(phonesCursor
                        .getColumnIndex(Phone.NUMBER))  +" "
                        + ((name != null)?name:"");
                break;

            }
        }

        return phone;
    }
    private Cursor getContacts() {
        // Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME };
        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '"
                + ("1") + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, selectionArgs,
                sortOrder);
    }
}
