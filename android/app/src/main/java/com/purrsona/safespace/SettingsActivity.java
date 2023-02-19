package com.purrsona.safespace;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {
    private int REQUEST_CONTACT = 121;

    private ListView listViewContacts;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        listViewContacts = (ListView) findViewById(R.id.list_view_contacts);
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        listViewContacts.setAdapter(adapter);

        JSONArray contactsArray = getContactsList();
        for (int i = 0; i < contactsArray.length(); i++) {
            try {
                JSONObject contact = contactsArray.getJSONObject(i);
                adapter.add(contact.getString("name") + " (" + contact.getString("number") + ")");
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        findViewById(R.id.button_add_contact).setOnClickListener(view -> {
            if (hasReachMaxContactsLimit()) {
                Toast.makeText(this, "You've already reached the max contacts limit!", Toast.LENGTH_SHORT).show();
                return;
            }
            final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(pickContact, REQUEST_CONTACT);
        });

        findViewById(R.id.button_clear).setOnClickListener(view -> {
            clearAllContacts();
            adapter.clear();
            adapter.notifyDataSetChanged();
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        Uri contactData = data.getData();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(contactData, null, null, null, null);
        if (cur.getCount() > 0) {// thats mean some resutl has been found
            if(cur.moveToNext()) {
                int t;
                String id = cur.getString((t = cur.getColumnIndex(ContactsContract.Contacts._ID)) == -1 ? 0 : t);
                String name = cur.getString((t = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) == -1 ? 0 : t);

                if (Integer.parseInt(cur.getString((t = cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == -1 ? 0 : t)) > 0)
                {
                    // Query phone here. Covered next
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString((t = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) == -1 ? 0 : t);

                        addNewContact(name, phoneNumber);
                        adapter.add(name + "(" + phoneNumber + ")");
                        adapter.notifyDataSetChanged();

                        break;
                    }
                    phones.close();
                }

            }
        }
        cur.close();

    }

    public void addNewContact(String name, String number) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("number", number);

            JSONArray array = new JSONArray(getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE).getString("contacts", "[]"));
            array.put(obj);

            getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE).edit().putString("contacts", array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean hasReachMaxContactsLimit() {
        try {
            JSONArray array = new JSONArray(getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE).getString("contacts", "[]"));
            return array.length() > Constants.COUNT_MAX_CONTACTS;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONArray getContactsList() {
        try {
            return new JSONArray(getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE).getString("contacts", "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public void clearAllContacts() {
        getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE).edit().putString("contacts", "[]").apply();
    }

}