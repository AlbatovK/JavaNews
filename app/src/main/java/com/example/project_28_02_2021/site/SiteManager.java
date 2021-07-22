package com.example.project_28_02_2021.site;

import android.content.Context;

import com.example.project_28_02_2021.util.database.DataBaseHelper;

import java.util.ArrayList;

public class SiteManager {
    private final ArrayList<Site> site_list = new ArrayList<>();
    private final DataBaseHelper helper;
    private static SiteManager manager = null;

    public static SiteManager getInstance(Context context, ArrayList<String> exe_rows) {
        if (manager == null) { manager = new SiteManager(context, exe_rows); }
        return manager;
    }

    private SiteManager(Context context, ArrayList<String> exe_rows) {
        helper = new DataBaseHelper(context, exe_rows);
        helper.getReadableDatabase();
        site_list.addAll(helper.getSites());
    }

    public ArrayList<Site> getSites() { return site_list; }

    public void addSite(Site site) {
        site_list.add(site);
        helper.addSite(site);
    }

    public void deleteSite(Site site) {
        helper.getReadableDatabase();
        site_list.remove(site);
        helper.deleteSite(site);
    }

    public Site getSiteByName(String name, Context context) {
        for (Site site : site_list)
            if (site.getName().equals(name))
                return site;
            return new Site(name, "?", context);
    }
}