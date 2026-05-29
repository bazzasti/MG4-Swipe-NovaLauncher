package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends ArrayAdapter<ApplicationInfo> {
    private final PackageManager packageManager;
    private String selectedPackage;
    private List<ApplicationInfo> allApps;
    private boolean showSystemApps = false;
    private java.util.Set<String> launcherPackages;

    public AppListAdapter(@NonNull Context context, List<ApplicationInfo> apps, String selectedPackage) {
        super(context, 0, apps);
        this.packageManager = context.getPackageManager();
        this.selectedPackage = selectedPackage;
        allApps = new ArrayList<>(apps);
        launcherPackages = cacheLauncherPackages();
        filterApps();
    }

    public void setSelectedPackage(String packageName) {
        this.selectedPackage = packageName;
    }

    public void toggleSystemAppsVisibility() {
        showSystemApps = !showSystemApps;
        filterApps();
    }

    public boolean isSystemAppsVisible() {
        return showSystemApps;
    }

    private void filterApps() {
        List<ApplicationInfo> filteredApps = new ArrayList<>();

        for (ApplicationInfo appInfo : allApps) {
            if (isLauncherApp(appInfo)) {
                filteredApps.add(appInfo);
            }
        }

        for (ApplicationInfo appInfo : allApps) {
            if (!isLauncherApp(appInfo) && (showSystemApps || !isSystemApp(appInfo))) {
                filteredApps.add(appInfo);
            }
        }

        clear();
        addAll(filteredApps);
        notifyDataSetChanged();
    }

    private java.util.Set<String> cacheLauncherPackages() {
        java.util.Set<String> result = new java.util.HashSet<>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        for (ResolveInfo ri : packageManager.queryIntentActivities(intent, 0)) {
            result.add(ri.activityInfo.packageName);
        }
        return result;
    }

    private boolean isLauncherApp(ApplicationInfo appInfo) {
        return launcherPackages.contains(appInfo.packageName);
    }

    private boolean isSystemApp(ApplicationInfo appInfo) {
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_app, parent, false);
        }

        ApplicationInfo appInfo = getItem(position);
        if (appInfo == null) return convertView;

        ImageView iconView = convertView.findViewById(R.id.app_icon);
        TextView nameView = convertView.findViewById(R.id.app_name);
        ImageView checkmarkView = convertView.findViewById(R.id.app_checkmark);

        try {
            iconView.setImageDrawable(appInfo.loadIcon(packageManager));
            nameView.setText(appInfo.loadLabel(packageManager));
        } catch (Exception e) {
            nameView.setText(appInfo.packageName);
        }

        checkmarkView.setVisibility(appInfo.packageName.equals(selectedPackage) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }
}
