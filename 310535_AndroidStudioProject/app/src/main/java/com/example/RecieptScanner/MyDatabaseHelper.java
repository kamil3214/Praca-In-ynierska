package com.example.RecieptScanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "purchases209.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME_PURCHASES = "my_purchases";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PRODUCT = "product_name";
    private static final String COLUMN_PRICE = "product_price";
    private static final String COLUMN_DATE = "purchase_date";

    private static final String COLUMN_CATEGORY = "product_category";

    private static final String TABLE_NAME_CATEGORIES = "my_categories";


    final String VISIBLE_FORMAT = "dd-MM-yyyy";
    final String HIDDEN_FORMAT = "yyyy-MM-dd";


    MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_PURCHASES +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT + " TEXT, " +
                COLUMN_PRICE + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_DATE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_NAME_CATEGORIES +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY + " TEXT UNIQUE);";
        db.execSQL(query);

        query = "CREATE TABLE app_settings (setting_name TEXT UNIQUE, set_value INTEGER);";
        db.execSQL(query);

        query = "INSERT INTO app_settings (setting_name,set_value) VALUES('include_discounts', 0);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PURCHASES);
        onCreate(db);
    }

    void addPurchase(String product, String price, String category,String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PRODUCT, product);
        cv.put(COLUMN_PRICE, price);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_DATE, date);
        long result = db.insert(TABLE_NAME_PURCHASES,null, cv);
    }

    void addCategory(String category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CATEGORY, category);

        long result = db.insert(TABLE_NAME_CATEGORIES,null, cv);
        if(result == -1){
            Toast.makeText(context, "Kategoria o tej nazwie już istnieje", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Dodano", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor readAllData(String date_start, String date_end, int sort_option){
        String query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                " ORDER BY purchase_date DESC;";
        if(sort_option==0){
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                    " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                    " ORDER BY purchase_date DESC;";

        } else if (sort_option==1) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                    " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                    " ORDER BY purchase_date ASC;";
        } else if (sort_option==2) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                    " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                    " ORDER BY CAST(product_price AS decimal) DESC;";
        } else if (sort_option==3) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                    " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                    " ORDER BY CAST(product_price AS decimal) ASC;";
        } else if (sort_option==4) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                    " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                    " ORDER BY product_name COLLATE NOCASE DESC;";
        }   else if (sort_option==5) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES +
                    " WHERE " + COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+"'"+
                    " ORDER BY product_name COLLATE NOCASE ASC;";
        }
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readAllDataWithCategory(String category, String date_start, String date_end, int sort_option){
        String query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                       +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                "' ORDER BY purchase_date DESC;";
        if(sort_option==0){
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                    +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                    "' ORDER BY purchase_date DESC;";

        } else if (sort_option==1) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                    +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                    "' ORDER BY purchase_date ASC;";

        } else if (sort_option==2) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                    +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                    "' ORDER BY CAST(product_price AS decimal) DESC;";

        } else if (sort_option==3) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                    +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                    "' ORDER BY CAST(product_price AS decimal) ASC;";
        } else if (sort_option==4) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                    +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                    "' ORDER BY product_name COLLATE NOCASE DESC;";
        }   else if (sort_option==5) {
            query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " WHERE " + COLUMN_CATEGORY + " = '"+category+"'"
                    +" AND "+ COLUMN_DATE +" >= '" + date_start + "' AND "+ COLUMN_DATE +" <= '" + date_end+
                    "' ORDER BY product_name COLLATE NOCASE ASC;";
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readAllDataCategories(){
        String query = "SELECT * FROM " + TABLE_NAME_CATEGORIES+" ORDER BY product_category COLLATE NOCASE ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    void updateData(String row_id, String product_name, String product_price, String product_category, String purchase_date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PRODUCT, product_name);
        cv.put(COLUMN_PRICE, product_price);
        cv.put(COLUMN_CATEGORY, product_category);
        cv.put(COLUMN_DATE, purchase_date);

        long result = db.update(TABLE_NAME_PURCHASES, cv, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Coś poszło nie tak", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Zaktualizowano", Toast.LENGTH_SHORT).show();
        }

    }

    void updateCategories(String row_id, String product_category){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME_CATEGORIES + " WHERE " + COLUMN_ID + " = '"+row_id+"';";

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        String category_to_update = "";
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                category_to_update = cursor.getString(1); //

            }
            cursor.close();
            Log.d("as1",category_to_update);
        }

        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CATEGORY, product_category);

        try {


            long result = db.update(TABLE_NAME_CATEGORIES, cv, "_id=?", new String[]{row_id});
            if (result == -1) {
                Toast.makeText(context, "Kategoria o tej nazwie już istnieje", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Zaktualizowano", Toast.LENGTH_SHORT).show();
            }

            db = this.getWritableDatabase();
            if(category_to_update!=""){

                cv = new ContentValues();

                cv.put(COLUMN_CATEGORY, product_category);
                db.update(TABLE_NAME_PURCHASES, cv, "product_category=?", new String[]{category_to_update});

            }

        }catch (Exception e){
            Toast.makeText(context, "Kategoria o tej nazwie już istnieje", Toast.LENGTH_SHORT).show();
        }

    }

    void deleteOneRowPurchases(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME_PURCHASES, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Coś poszło nie tak", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Usunięto", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteOneRowCategories(String row_id){
        SQLiteDatabase db = this.getReadableDatabase();


        String query = "SELECT * FROM " + TABLE_NAME_CATEGORIES + " WHERE " + COLUMN_ID + " = '"+row_id+"';";

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        String category_to_delete = "";
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                category_to_delete = cursor.getString(1); //

            }
            cursor.close();
            Log.d("as",category_to_delete);
        }



        db = this.getWritableDatabase();

        long result = db.delete(TABLE_NAME_CATEGORIES, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Coś poszło nie tak", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Usunięto", Toast.LENGTH_SHORT).show();
        }


        db = this.getWritableDatabase();
        if(category_to_delete!=""){

            ContentValues cv = new ContentValues();

            cv.put(COLUMN_CATEGORY, "");
            db.update(TABLE_NAME_PURCHASES, cv, "product_category=?", new String[]{category_to_delete});

        }








    }

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME_PURCHASES);
        Toast.makeText(context, "Usunięto wszystkie zakupy", Toast.LENGTH_SHORT).show();

    }

    void deleteAllCategories(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME_CATEGORIES);
        db.execSQL("UPDATE " + TABLE_NAME_PURCHASES+ " SET " +COLUMN_CATEGORY+ "=''");
        Toast.makeText(context, "Usunięto wszystkie kategorie", Toast.LENGTH_SHORT).show();

    }


    String getMinDate(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " ORDER BY purchase_date ASC LIMIT 1;";

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        String min_date = "Data od";
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                min_date = cursor.getString(4);

                SimpleDateFormat sdf = new SimpleDateFormat(HIDDEN_FORMAT);
                Date d;
                try {
                    d = sdf.parse(min_date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                sdf.applyPattern(VISIBLE_FORMAT);
                min_date = sdf.format(d);


            }
            cursor.close();

        }

        return min_date;
    }


    String getMaxDate(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_PURCHASES + " ORDER BY purchase_date DESC LIMIT 1;";

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        String max_date = "Data do";
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                max_date = cursor.getString(4);

                SimpleDateFormat sdf = new SimpleDateFormat(HIDDEN_FORMAT);
                Date d;
                try {
                    d = sdf.parse(max_date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                sdf.applyPattern(VISIBLE_FORMAT);
                max_date = sdf.format(d);


            }
            cursor.close();

        }

        return max_date;
    }

    int getIncludeDiscoutsValue(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery("SELECT * FROM app_settings WHERE setting_name='include_discounts'", null);
        }

        int set_value = 0;
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                set_value = cursor.getInt(1); //

            }
            cursor.close();
            Log.d("SuperLog",String.valueOf(set_value));
        }

        return set_value;
    }

    void updateIncludeDiscoutsValue(int switch_value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("set_value", switch_value);
        db.update("app_settings", cv, "setting_name=?", new String[]{"include_discounts"});
    }
}
