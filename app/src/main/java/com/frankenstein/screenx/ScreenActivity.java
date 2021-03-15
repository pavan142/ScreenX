package com.frankenstein.screenx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import static com.frankenstein.screenx.Constants.TOOLBAR_TRANSITION;

public class ScreenActivity extends ImmersiveActivity {

    private Logger _logger;
    private ViewPager _viewpager;
    private ViewPagerAdapter _adapter;
    private ScreenFactory _sf;
    private ArrayList<Screenshot> _screens;
    private ImageButton _deleteButton;
    private ImageButton _shareButton;
    private LinearLayout _toolbar;

    public Resources resources;
    public Utils utils;
    public AlertDialog.Builder mAlertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_slider);

        resources = getApplicationContext().getResources();
        mAlertBuilder = new AlertDialog.Builder(this);

        utils = Utils.getInstance();
        _logger = Logger.getInstance("FILES");
        _viewpager = findViewById(R.id.view_pager);
        _sf = ScreenFactory.getInstance();

        String screenName = getIntent().getStringExtra("SCREEN_NAME");
        int screenPosition = getIntent().getIntExtra("SCREEN_POSITION", 0);
        Screenshot screen = _sf.findScreenByName(screenName);

        AppGroup ag =_sf.appgroups.get(screen.appName);
        if (ag == null) {
            finish();
        }

        _toolbar = findViewById(R.id.toolbar);

        _deleteButton = findViewById(R.id.delete);
        _deleteButton.setOnClickListener(view -> onDelete());

        _shareButton = findViewById(R.id.share);
        _shareButton.setOnClickListener(view -> onShare());

        _toolbar.setAlpha(0);
        alignToolbarWithNavbar();
        _screens = ag .screenshots;
        _adapter = new ViewPagerAdapter(getApplicationContext(), _screens);
        _viewpager.setAdapter(_adapter);
        _viewpager.setCurrentItem(screenPosition);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        this.setupTapHandling(_viewpager);
    }

    private void onDelete() {
        int position = _viewpager.getCurrentItem();
        Screenshot screen = _screens.get(position);
        _logger.log("ASKED TO DELETE", screen.name, screen.appName);
        mAlertBuilder.setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this Image?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        _logger.log("CONFIRMED TO DELETE", screen.name, screen.appName);
                        if (FileHelper.deleteScreenshot(screen)) {
                            _logger.log("SUCCESSFULLY DELETED SCREEN", screen.name, screen.appName);
                            _screens.remove(position);
                            _adapter.notifyDataSetChanged();
                            _sf.removeScreen(screen.name);
                        } else {
                            _logger.log("FAILED TO DELETE SCREEN", screen.name, screen.appName);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void onShare() {
        int position = _viewpager.getCurrentItem();
        Screenshot screen = _screens.get(position);
        _logger.log("ASKED TO SHARE", screen.name, screen.appName);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(
                Intent.EXTRA_STREAM,
                Uri.fromFile(screen.file)
        );
        sendIntent.setType("image/*");
        startActivity(Intent.createChooser(sendIntent, "Share Image"));
    }

    private void alignToolbarWithNavbar() {
        int height = (int)getResources().getDimension(R.dimen.bottom_toolbar_height);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                height
        );
        int navbarHeight = utils.getNavbarHeight();
        _logger.log("Method2: Navigation Bar Height is", navbarHeight, "and the height is", height);
        params.bottomMargin= navbarHeight;
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        _toolbar.setLayoutParams(params);
    }

    @Override
    public void hideSystemUI() {
        super.hideSystemUI();
        if (_toolbar == null)
            return;
        _logger.log("SETTING VISIBILITY OF TOOLBAR TO INVISIBLE");
        _toolbar.animate().alpha(0).setDuration(TOOLBAR_TRANSITION);
    }

    @Override
    public void showSystemUI() {
        super.showSystemUI();
        _logger.log("CALLING SHOW SYSTEM UI");
        if (_toolbar == null)
            return;
        _logger.log("SETTING VISIBILITY OF TOOLBAR TO VISIBLE");
        alignToolbarWithNavbar();
        _toolbar.animate().alpha(1).setDuration(TOOLBAR_TRANSITION);
    }
}
