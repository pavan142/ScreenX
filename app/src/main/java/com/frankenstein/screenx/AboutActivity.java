package com.frankenstein.screenx;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    private ArrayList<String> _mLabels = new ArrayList<>();
    private Toolbar _mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutpage);
        _mToolbar = (Toolbar) findViewById(R.id.toolbar);
        _mToolbar.setNavigationOnClickListener((View view) -> {
            finish();
        });
    }
}
