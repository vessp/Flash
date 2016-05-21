package com.vessp.flash;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.vessp.flash.data.AttrMap;
import com.vessp.flash.data.IQuestion;
import com.vessp.flash.data.TransitiveLink;
import com.vessp.flash.support.Ops;
import com.vessp.flash.support.Tracer;
import com.vessp.flash.support.Tracer.TT;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 *  Database commands.
 */
public class FlashDB
{
    private static final File DB_DIR = new File(Environment.getExternalStorageDirectory(), "vflash");
    private static final String DB_NAME = "FlashDB.db";

    private File currentDB = new File(DB_DIR, DB_NAME);
    private SQLiteDatabase _db;

    public static final String TABLE_NAME_TRANSLATIONS = "Translations";

    public SQLiteDatabase db()
    {
        if(_db == null)
            _db = SQLiteDatabase.openDatabase(currentDB.toString(), null, SQLiteDatabase.OPEN_READWRITE);
        return _db;
    }

    public FlashDB(Context context) throws IOException
    {
        if(!DB_DIR.exists())
        {
            DB_DIR.mkdir();
            MediaScannerConnection.scanFile(context, new String[]{DB_DIR.toString()}, null, null);
        }

        if(!currentDB.exists())
        {
            currentDB.createNewFile();
            File backup = getLatestBackupDbFile();
            if(backup != null)
                copyFile(getLatestBackupDbFile(), currentDB);
            MediaScannerConnection.scanFile(context, new String[]{currentDB.toString()}, null, null);
        }

        traceTables();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------

    public boolean hasTable(String tableName)
    {
        Cursor c = db().rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);

        if (c.moveToFirst()) {
            return c.getString(0).equals(tableName);
        }
        return false;
    }

    public void createTable(String tableName, AttrMap attrs)
    {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        sqlCreateTable += BaseColumns._ID + " INTEGER PRIMARY KEY";

        for(String key : attrs.keySet())
        {
            if(key.equals(BaseColumns._ID))
                continue;

            Object val = attrs.get(key);
            String colType = "TEXT";
            if(val instanceof Integer || val instanceof Long)
                colType = "INTEGER";
            else if(val instanceof Float || val instanceof Double)
                colType = "REAL";

            sqlCreateTable += ", " + key + " " + colType;
        }

        sqlCreateTable += " )";
        db().execSQL(sqlCreateTable);
    }

