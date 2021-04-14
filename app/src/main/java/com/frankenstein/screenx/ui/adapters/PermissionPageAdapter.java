package com.frankenstein.screenx.ui.adapters;

import android.content.Context;
import android.os.Bundle;

import com.frankenstein.screenx.R;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.PermissionDetail;
import com.frankenstein.screenx.models.PermissionDetail.PermissionRequestListener;
import com.frankenstein.screenx.ui.PermissionDetailFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PermissionPageAdapter extends FragmentStateAdapter {

    private Logger _logger = Logger.getInstance("PermissionPageAdapter");
    public static final Map<String, PermissionDetail> detailsMap = new HashMap<>();
    private final ArrayList<String> _permissionOrder = new ArrayList<>();
    private PermissionRequestListener _listener;
    public static  final String STORAGE_PERMISSION = "STORAGE_PERMISSION";
    public static final String USAGE_PERMISSION = "USAGE_PERMISSION";

    public PermissionPageAdapter(FragmentActivity fa, Context context, PermissionRequestListener listener) {
        super(fa);
        this._listener = listener;
        detailsMap.put(STORAGE_PERMISSION,new PermissionDetail(
                STORAGE_PERMISSION,
                context.getResources().getString(R.string.storage_permission_title),
                context.getResources().getDrawable(R.drawable.ic_storage),
                context.getResources().getString(R.string.storage_permission_description),
                listener
        ));
        detailsMap.put(USAGE_PERMISSION,new PermissionDetail(
                USAGE_PERMISSION,
                context.getResources().getString(R.string.usage_permission_title),
                context.getResources().getDrawable(R.drawable.ic_usage),
                context.getResources().getString(R.string.usage_permission_description),
                listener
        ));
        _permissionOrder.add(STORAGE_PERMISSION);
        _permissionOrder.add(USAGE_PERMISSION);
    }

    @Override
    public int getItemCount() {
        return _permissionOrder.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        _logger.log("View Pager Instantiating item", position);
        String permissionName = _permissionOrder.get(position);
        PermissionDetailFragment fragment =  new PermissionDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PermissionDetailFragment.PERMISSION_NAME, permissionName);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onPermissionChanged(String permissionName, boolean hasPermission) {
        _logger.log("onPermissionChanged", permissionName, hasPermission);
        PermissionDetail detail =  detailsMap.get(permissionName);
        detail.hasPermission.setValue(hasPermission);
    }
}
