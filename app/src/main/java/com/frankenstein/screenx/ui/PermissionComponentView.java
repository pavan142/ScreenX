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

public class PermissionComponentView extends LinearLayout {

    private TextView _mTextView;
    private LottieAnimationView _mSuccessAnimation;
    private NeumorphCardView _mCardView;
    private View _mLeftBorder;

    public PermissionComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PermissionComponentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.homepage_permissions_component, this);

        _mTextView = findViewById(R.id.text_view);
        _mSuccessAnimation = findViewById(R.id.success_animation);
        _mCardView = findViewById(R.id.card_view);
        _mLeftBorder = findViewById(R.id.left_border);

        // TODO: find a better design, I am not quite satisified with it yet, so removing both border
        // and success and animation
        _mLeftBorder.setVisibility(View.GONE);
        _mSuccessAnimation.setVisibility(View.GONE);
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.PermissionComponentView);
        _mTextView.setText(styledAttrs.getString(R.styleable.PermissionComponentView_permission_text));
    }

    public void onPermissionChanged(Boolean hasPermission) {
        int bgColor = hasPermission? getResources().getColor(R.color.permission_granted_shadow_dark):
                getResources().getColor(R.color.lightWhite);
        _mCardView.setBackgroundColor(bgColor);
//        _mSuccessAnimation.setVisibility(hasPermission? View.VISIBLE: View.INVISIBLE);
//        _mLeftBorder.setVisibility(hasPermission? View.VISIBLE: View.GONE);
    }
}
