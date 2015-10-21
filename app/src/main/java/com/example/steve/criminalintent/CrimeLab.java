package com.example.steve.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.steve.criminalintent.database.CrimeBaseHelper;
import com.example.steve.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Steve on 10/7/2015.
 */
public class CrimeLab {
    private static CrimeLab myInstance;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab getInstance(Context context) {
        if(myInstance == null){
            myInstance = new CrimeLab(context);
        }
        return myInstance;
    }

    private CrimeLab(Context context) {
        //avoid activity referred by context never be cleaned up,
        // notice that CrimeLab is a static class
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public void addCrime(Crime c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id){

        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?", new String[] {id.toString()});
        try{
            if(cursor.getCount() == 0) return null;
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[] {uuidString});
    }

    public File getPhotoFile(Crime crime){
        File externalFileDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(externalFileDir == null){
            return null;
        }
        return new File(externalFileDir, crime.getPhotoFilename());
    }

    /*----------------private methods--------------------------*/

    private CrimeCursorWrapper queryCrimes(String whereCaluse, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, //columns: null selects all columns
                whereCaluse,
                whereArgs,
                null,   //groupby
                null,   //having
                null //orderby
        );

        return new CrimeCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        values.put(CrimeTable.Cols.CONTACT_ID, crime.getIdSuspect());

        return values;
    }

}
