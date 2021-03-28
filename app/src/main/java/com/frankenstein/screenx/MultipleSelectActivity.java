package com.frankenstein.screenx;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.frankenstein.screenx.helper.FileHelper;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.adapters.AppGroupPageAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import static com.frankenstein.screenx.Constants.FILE_PROVIDER_AUTHORITY;

public class MultipleSelectActivity extends AppCompatActivity {
    private ImageButton _mSelectAll;
    private Toolbar _mDefaultToolbar;
    private Toolbar _mSelectModeToolbar;
    private ImageButton _mShare;
    private ImageButton _mDelete;
    private TextView _mSelectTitleView;
    private AlertDialog.Builder _mAlertBuilder;
    private Logger _mLogger = Logger.getInstance("MultipleSelectActivity");

    protected ArrayList<Screenshot> mScreens = new ArrayList<>();
    protected Set<Screenshot> mSelectedScreens = new HashSet<>();
    protected BaseAdapter mAdapter;
    protected GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _mSelectAll = findViewById(R.id.select_all);
        _mSelectAll.setOnClickListener(this::onSelectAll);
        _mDefaultToolbar = findViewById(R.id.default_toolbar);
        _mDefaultToolbar.setNavigationOnClickListener(this::defaultModeNavigation);
        _mSelectModeToolbar = findViewById(R.id.select_toolbar);
        _mSelectModeToolbar.setNavigationOnClickListener(this::selectModeNavigation);
        _mShare = findViewById(R.id.share);
        _mShare.setOnClickListener(this::onShare);
        _mDelete = findViewById(R.id.delete);
        _mDelete.setOnClickListener(this::onDelete);
        _mSelectTitleView = findViewById(R.id.multipleselect_title);
        _mAlertBuilder = new AlertDialog.Builder(this);
        mGridView = findViewById(R.id.grid_view);
    }

    private void exitSelectionMode() {
        _mSelectModeToolbar.setVisibility(View.INVISIBLE);
        _mDefaultToolbar.setVisibility(View.VISIBLE);
    }

    private void enterSelectionMode() {
        String text;
        if (mSelectedScreens.size() == mScreens.size()) {
            text = "All(" + mSelectedScreens.size() + ")";
            _mSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.ic_select_all_blue));
        } else {
            text = mSelectedScreens.size() + "";
            _mSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.ic_select_all));
        }
        _mSelectTitleView.setText(text);
        _mSelectModeToolbar.setVisibility(View.VISIBLE);
        _mDefaultToolbar.setVisibility(View.INVISIBLE);
    }

    private void onSelectAll(View view) {
        if (mSelectedScreens.size() == mScreens.size()) {
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
        for(Screenshot screen : mSelectedScreens) {
            _mLogger.log("Planning to share", screen.name);
            Uri uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY , screen.file);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void onDelete(View view) {
        ArrayList<Screenshot> toBeDeleted = new ArrayList<>();
        for (Screenshot screen: mSelectedScreens)
            toBeDeleted.add(screen);
        String Message = "Are you sure? You want to delete " + toBeDeleted.size();
        Message +=  (toBeDeleted.size() > 1)? " images ": "image";
        Message += "from your device permanently?";
        _mAlertBuilder.setTitle("Delete")
                .setMessage(Message)
                .setPositiveButton(getResources().getString(R.string.delete_confirm), (dialog, which) -> {
                    _mLogger.log("User confirmed to delete");
                    ArrayList<Boolean> results = FileHelper.deleteScreenshotList(toBeDeleted);
                    ArrayList<String> deletedScreens = new ArrayList<>();
                    for (int i = 0; i < results.size(); i++) {
                        Boolean status = results.get(i);
                        Screenshot screen = toBeDeleted.get(i);
                        if (status) {
                            deletedScreens.add(screen.name);
                            _mLogger.log("Successfully deleted the screenshot", screen.name, screen.appName);
                        } else {
                            _mLogger.log("Failed to delete the screenshot", screen.name, screen.appName);
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
        for (int i = 0; i < mScreens.size(); i++) {
            Screenshot screen = mScreens.get(i);
            if (!mSelectedScreens.contains(screen)) {
                mSelectedScreens.add(screen);
            }
        }
        checkAndChangeSelectionMode();
        mAdapter.notifyDataSetChanged();
    }

    private void unMarkAll() {
        mSelectedScreens.clear();
        checkAndChangeSelectionMode();
        mAdapter.notifyDataSetChanged();
    }

    private void selectModeNavigation(View view) {
        unMarkAll();
    }

    private void defaultModeNavigation(View view) {
        finish();
    }

    protected void checkAndChangeSelectionMode() {
        if (inSelectionMode())
            enterSelectionMode();
        else
            exitSelectionMode();
    }

    protected boolean inSelectionMode() {
        return mSelectedScreens.size() != 0;
    }

    protected void toggleSelection(Screenshot screen) {
        if (mSelectedScreens.contains(screen)) {
            mSelectedScreens.remove(screen);
        } else {
            mSelectedScreens.add(screen);
        }
        checkAndChangeSelectionMode();
        mAdapter.notifyDataSetChanged();
    }

    protected void updateAdapter() {
        mSelectedScreens.clear();
        checkAndChangeSelectionMode();
        mAdapter = new AppGroupPageAdapter(getApplicationContext(), mScreens, mSelectedScreens);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = mScreens.get(i);

                if (inSelectionMode()) {
                   toggleSelection(selected);
                   return;
                }

                _mLogger.log("The screen selected is", i, selected.name, "the appName: ", selected.appName);
                Intent intent = getTapIntent(selected, i);
                if (intent != null)
                    startActivity(intent);
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (inSelectionMode()) {
                    return true;
                }
                toggleSelection(mScreens.get(position));
                return true;
            }
        });
    }

    public Intent getTapIntent(Screenshot selected, int position) {
        return null;
    }

    @Override
    public void onBackPressed() {
        if (inSelectionMode()) {
            unMarkAll();
            return;
        }
        super.onBackPressed();
    }
}
