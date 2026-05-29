package com.tommasov.mg4swipenovalauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.tommasov.mg4swipenovalauncher.databinding.ListItemAppBinding

class AppListAdapter(
    context: Context,
    private val allApps: List<ApplicationInfo>,
    private var selectedPackage: String?
) : ArrayAdapter<ApplicationInfo>(context, 0, allApps.toMutableList()) {

    private val pm: PackageManager = context.packageManager
    private var showSystemApps = false
    private val launcherPackages: Set<String> = cacheLauncherPackages()

    init { filterApps() }

    fun setSelectedPackage(pkg: String) { selectedPackage = pkg }
    fun toggleSystemAppsVisibility() { showSystemApps = !showSystemApps; filterApps() }
    fun isSystemAppsVisible(): Boolean = showSystemApps

    private fun cacheLauncherPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(intent, 0).map { it.activityInfo.packageName }.toSet()
    }

    private fun filterApps() {
        val launchers = allApps.filter { it.packageName in launcherPackages }
        val others = allApps.filter { it.packageName !in launcherPackages && (showSystemApps || !isSystemApp(it)) }
        clear()
        addAll(launchers + others)
        notifyDataSetChanged()
    }

    private fun isSystemApp(app: ApplicationInfo) = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ListItemAppBinding.inflate(LayoutInflater.from(context), parent, false).also {
                it.root.tag = it
            }
        } else {
            convertView.tag as ListItemAppBinding
        }

        val appInfo = getItem(position) ?: return binding.root

        try {
            binding.appIcon.setImageDrawable(appInfo.loadIcon(pm))
            binding.appName.text = appInfo.loadLabel(pm)
        } catch (_: Exception) {
            binding.appName.text = appInfo.packageName
        }

        binding.appCheckmark.visibility =
            if (appInfo.packageName == selectedPackage) View.VISIBLE else View.INVISIBLE

        return binding.root
    }
}
