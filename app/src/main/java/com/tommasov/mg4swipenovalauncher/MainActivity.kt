package com.tommasov.mg4swipenovalauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tommasov.mg4swipenovalauncher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager.getInstance(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.explanationText.setText(R.string.explanation_text)

        val pm = packageManager
        val userApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        var selectedPackage = prefs.selectedPackage
        if (selectedPackage == null) {
            userApps.find { it.packageName == SwipeService.DEFAULT_LAUNCHER }?.let {
                selectedPackage = it.packageName
                prefs.selectedPackage = selectedPackage
            }
        }

        adapter = AppListAdapter(this, userApps, selectedPackage)
        binding.appList.adapter = adapter

        binding.appList.setOnItemClickListener { _, _, position, _ ->
            val app = userApps[position]
            prefs.selectedPackage = app.packageName
            adapter.setSelectedPackage(app.packageName)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, String.format(getString(R.string.toast_selected), app.loadLabel(pm)), Toast.LENGTH_SHORT).show()
        }

        binding.toggleSystemAppsButton.setOnClickListener {
            adapter.toggleSystemAppsVisibility()
            binding.toggleSystemAppsButton.text = getString(
                if (adapter.isSystemAppsVisible()) R.string.hide_system_apps else R.string.show_system_apps
            )
        }

        binding.switchBackButton.isChecked = prefs.isBackButtonHidden
        binding.switchBackButton.setOnClickListener {
            val hide = !prefs.isBackButtonHidden
            prefs.backButtonVisibility = if (hide) "INVISIBLE" else "VISIBLE"
            binding.switchBackButton.isChecked = hide
            stopSwipeService()
            startSwipeService()
        }

        binding.uninstallButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.uninstall))
                .setMessage(getString(R.string.uninstall_confirm))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    stopSwipeService()
                    val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
                    startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        startSwipeService()
    }

    private fun stopSwipeService() {
        try { stopService(Intent(this, SwipeService::class.java)) }
        catch (e: Exception) { AppLogger.e("Failed to stop SwipeService", e) }
    }

    private fun startSwipeService() {
        try { startForegroundService(Intent(this, SwipeService::class.java)) }
        catch (e: Exception) { AppLogger.e("Failed to start SwipeService", e) }
    }
}
