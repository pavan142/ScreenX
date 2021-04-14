package com.frankenstein.screenx.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frankenstein.screenx.R;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.PermissionDetail;
import com.frankenstein.screenx.ui.adapters.PermissionPageAdapter;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermissionDetailFragment extends Fragment {
    private PermissionDetail _mDetail;
    private TextView _mTitle;
    private TextView _mDescription;
    private ImageView _mImage;
    private TextView _mPermissionButton;
    private static final Logger _mLogger = Logger.getInstance("PermissionDetailFragment");
    public static final String PERMISSION_NAME="PERMISSION_NAME";

    public PermissionDetailFragment () {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.homepage_permissions_component, container, false);
        _mTitle = view.findViewById(R.id.permission_title);
        _mDescription = view.findViewById(R.id.permission_description);
        _mImage = view.findViewById(R.id.permission_image);
        _mPermissionButton = view.findViewById(R.id.grant_permissions);
        Bundle bundle = getArguments();
        String permissionName = bundle.getString(PERMISSION_NAME);
        _mDetail = PermissionPageAdapter.detailsMap.get(permissionName);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        onPermissionsChanged(_mDetail.hasPermission.getValue());
        _mDetail.hasPermission.observe(getViewLifecycleOwner(), this::onPermissionsChanged);
        _mPermissionButton.setOnClickListener((v) -> {
            _mDetail.listener.onPermissionRequested(_mDetail.name);
        });
        _mTitle.setText(_mDetail.title);
        _mDescription.setText(_mDetail.description);
        _mImage.setImageDrawable(_mDetail.drawable);
        super.onViewCreated(view, savedInstanceState);
    }

    public void onPermissionsChanged(boolean hasPermission) {
        _mLogger.log("onPermissionsChanged", _mDetail.name, hasPermission);
        if (!hasPermission)
            _mPermissionButton.setText(getActivity().getResources().getText(R.string.grant_permissions));
        else
            _mPermissionButton.setText(getActivity().getResources().getText(R.string.permissions_granted));
    }
}
