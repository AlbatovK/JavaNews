package com.example.project_28_02_2021;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class NewsRssItemAdapter extends ArrayAdapter<NewsRssItem> {

    private final LayoutInflater inflater;
    private final int layoutRes;
    private final List<NewsRssItem> items;
    private final boolean mainActivity;

    public NewsRssItemAdapter(Context context, int resource, List<NewsRssItem> items, boolean mainActivity) {
        super(context, resource, items);
        inflater = LayoutInflater.from(context);
        this.items = items;
        layoutRes = resource;
        this.mainActivity = mainActivity;
    }

    private static class ViewHolder {
        final TextView textView;
        final TextView dateView;
        final ImageView share, delete, like;
        // final ImageView image;

        public ViewHolder(View view) {
            textView = view.findViewById(R.id.item_text_view);
            dateView = view.findViewById(R.id.date_view);
            share = view.findViewById(R.id.share_button);
            delete = view.findViewById(R.id.delete_button);
            like = view.findViewById(R.id.like_button);
            // image = view.findViewById(R.id.image_icon);
        }
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parents) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutRes, parents, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NewsRssItem item = items.get(pos);
        /*
        getContext().getAssets();
        viewHolder.image.setImageResource(R.drawable.news_icon);
        try { Picasso.with(getContext()).load(item.getUrlImage())
                .error(R.drawable.news_icon)
                .into(viewHolder.image);
        } catch (Exception e) {
            viewHolder.image.setImageResource(R.drawable.news_icon); }
        */
        boolean liked = false;
        for (NewsRssItem likedItem : NewsRssItem.getLikedNews()) {
            if (likedItem.isEqual(item)) {
                liked = true;
            }
        }
        if (liked) {
            viewHolder.like.setColorFilter(R.color.black);
        } else {
            viewHolder.like.clearColorFilter();
        }
        viewHolder.textView.setText(getContext().getString(R.string.str_adapter_add_data,
                item.getTitle(), item.getSite().getName()));
        viewHolder.delete.clearColorFilter();
        viewHolder.dateView.setText(item.getRegexDate());
        viewHolder.share.setOnClickListener(v -> {
            viewHolder.share.setColorFilter(R.color.black);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, item.getLink());
            shareIntent.setType("text/plain");
            getContext().startActivity(shareIntent);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        viewHolder.share.clearColorFilter();
                    } catch (InterruptedException ignored) {
                    }
                }
            };
            thread.start();
        });
        View finalConvertView = convertView;
        viewHolder.like.setOnClickListener(v -> {
            try {
                for (NewsRssItem loopItem : NewsRssItem.getLikedNews()) {
                    if (loopItem.isEqual(item)) {
                        Snackbar.make(finalConvertView, getContext().getString(R.string.str_already_liked),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }
                NewsRssItem.getLikedNews().add(item);
                viewHolder.like.setColorFilter(R.color.black);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.newDocument();
                Element rootElem = document.createElement("items");
                Element itemElem = document.createElement("item");
                Element nameElem = document.createElement("title");
                nameElem.appendChild(document.createTextNode(item.getTitle()));
                Element siteElem = document.createElement("site");
                siteElem.appendChild(document.createTextNode(item.getSite().getName()));
                Element urlElem = document.createElement("link");
                urlElem.appendChild(document.createTextNode(item.getLink()));
                Element dateElem = document.createElement("pubDate");
                dateElem.appendChild(document.createTextNode(item.getDate().toString()));
                Element imageElement = document.createElement("image");
                imageElement.appendChild(document.createTextNode(item.getUrlImage()));
                itemElem.appendChild(nameElem);
                itemElem.appendChild(siteElem);
                itemElem.appendChild(urlElem);
                itemElem.appendChild(dateElem);
                itemElem.appendChild(imageElement);
                rootElem.appendChild(itemElem);
                document.appendChild(rootElem);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(getContext().openFileOutput("news.xml", Context.MODE_APPEND));
                transformer.transform(source, result);
                Snackbar.make(finalConvertView, getContext().getString(R.string.str_liked_done),
                        Snackbar.LENGTH_SHORT).show();
            } catch (Exception ignored) {
            }
        });
        viewHolder.delete.setOnClickListener(v -> {
            viewHolder.delete.setColorFilter(R.color.black);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog alertDialog = builder
                    .setTitle(mainActivity ? R.string.str_delete_from_all : R.string.str_delete_liked)
                    .setMessage(R.string.str_sure_delete_liked)
                    .setNegativeButton(R.string.str_add_site_neg_button,
                            (dialog, which) -> viewHolder.delete.clearColorFilter())
                    .setPositiveButton(R.string.str_delete_site, (dialog, which) -> {
                        if (mainActivity) {
                            try (OutputStream stream = getContext().openFileOutput("read.xml", Context.MODE_APPEND)) {
                                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                                Element rootElem = document.createElement("items");
                                Element itemElem = document.createElement("item");
                                Element nameElem = document.createElement("title");
                                nameElem.appendChild(document.createTextNode(item.getTitle()));
                                Element siteElem = document.createElement("site");
                                siteElem.appendChild(document.createTextNode(item.getSite().getName()));
                                Element urlElem = document.createElement("link");
                                urlElem.appendChild(document.createTextNode(item.getLink()));
                                Element dateElem = document.createElement("pubDate");
                                dateElem.appendChild(document.createTextNode(item.getDate().toString()));
                                itemElem.appendChild(nameElem);
                                itemElem.appendChild(siteElem);
                                itemElem.appendChild(urlElem);
                                itemElem.appendChild(dateElem);
                                rootElem.appendChild(itemElem);
                                document.appendChild(rootElem);
                                TransformerFactory.newInstance().newTransformer().
                                        transform(new DOMSource(document), new StreamResult(stream));
                                this.remove(item);
                                viewHolder.delete.clearColorFilter();
                            } catch (Exception ignored) {
                            }
                        } else {
                            StringBuilder gotXml = new StringBuilder();
                            try (InputStream inputStream = getContext().openFileInput("news.xml")) {
                                Scanner scanner = new Scanner(inputStream);
                                while (scanner.hasNext()) {
                                    gotXml.append(scanner.nextLine());
                                }
                            } catch (IOException ignored) {
                            }
                            getContext().deleteFile("news.xml");
                            org.jsoup.nodes.Document doc = Jsoup.parse(gotXml.toString(), "", Parser.xmlParser());
                            Elements items = doc.select("item");
                            StringBuilder resXml = new StringBuilder();
                            String root = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                            String startTag = "<items>";
                            String endTag = "</items>";
                            try {
                                for (org.jsoup.nodes.Element docItem : items) {
                                    if (docItem.select("title").text().equals(item.getTitle())) {
                                        continue;
                                    }
                                    resXml.append(docItem.toString());
                                }
                            } catch (Exception ignored) {
                            }
                            String res = root + startTag + resXml.toString() + endTag;
                            try {
                                getContext().openFileOutput("news.xml", Context.MODE_APPEND).write(res.getBytes());
                            } catch (IOException ignored) {
                            }
                            NewsRssItem.getLikedNews().remove(item);
                            this.remove(item);
                            viewHolder.delete.clearColorFilter();
                        }
                    })

                    .create();
            alertDialog.show();
        });
        return convertView;
    }
}
