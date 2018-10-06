package com.beyond.fly.note3.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by beyond on 2018/1/3.
 */

public class ContactUtils {

    public String getContactsName(Context context,String phoneNumber){
        String name;
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,phoneNumber);
        Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
        if (cur!=null){
            cur.moveToFirst();
            name= cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cur.close();
            return name;
        }
        return "";
    }
    public String[] getContactsNumbers(Context context){
        String[] name;
        Uri uri=ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
        if (cur!=null){
            cur.moveToFirst();
            name=new String[cur.getCount()];
            int i=0;
            while (cur.moveToNext()) {
                name[i] = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                i++;
            }
            cur.close();
            return name;
        }
        return null;
    }
}
