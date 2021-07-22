package com.example.project_28_02_2021.util.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.project_28_02_2021.site.Site;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    protected final ArrayList<String> exe_rows = new ArrayList<>();
    protected final Context context;

    public static String tableName
            = "sites_table";
    public static String databaseName
            = "sites_base.db";
    public static String assetName
            = "sites_table.sql";

    public static String deleteQuery
            = "delete from " + tableName + " where name = '%s';";
    public static String addQuery
            = "insert into " + tableName + " values ('%s', '%s');";
    public static String selectQuery
            = "select name, url from " + tableName;

    public static String createQuery
            = "create table " + tableName + "\n" +
            "(" + "\n" +
            "    name text not null, " + "\n" +
            "    url  text not null " + "\n" +
            ");";

    public DataBaseHelper(Context context, ArrayList<String> rows) {
        super(context, databaseName, null, 1);
        this.exe_rows.addAll(rows);
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
        for (String row : exe_rows)
            db.execSQL(row);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<Site> getSites() {
        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        ArrayList<Site> sites = new ArrayList<>();
        final String name_str = "name";
        final String url_str = "url";
        while (cursor.moveToNext()) {
            int nameId = cursor.getColumnIndex(name_str);
            int urlId = cursor.getColumnIndex(url_str);
            String name = cursor.getString(nameId);
            String url = cursor.getString(urlId);
            Site new_site = new Site(name, url, context);
            sites.add(new_site);
        }
        cursor.close();
        return sites;
    }
}