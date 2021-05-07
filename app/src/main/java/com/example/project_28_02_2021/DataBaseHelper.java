package com.example.project_28_02_2021;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    protected final ArrayList<String> rows = new ArrayList<>();
    protected final Context           context;

    public static String deleteQuery
            = "delete from sites_table where name = '%s';";
    public static String addQuery
            = "insert into sites_table values ('%s', '%s');";
    public static String selectQuery
            = "select name, url from sites_table";
    public static String createQuery
            = "create table sites_table\n(\n    name text not null,\n    url  text not null\n);";

    public DataBaseHelper(Context context, ArrayList<String> rows) {
        super(context, "sites_base.db", null, 1);
        this.rows.addAll(rows);
        this.context = context;
    }

    public void deleteSite(Site site) {
        getReadableDatabase().execSQL(String.format(deleteQuery, site.getName()));
    }

    public void addSite(Site site) {
        getReadableDatabase().execSQL(String.format(addQuery, site.getName(), site.getUrl()));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createQuery);
        for (String row : rows) { db.execSQL(row); }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<Site> getSites() {
        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        ArrayList<Site> sites = new ArrayList<>();
        while (cursor.moveToNext()) {
            int nameId = cursor.getColumnIndex("name");
            int urlId = cursor.getColumnIndex("url");
            String name = cursor.getString(nameId);
            String url = cursor.getString(urlId);
            sites.add(new Site(name, url, context));
        }
        cursor.close();
        return sites;
    }
}