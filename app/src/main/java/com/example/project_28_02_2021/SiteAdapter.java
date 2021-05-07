package com.example.project_28_02_2021;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SiteAdapter extends ArrayAdapter<Site> {

    private final LayoutInflater inflater;
    private final int layoutRes;
    private final List<Site> items;

    public SiteAdapter(Context context, int resource, List<Site> items) {
        super(context, resource, items);
        inflater = LayoutInflater.from(context);
        layoutRes = resource;
        this.items = items;
    }

    private static class ViewHolder {
        final TextView infoView;
        final TextView quantity_view;
        final ImageView site_icon;

        public ViewHolder(View view) {
            infoView = view.findViewById(R.id.site_data_view);
            quantity_view = view.findViewById(R.id.quant_data);
            site_icon = view.findViewById(R.id.icon);
        }
    }

    private void picIntoViewByLink(String link, ImageView view) {
        final String f_str = "Done";
        if (f_str.equalsIgnoreCase((String) view.getContentDescription())) {
        Context context = getContext();
        context.getAssets();
        view.setImageResource(R.drawable.rss_icon);
        try { Picasso.with(context).load(link).error(R.drawable.rss_icon).into(view); }
        catch (Exception e) { view.setImageResource(R.drawable.rss_icon); }
        view.setContentDescription(f_str);
        }
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parents) {
        SiteAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutRes, parents, false);
            viewHolder = new SiteAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else { viewHolder = (SiteAdapter.ViewHolder) convertView.getTag(); }
        Site item = items.get(pos);
        picIntoViewByLink(item.getImageLink(), viewHolder.site_icon);
        viewHolder.infoView.setText(item.asString());
        viewHolder.quantity_view.setText(
                getContext().getString(R.string.str_items_count,
                item.getItemsCount(),
                getContext().getResources().getQuantityString(R.plurals.items_plurals, item.getItemsCount()))
        );
        convertView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog dialog = builder
                    .setTitle(R.string.str_delete_full_site)
                    .setMessage(getContext().getString(R.string.str_sure, item.getName()))
                    .setPositiveButton(R.string.str_delete_site,
                            (dialog1, which) -> {
                                Site.getSites().remove(item);
                                this.remove(item);
                                DataBaseHelper helper = new DataBaseHelper(getContext(), new ArrayList<>());
                                helper.getReadableDatabase();
                                helper.deleteSite(item);
                            }
                    )
                    .setNegativeButton(R.string.str_add_site_neg_button, (dialog12, which) -> {} )
                    .create();
            dialog.show();
            return true;
        });
        return convertView;
    }
}
