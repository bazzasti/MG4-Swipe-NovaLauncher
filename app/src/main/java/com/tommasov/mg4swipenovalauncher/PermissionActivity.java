package com.tommasov.mg4swipenovalauncher;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionActivity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> checkPermissions());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
    }

    private void checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else if (!isAccessibilityServiceEnabled(this, AccService.class)) {
            requestAccessibilityPermission();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void requestOverlayPermission() {
        setContentView(R.layout.activity_permission_overlay);
        Button grantOverlayPermissionButton = findViewById(R.id.buttonGrantOverlayPermission);
        grantOverlayPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            permissionLauncher.launch(intent);
        });
    }

    private void requestAccessibilityPermission() {
        setContentView(R.layout.activity_permission_accessibility);
        Button grantAccessibilityPermissionButton = findViewById(R.id.buttonGrantAccessibilityPermission);
        grantAccessibilityPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            permissionLauncher.launch(intent);
        });
    }

    private boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        String serviceId = context.getPackageName() + "/" + service.getName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices != null) {
            for (String enabledService : enabledServices.split(":")) {
                if (enabledService.equalsIgnoreCase(serviceId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
