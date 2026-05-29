package com.tommasov.mg4swipenovalauncher

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tommasov.mg4swipenovalauncher.databinding.ActivityPermissionOverlayBinding
import com.tommasov.mg4swipenovalauncher.databinding.ActivityPermissionAccessibilityBinding

class PermissionActivity : AppCompatActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { checkPermissions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    private fun checkPermissions() {
        when {
            !Settings.canDrawOverlays(this) -> requestOverlayPermission()
            !isAccessibilityEnabled(this, AccService::class.java) -> requestAccessibilityPermission()
            else -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun requestOverlayPermission() {
        val binding = ActivityPermissionOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonGrantOverlayPermission.setOnClickListener {
            permissionLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
    }

    private fun requestAccessibilityPermission() {
        val binding = ActivityPermissionAccessibilityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonGrantAccessibilityPermission.setOnClickListener {
            permissionLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val serviceId = "${context.packageName}/${service.name}"
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabled?.split(":")?.any { it.equals(serviceId, ignoreCase = true) } == true
    }
}
