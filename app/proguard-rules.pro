# Keep accessibility service (referenced by name in AndroidManifest)
-keep class com.tommasov.mg4swipenovalauncher.AccService { *; }

# Keep SwipeService (foreground service started by class name)
-keep class com.tommasov.mg4swipenovalauncher.SwipeService { *; }

# Keep BootReceiver (referenced in manifest)
-keep class com.tommasov.mg4swipenovalauncher.BootReceiver { *; }
