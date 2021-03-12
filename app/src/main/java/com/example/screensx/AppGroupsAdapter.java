package com.example.screensx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import com.bumptech.glide.Glide;


public class AppGroupsAdapter extends BaseAdapter {
    private Context _context;
    private ArrayList<Screenshot> _screens;
    private Logger _logger;

    public AppGroupsAdapter(Context context, ArrayList<Screenshot> arrayList) {
        this._context = context;
        this._screens = arrayList;
        this._logger = Logger.getInstance("FILES");
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
            convertView = LayoutInflater.from(_context).inflate(R.layout.appgroup_preview, parent, false);
        }
        ImageView imageView;
        imageView = (ImageView) convertView.findViewById(R.id.image);
        Screenshot screen = _screens.get(position);
        _logger.log("AppGroupsAdapter, view for", position, _screens.size());
        File file = screen.file;
        _logger.log("Loading View for", position, screen.appName,screen.name,screen.file.getAbsolutePath());
        Glide.with(_context).load(file).thumbnail(0.1f).into(imageView);

        TextView appNameView = convertView.findViewById(R.id.app_name);
        appNameView.setText(screen.appName);
        return convertView;
    }
}
