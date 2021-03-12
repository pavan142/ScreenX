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

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Screenshot> arrayList;
    private Logger files;

    public ImageAdapter(Context context, ArrayList<Screenshot> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.files = Logger.getInstance("FILES");
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public  View getView(int position, View convertView, ViewGroup parent) {
        if (convertView ==  null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        }
        ImageView imageView;
        imageView = (ImageView) convertView.findViewById(R.id.image);
        Screenshot screen = arrayList.get(position);
        File file = screen.file;
        files.log("Loading View for", position, screen.appName,screen.name,screen.file.getAbsolutePath());
        Glide.with(context).load(file).into(imageView);

        TextView appNameView = convertView.findViewById(R.id.app_name);
        appNameView.setText(screen.appName);
        return convertView;
    }
}
