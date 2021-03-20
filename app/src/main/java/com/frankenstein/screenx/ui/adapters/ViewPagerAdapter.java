package com.frankenstein.screenx.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import com.bumptech.glide.Glide;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.R;
import com.frankenstein.screenx.models.Screenshot;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapter extends PagerAdapter {

    private Context _context;
    private ArrayList<Screenshot> _screens;
    private Logger _logger;

    public ViewPagerAdapter(Context context, ArrayList<Screenshot> screens) {
        this._context = context;
        this._screens = screens;
        this._logger = Logger.getInstance("FILES");
    }

    @Override
    public int getCount() {
        return _screens.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        _logger.log("VIEW PAGER Instantiating item", position);
        View itemView = LayoutInflater.from(_context).inflate(R.layout.screen_expanded, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
        Screenshot screen = _screens.get(position);
        File file = screen.file;
        Glide.with(_context).load(file).into(imageView);
        Objects.requireNonNull(container).addView(itemView);

        // Running OCR
//        TextHelper.getInstance(_context).getData(screen.file);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
