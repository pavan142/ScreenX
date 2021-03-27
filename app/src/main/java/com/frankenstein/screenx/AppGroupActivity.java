package com.frankenstein.screenx;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.frankenstein.screenx.helper.FileHelper;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.adapters.AppGroupPageAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.frankenstein.screenx.Constants.FILE_PROVIDER_AUTHORITY;

public class AppGroupActivity extends AppCompatActivity {

    private GridView _gridView;
    private AppGroupPageAdapter _adapter;
    private Logger _logger;
    private String _appName;
    private SwipeRefreshLayout _pullToRefresh;
    private static final int SCREEN_ACTIVITY_INTENT_CODE = 1;
    private boolean _mPaused = false;
    private Set<Screenshot> _mSelectedScreens = new HashSet<>();
    private TextView _mTextView;
    private ImageButton _mSelectAll;
    private Toolbar _mToolbar;
    private View _mSelectActions;
    private ImageButton _mShare;
    private ImageButton _mDelete;
    private ArrayList<Screenshot> _mScreens = new ArrayList<>();
    private AlertDialog.Builder _mAlertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appgrouppage);
        _logger = Logger.getInstance("AppGroupActivity");
        _gridView = findViewById(R.id.grid_view);
        _appName = getIntent().getStringExtra("APP_GROUP_NAME");
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        _pullToRefresh.setOnRefreshListener(() -> refresh());
        _mTextView = findViewById(R.id.page_title);
        _mSelectAll = findViewById(R.id.select_all);
        _mSelectAll.setOnClickListener(this::onSelectAll);
        _mToolbar = findViewById(R.id.toolbar);
        _mToolbar.setNavigationOnClickListener(this::onNavigation);
        _mTextView.setText(_appName);
        _mSelectActions = findViewById(R.id.select_actions);
        _mShare = findViewById(R.id.share);
        _mShare.setOnClickListener(this::onShare);
        _mDelete = findViewById(R.id.delete);
        _mDelete.setOnClickListener(this::onDelete);
        _mAlertBuilder = new AlertDialog.Builder(this);
        ScreenXApplication.screenFactory.screenshots.observe(this, this::postRefresh);
        updateAdapter();
    }


    private void refresh() {
        _logger.log("AppGroupActivity: Refreshing Screenshots");
        ScreenXApplication.screenFactory.refresh(this);
    }

    private void postRefresh(ArrayList<Screenshot> screens) {
        _logger.log("AppGroupActivity: Successfully refreshed data");
        _pullToRefresh.setRefreshing(false);
        updateAdapter();
    }


    private void updateAdapter() {
        AppGroup ag = ScreenXApplication.screenFactory.appgroups.get(_appName);
        if (ag == null || ag.screenshots.size() == 0) {
            finish();
            return;
        }
        boolean sameData = ag.screenshots.size() == _mScreens.size();
        if (sameData) {
            for (int i = 0; i < ag.screenshots.size(); i++) {
                if (!ag.screenshots.get(i).name.equals(_mScreens.get(i).name)) {
                    sameData = false;
                    break;
                }
            }
        }
        if (sameData)
            return;
        _mScreens = (ArrayList<Screenshot>) ag.screenshots.clone();
        _mSelectedScreens.clear();
        checkAndChangeSelectionMode();
        _logger.log("Displaying Scrrens of Appgroup", _appName, _mScreens.size());
        _adapter = new AppGroupPageAdapter(getApplicationContext(), _mScreens, _mSelectedScreens);
        _gridView.setAdapter(_adapter);
        _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = _mScreens.get(i);
                if (inSelectionMode()) {
                    toggleSelection(selected);
                    return;
                }
                _logger.log("The screen selected is", i, selected.name, "the appName: ", selected.appName);
                Intent intent = new Intent(getBaseContext(), ScreenActivity.class);
                intent.putExtra(ScreenActivity.INTENT_SCREEN_NAME, selected.name);
                intent.putExtra(ScreenActivity.INTENT_SCREEN_POSITION, i);
                intent.putExtra(ScreenActivity.INTENT_DISPLAY_TYPE, ScreenActivity.DISPLAY_APP_GROUP_ITEMS);
                startActivityForResult(intent, SCREEN_ACTIVITY_INTENT_CODE);
            }
        });
        _gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (inSelectionMode()) {
                    return true;
                }
                toggleSelection(_mScreens.get(position));
                return true;
            }
        });
    }

    private void exitSelectionMode() {
        _mTextView.setText(_appName);
        _mSelectActions.setVisibility(View.INVISIBLE);
        _mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back));
    }

    private void enterSelectionMode() {
        String itemDisplayText = _mSelectedScreens.size() > 1 ? " items": " item";
        String text;
        if (_mSelectedScreens.size() == _mScreens.size()) {
            text = "All(" + _mSelectedScreens.size() + ")";
            _mSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.ic_select_all_blue));
        } else {
            text = _mSelectedScreens.size() + "";
            _mSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.ic_select_all));
        }
        _mTextView.setText(text);
        _mSelectActions.setVisibility(View.VISIBLE);
        _mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close));
    }

    private void onSelectAll(View view) {
        if (_mSelectedScreens.size() == _mScreens.size()) {
           unMarkAll();
        } else {
            markAll();
        }
    }

    private void onShare(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");

        ArrayList<Uri> files = new ArrayList<Uri>();
        for(Screenshot screen : _mSelectedScreens) {
            _logger.log("Planning to share", screen.name);
            Uri uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY , screen.file);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void onDelete(View view) {
        ArrayList<Screenshot> toBeDeleted = new ArrayList<>();
        for (Screenshot screen: _mSelectedScreens)
            toBeDeleted.add(screen);
        String Message = "Are you sure? You want to delete " + toBeDeleted.size();
        Message +=  (toBeDeleted.size() > 1)? " images ": "image";
        Message += "from your device permanently?";
        _mAlertBuilder.setTitle("Delete")
                .setMessage(Message)
                .setPositiveButton(getResources().getString(R.string.delete_confirm), (dialog, which) -> {
                    _logger.log("User confirmed to delete");
                    ArrayList<Boolean> results = FileHelper.deleteScreenshotList(toBeDeleted);
                    ArrayList<String> deletedScreens = new ArrayList<>();
                    for (int i = 0; i < results.size(); i++) {
                        Boolean status = results.get(i);
                        Screenshot screen = toBeDeleted.get(i);
                        if (status) {
                            deletedScreens.add(screen.name);
                        _logger.log("Successfully deleted the screenshot", screen.name, screen.appName);
                        } else {
                        _logger.log("Failed to delete the screenshot", screen.name, screen.appName);
                        }
                    }
                    ScreenXApplication.screenFactory.removeScreenList(deletedScreens);
                    unMarkAll();
                    ScreenXApplication.screenFactory.refresh(this);
                })
                .setNegativeButton(getResources().getString(R.string.delete_cancel), null)
                .show();
    }

    private void markAll() {
        for (int i = 0; i < _mScreens.size(); i++) {
            Screenshot screen = _mScreens.get(i);
            if (!_mSelectedScreens.contains(screen)) {
                _mSelectedScreens.add(screen);
            }
        }
        checkAndChangeSelectionMode();
        _adapter.notifyDataSetChanged();
    }

    private void unMarkAll() {
        _mSelectedScreens.clear();
        checkAndChangeSelectionMode();
        _adapter.notifyDataSetChanged();
    }

    private void onNavigation(View view) {
        if (inSelectionMode()) {
            unMarkAll();
        } else {
            finish();
        }
    }

    private void checkAndChangeSelectionMode() {
        if (inSelectionMode())
            enterSelectionMode();
        else
            exitSelectionMode();
    }

    private boolean inSelectionMode() {
        return _mSelectedScreens.size() != 0;
    }

    private void toggleSelection(Screenshot screen) {
        if (_mSelectedScreens.contains(screen)) {
            _mSelectedScreens.remove(screen);
        } else {
            _mSelectedScreens.add(screen);
        }
        checkAndChangeSelectionMode();
        _adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        if (_mPaused)
            refresh();
        _mPaused = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        _mPaused = true;
        super.onPause();
    }
}
