package com.example.project_28_02_2021;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.example.project_28_02_2021.PreferenceManager.SORT_BY_DATE;

public class SettingsActivity extends AppCompatActivity {

    public static void setPreferenceState(SharedPreferences settings,
                                   RadioGroup group,
                                   String mode, HashMap<Integer, String> map,
                                   String std_mode) {
        Iterator<Integer> keyIterator = map.keySet().iterator();
        Iterator<String> valueIterator = map.values().iterator();
        while (valueIterator.hasNext() && keyIterator.hasNext()) {
            boolean hadNext = false;
            if (settings.getString(mode, std_mode).equals(valueIterator.next())) {
                group.check(keyIterator.next());
                hadNext = true;
            }
            if (!hadNext) { keyIterator.next(); }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar bar = getSupportActionBar();
        if (bar != null) { bar.setDisplayHomeAsUpEnabled(true); }
        SharedPreferences settings = getSharedPreferences(PreferenceManager.SETTINGS_NAME, MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        RadioGroup sortGroup = findViewById(R.id.sort_list);
        HashMap<Integer, String> sortMap = new HashMap<>();
        sortMap.put(R.id.sort_by_date_button, PreferenceManager.SORT_BY_DATE);
        sortMap.put(R.id.sort_by_site_button, PreferenceManager.SORT_BY_SITE);
        sortMap.put(R.id.sort_by_size_button, PreferenceManager.SORT_BY_SIZE);
        setPreferenceState(settings, sortGroup, PreferenceManager.SORT_KEY, sortMap, SORT_BY_DATE);
        RadioGroup modeGroup = findViewById(R.id.mode_list);
        HashMap<Integer, String> modeMap = new HashMap<>();
        modeMap.put(R.id.none_filter, PreferenceManager.NONE_FILTER_MODE);
        modeMap.put(R.id.filter_tag, PreferenceManager.FILTER_MODE);
        setPreferenceState(settings, modeGroup, PreferenceManager.FILTER_KEY, modeMap, PreferenceManager.NONE_FILTER_MODE);
        RadioGroup.OnCheckedChangeListener sortChangeListener =
                (group, checkedId) ->
                {
                    PreferenceManager manager = new PreferenceManager(this);
                    if (checkedId == R.id.sort_by_date_button) {
                        manager.setValueByKey(
                                new PreferenceManager.PreferencePair(PreferenceManager.SORT_KEY, PreferenceManager.SORT_BY_DATE)
                        );
                        Collections.sort(NewsRssItem.getNews(),
                                NewsRssItem.getComparator(NewsRssItem.ItemComparators.SORT_BY_DATE));
                    } else if (checkedId == R.id.sort_by_site_button) {
                        manager.setValueByKey(
                                new PreferenceManager.PreferencePair(PreferenceManager.SORT_KEY, PreferenceManager.SORT_BY_SITE)
                        );
                        Collections.sort(NewsRssItem.getNews(),
                                NewsRssItem.getComparator(NewsRssItem.ItemComparators.SORT_BY_SITE));
                    } else if (checkedId == R.id.sort_by_size_button) {
                      manager.setValueByKey(
                              new PreferenceManager.PreferencePair(PreferenceManager.SORT_KEY, PreferenceManager.SORT_BY_SIZE)
                      );
                        Collections.sort(NewsRssItem.getNews(),
                                NewsRssItem.getComparator(NewsRssItem.ItemComparators.SORT_BY_SIZE));
                    }
                    editor.apply();
                };
        RadioGroup.OnCheckedChangeListener modeChangeListener =
                (group, checkedId) ->
                {
                    if (checkedId == R.id.none_filter) { editor.putString(PreferenceManager.FILTER_KEY, PreferenceManager.NONE_FILTER_MODE); }
                    else if (checkedId == R.id.filter_tag) { editor.putString(PreferenceManager.FILTER_KEY, PreferenceManager.FILTER_MODE); }
                    editor.commit();
                };
        sortGroup.setOnCheckedChangeListener(sortChangeListener);
        modeGroup.setOnCheckedChangeListener(modeChangeListener);
        editor.commit();
        ListView sitesList = findViewById(R.id.site_settings_list);
        SiteAdapter adapter = new SiteAdapter(this, R.layout.list_site_layout, Site.getSites());
        sitesList.setAdapter(adapter);
        registerForContextMenu(sitesList);
        TextWatcher watcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] tags = s.toString().split(",");
                try {
                    deleteFile("tags.xml");
                    openFileOutput("tags.xml", MODE_APPEND);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.newDocument();
                    Element rootElem = document.createElement("tags");
                    for (String tag : tags) {
                        Element tagElem = document.createElement("tag");
                        tagElem.appendChild(document.createTextNode(tag.trim() + " "));
                        rootElem.appendChild(tagElem);
                    }
                    document.appendChild(rootElem);
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    StreamResult result = new StreamResult(openFileOutput("tags.xml", Context.MODE_APPEND));
                    transformer.transform(source, result);
                    PreferenceManager manager = new PreferenceManager(getApplicationContext());
                    manager.setValueByKey(
                            new PreferenceManager.PreferencePair(PreferenceManager.FILTER_KEY, PreferenceManager.FILTER_MODE)
                    );
                } catch (Exception ignored) {}
            }
        };
        String last_data = " ";
        try { openFileOutput("tags.xml", MODE_APPEND); } catch (Exception ignored) {}
        try (InputStream in_stream = openFileInput("tags.xml")) {
            StringBuilder tmp_str = new StringBuilder();
            Scanner in_scan = new Scanner(in_stream);
            while (in_scan.hasNext()) { tmp_str.append(in_scan.next()); }
            org.jsoup.nodes.Document doc = Jsoup.parse(tmp_str.toString(), " ", Parser.xmlParser());
            last_data = doc.select("tag").text();
        } catch (Exception ignored) {}
        EditText text = findViewById(R.id.input_tags);
        text.setText(last_data);
        text.addTextChangedListener(watcher);
    }
}