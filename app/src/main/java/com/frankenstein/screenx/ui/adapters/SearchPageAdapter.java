package com.frankenstein.screenx.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import com.bumptech.glide.Glide;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.R;
import com.frankenstein.screenx.models.Screenshot;

public class SearchPageAdapter extends BaseAdapter {
    private Context _context;
    private ArrayList<Screenshot> _screens;
    private Logger _logger;

    public SearchPageAdapter(Context context, ArrayList<Screenshot> arrayList) {
        this._context = context;
        this._screens = arrayList;
        this._logger = Logger.getInstance("SearchPageAdapter");
    }
    @Override
    public int getCount() {
        return _screens.size();
    }
    @Override
    public Object getItem(int position) {
        return _screens.get(position);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public  View getView(int position, View convertView, ViewGroup parent) {
        if (convertView ==  null) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.searchpage_grid_item, parent, false);
        }
        ImageView imageView;
        imageView = (ImageView) convertView.findViewById(R.id.image);
        Screenshot screen = _screens.get(position);
        File file = screen.file;
        Glide.with(_context).load(file).thumbnail(0.1f).into(imageView);
        return convertView;
    }
}