    public void dropTable(String tableName)
    {
        db().execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    public int push(String tableName, AttrMap attrs)
    {
        SQLiteDatabase db = db();

        ContentValues values = new ContentValues();
        for(String key : attrs.keySet())
        {
            if(key.equals(BaseColumns._ID))
                continue;

            Object val = attrs.get(key);
            values.put(key, val!=null?val.toString():null);
        }

        int _id = (int)attrs.get(BaseColumns._ID);
        if(_id == -1)
        {
            return (int) db.insertOrThrow(
                    tableName,
                    null,
                    values);
        }
        else
        {
            String[] whereArgs = {};
            int numRowsUpdated = db.update(
                    tableName,
                    values,
                    BaseColumns._ID + "=" + _id,
                    whereArgs
            );

            if(numRowsUpdated == 0)
            {
                Tracer.e("unable to update db row in table=" + tableName + " with id=" + _id, TT.FLASH_DB);
            }
        }
        return -1;
    }

    public List<AttrMap> pullTableRows(String tableName)
    {
        List<AttrMap> list = new ArrayList<>();

        Cursor cursor = queryFullTable(tableName);

        if(cursor.moveToFirst())
        {
            while (true)
            {
                AttrMap attrs = new AttrMap();

                for(int i=0; i<cursor.getColumnCount(); i++)
                {
                    String colName = cursor.getColumnName(i);
                    int colType = cursor.getType(i);
                    switch(colType)
                    {
                        case Cursor.FIELD_TYPE_NULL:
                        case Cursor.FIELD_TYPE_STRING:
                            attrs.put(colName, cursor.getString(i));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            attrs.put(colName, cursor.getLong(i));//TODO how to check int vs long? instant columns fail to pull properly if i use getInt() here
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            attrs.put(colName, cursor.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            attrs.put(colName, cursor.getBlob(i));
                            break;
                    }
                }

                list.add(attrs);

                if (!cursor.moveToNext())
                    break;
            }
        }

        return list;
    }

    public void createTableForClass(Class c, String tableName)
    {
        String sqlCreateTable = "CREATE TABLE " + tableName + " (";
        sqlCreateTable += BaseColumns._ID + " INTEGER PRIMARY KEY";

        List<Field> fields = getFields(c, false);
        for(int i=0; i<fields.size(); i++)
        {
            Field f = fields.get(i);

            String colType = "TEXT";
            if(f.getType() == int.class || f.getType() == long.class)
                colType = "INTEGER";

            sqlCreateTable += ", " + f.getName() + " " + colType;
        }

        sqlCreateTable += " )";

        db().execSQL(sqlCreateTable);
    }

    public void createTableForClass(Class c)
    {
        createTableForClass(c, c.getSimpleName());
    }

//    public void dropTables()
//    {
//        db().execSQL("DROP TABLE IF EXISTS " + Translation.class.getSimpleName());
//        db().execSQL("DROP TABLE IF EXISTS " + TransitiveLink.class.getSimpleName());
//    }

    public void traceTables()
    {
        Cursor c = db().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            Tracer.d("---DB Tables---", TT.FLASH_DB);
            while ( !c.isAfterLast() ) {
                Tracer.d("" + c.getString(0), TT.FLASH_DB);
                c.moveToNext();
            }
        }
        else
        {
            Tracer.d("NO DATABASE TABLES", TT.FLASH_DB);
        }
        Tracer.d("---------------", TT.FLASH_DB);
        c.close();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------

    public int insert(Object o) throws IllegalAccessException
    {
        SQLiteDatabase db = db();

        ContentValues values = new ContentValues();
        List<Field> fields = getFields(o.getClass(), false);
        for(Field f : fields)
        {
            f.setAccessible(true);
            Object value = f.get(o);
            values.put(f.getName(), value!=null?value.toString():null);
        }

        return (int) db.insertOrThrow(
                o.getClass().getSimpleName(),
                null,
                values);
    }

    public void update(IQuestion o) throws IllegalAccessException
    {
        SQLiteDatabase db = db();

        ContentValues values = new ContentValues();
        List<Field> fields = getFields(o.getClass(), false);
        for(Field f : fields)
        {
            Class fc = f.getType();
            String fName = f.getName();

            f.setAccessible(true);
            Object value = f.get(o);
            values.put(f.getName(), value!=null?value.toString():null);
        }

        String[] whereArgs = {};
        db.update(
                o.getClass().getSimpleName(),
                values,
                "_id=" + -1,  //o.id(),
                whereArgs
        );
    }

    public List<TransitiveLink> queryTransitiveLinks()
    {
        List<TransitiveLink> list = new ArrayList<>();

        Cursor cursor = queryFullTable(TransitiveLink.class.getSimpleName());
        if(cursor.moveToFirst())
        {
            while (true)
            {
                TransitiveLink tLink = rowToObject(TransitiveLink.class, cursor);
                if(tLink != null)
                {
                    tLink.trans = FlashApp.inst().getTranslationFromId(tLink.transId);
                    tLink.intrans = FlashApp.inst().getTranslationFromId(tLink.intransId);
                    list.add(tLink);
                }

                if (!cursor.moveToNext())
                    break;
            }
        }

        return list;
    }

    public <T> List<T> queryListOfClass(Class<T> c)
    {
        List<T> list = new ArrayList<>();

        Cursor cursor = queryFullTable(c.getSimpleName());

        if(cursor.moveToFirst())
        {
            while (true)
            {
                T o = rowToObject(c, cursor);
                if(o != null)
                    list.add(o);

                if (!cursor.moveToNext())
                    break;
            }
        }

        return list;
    }

    public Cursor queryFullTable(String tableName)
    {
        String[] colsToReturn = null; //{"kanji"};
        String[] selArgs = {};

        Cursor cursor = db().query(
                tableName,  // The table to queryFullTable
                colsToReturn,       // The columns to return
                null,               // The columns for the WHERE clause
                selArgs,            // The values for the WHERE clause
                null,               // don't group the rows
                null,               // don't filter by row groups
                null// FeedEntry.COLUMN_NAME_UPDATED + " DESC" // The sort order
        );

        return cursor;
    }



    public static <T> T rowToObject(Class<T> t, Cursor cursor)
    {
        try
        {
            T o = t.newInstance();

            List<Field> fields = getFields(o.getClass(), true);
            for (Field f : fields)
            {
                f.setAccessible(true);
                Class fieldType = f.getType();
                int colIndex = cursor.getColumnIndexOrThrow(f.getName());
                if (fieldType == int.class)
                {
                    f.setInt(o, cursor.getInt(colIndex));
                } else if (fieldType == long.class)
                {
                    f.setLong(o, cursor.getLong(colIndex));
                } else if (fieldType.isEnum())
                    {
                    String value = cursor.getString(colIndex);
                    if (value != null && value.length() != 0)
                        f.set(o, Enum.valueOf(fieldType, value));
                } else
                {
                    f.set(o, cursor.getString(colIndex));
                }
            }

            return o;
        }
        catch(Exception e)
        {
            Tracer.e(e);
        }

        return null;
    }

    public static List<Field> getFields(Class c, boolean includeId)
    {
        List<Field> fields = new ArrayList<>();
        while(true)
        {
            if(c == null)
                break;

            Field[] fs = c.getDeclaredFields();
            for(Field f : fs)
            {
                if(Modifier.isTransient(f.getModifiers()) || (f.getName().equals(BaseColumns._ID) && !includeId))
                    continue;

                fields.add(f);
            }

            c = c.getSuperclass();
        }
        return fields;
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------

    public File backup(Context context) throws IOException
    {
        String backupDbName = DB_NAME.split("\\.")[0] + "_" + DateTime.now().getMillis() + "." + DB_NAME.split("\\.")[1];
        File backupFile = new File(DB_DIR, backupDbName);
        copyFile(currentDB, backupFile);
        MediaScannerConnection.scanFile(context, new String[]{backupFile.toString()}, null, null);
        listFiles(DB_DIR);
        return backupFile;
    }

    private File getLatestBackupDbFile()
    {
        List<File> files = Ops.asList(DB_DIR.listFiles());
        for(int i=0; i<files.size(); i++)
        {
            if(files.get(i).getName().equals(DB_NAME))
            {
                files.remove(i);
                i--;
            }
        }

        Collections.sort(files, new Comparator<File>()
        {
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        return files.size() == 0 ? null : files.get(files.size() - 1);
    }

    private void listFiles(File dir)
    {
        File[] files = dir.listFiles();
        Tracer.d(dir.toString() + ": ", TT.FLASH_DB);
        for(File f : files)
           Tracer.d(f.getName(), TT.FLASH_DB);
        Tracer.d("------------------------------", TT.FLASH_DB);
    }

    public static void copyFile(File fromFile, File toFile) throws IOException
    {
        FileChannel src = new FileInputStream(fromFile).getChannel();
        FileChannel dst = new FileOutputStream(toFile).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.db();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the queryFullTable
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the queryFullTable results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
