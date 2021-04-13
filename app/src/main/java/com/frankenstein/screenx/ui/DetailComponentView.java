package com.frankenstein.screenx.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.frankenstein.screenx.R;

import androidx.cardview.widget.CardView;
import soup.neumorphism.NeumorphCardView;

public class DetailComponentView extends LinearLayout {

    private TextView _mLabel;
    private TextView _mData;

    public DetailComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DetailComponentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.detailpage_detail_component, this);

        _mLabel = findViewById(R.id.label);
        _mData = findViewById(R.id.data);
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.DetailComponentView);
        _mLabel.setText(styledAttrs.getString(R.styleable.DetailComponentView_label));
        _mData.setText(styledAttrs.getString(R.styleable.DetailComponentView_data));
    }

    public void setData(String data) {
        _mData.setText(data);
    }
}
