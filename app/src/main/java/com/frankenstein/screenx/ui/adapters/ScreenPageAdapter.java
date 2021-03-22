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
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.R;
import com.frankenstein.screenx.helper.TextHelper;
import com.frankenstein.screenx.interfaces.ScreenTapListener;
import com.frankenstein.screenx.models.Screenshot;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ScreenPageAdapter extends PagerAdapter {

    private Context _context;
    private ArrayList<Screenshot> _screens;
    private Logger _logger;
    private ScreenTapListener _tapListener;

    public ScreenPageAdapter(Context context, ArrayList<Screenshot> screens, ScreenTapListener tapListener) {
        this._context = context;
        this._screens = screens;
        this._logger = Logger.getInstance("FILES");
        this._tapListener = tapListener;
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
        View itemView = LayoutInflater.from(_context).inflate(R.layout.screenpage_item, container, false);
        Screenshot screen = _screens.get(position);
        File file = screen.file;

//        ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
//        Glide.with(_context).load(file).into(imageView);

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) itemView.findViewById(R.id.image);
        imageView.setImage(ImageSource.uri(file.toString()));
        imageView.setOnClickListener(v -> _tapListener.onTap());

        Objects.requireNonNull(container).addView(itemView);

        // Running OCR
        TextHelper.getInstance(_context).getData(file, (text) -> this.onTextFetched(file.getName(), text));
        return itemView;
    }

    public void onTextFetched(String filename, String text) {
        _logger.log("got text from TextHelper for file", filename);
        _logger.log(text);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
