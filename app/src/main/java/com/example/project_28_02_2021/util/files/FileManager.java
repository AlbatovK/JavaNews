package com.example.project_28_02_2021.util.files;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.project_28_02_2021.util.database.DataBaseHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {

    public static String readFile(Context context, String name) throws FileNotFoundException {
        context.openFileOutput(name, Context.MODE_APPEND);
        InputStream stream = context.openFileInput(name);
        StringBuilder gotXml = new StringBuilder();
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNext())
            gotXml.append(scanner.next());
        return gotXml.toString();
    }


    public static void deleteFile(Context context, String name) {
        context.deleteFile(name);
    }

    public static ArrayList<String> getAssetData(AssetManager manager) throws IOException {
        ArrayList<String> exe_rows = new ArrayList<>();
        InputStream inputStream = manager.open(DataBaseHelper.assetName);
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) exe_rows.add(scanner.nextLine());
        inputStream.close();
        scanner.close();
        return exe_rows;
    }

    public static String liked_news_storage
            = "news.xml";
    public static String deleted_news_storage
            = "read.xml";
    public static String tags_storage
            = "tags.xml";
}
