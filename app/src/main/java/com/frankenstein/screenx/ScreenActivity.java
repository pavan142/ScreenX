package com.frankenstein.screenx;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.frankenstein.screenx.helper.FileHelper;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.ImmersiveActivity;
import com.frankenstein.screenx.ui.adapters.ScreenPageAdapter;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import static com.frankenstein.screenx.Constants.TOOLBAR_TRANSITION;

public class ScreenActivity extends ImmersiveActivity {

    public static final String DISPLAY_APP_GROUP_ITEMS = "DISPLAY_APP_GROUP_ITEMS";
    public static final String DISPLAY_SEARCH_RESULTS = "DISPLAY_SEARCH_RESULTS";
    public static final String INTENT_DISPLAY_TYPE = "INTENT_DISPLAY_TYPE";
    public static final String INTENT_SEARCH_MATCHES="INTENT_SEARCH_MATCHES";
    public static final String INTENT_SCREEN_POSITION="INTEN_SCREEN_POSITION";
    public static final String INTENT_SCREEN_NAME="INTENT_SCREEN_NAME";

    private Logger _logger;
    private ViewPager _viewpager;
    private ScreenPageAdapter _adapter;
    private ArrayList<Screenshot> _screens;
    private ImageButton _deleteButton;
    private ImageButton _shareButton;
    private LinearLayout _toolbar;
    private String _screenName;
    private int _currPosition;
    private ArrayList<String> _searchMatches;

    public Resources resources;
    public Utils utils;
    public AlertDialog.Builder mAlertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screenpage);

        resources = getApplicationContext().getResources();
        mAlertBuilder = new AlertDialog.Builder(this);

        utils = Utils.getInstance();
        _logger = Logger.getInstance("ScreenActivity");
        _viewpager = findViewById(R.id.view_pager);
        _toolbar = findViewById(R.id.toolbar);
        _deleteButton = findViewById(R.id.delete);
        _deleteButton.setOnClickListener(view -> onDelete());
        _shareButton = findViewById(R.id.share);
        _shareButton.setOnClickListener(view -> onShare());
        _toolbar.setAlpha(0);
        alignToolbarWithNavbar();

        _screenName = getIntent().getStringExtra(INTENT_SCREEN_NAME);
        _currPosition = getIntent().getIntExtra(INTENT_SCREEN_POSITION, 0);
        _searchMatches = getIntent().getStringArrayListExtra(INTENT_SEARCH_MATCHES);
        String displayType = getIntent().getStringExtra(INTENT_DISPLAY_TYPE);
        switch (displayType) {
            case DISPLAY_APP_GROUP_ITEMS:
                displayAppGroupItems();
                break;
            case DISPLAY_SEARCH_RESULTS:
                displaySearchItems();
                break;
            default:
                displayAppGroupItems();
                break;
        }

        updatePager(_currPosition);
        // TODO: Find a better fix to this
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void displayAppGroupItems() {
        Screenshot screen = ScreenXApplication.screenFactory.findScreenByName(_screenName);
        AppGroup ag = ScreenXApplication.screenFactory.appgroups.get(screen.appName);
        if (ag == null) {
            setResult(RESULT_OK);
            finish();
        }
        _screens = ag.screenshots;
    }

    private void displaySearchItems() {
        if (_searchMatches == null) {
            setResult(RESULT_OK);
            finish();
        }
        _screens = new ArrayList<>();
        for (int i = 0; i < _searchMatches.size(); i++) {
            String name = _searchMatches.get(i);
            _screens.add(ScreenXApplication.screenFactory.findScreenByName(name));
        }
    }

    private void updatePager(int position) {
        if (_screens.size() == 0) {
            setResult(RESULT_OK);
            finish();
        }
        _adapter = new ScreenPageAdapter(getApplicationContext(), _screens, this);
        _viewpager.setAdapter(_adapter);
        _viewpager.setCurrentItem(position);
    }

    private void onDelete() {
        int position = _viewpager.getCurrentItem();
        Screenshot screen = _screens.get(position);
        _logger.log("ASKED TO DELETE", screen.name, screen.appName);
        mAlertBuilder.setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this Image?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    _logger.log("CONFIRMED TO DELETE", screen.name, screen.appName);
                    if (FileHelper.deleteScreenshot(screen)) {
                        _logger.log("SUCCESSFULLY DELETED SCREEN", screen.name, screen.appName);
                        _screens.remove(position);
                        ScreenXApplication.screenFactory.removeScreen(screen.name);
                        updatePager(position);
                    } else {
                        _logger.log("FAILED TO DELETE SCREEN", screen.name, screen.appName);
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
