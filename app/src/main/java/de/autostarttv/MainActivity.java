package de.autostarttv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private Settings settings;

    private CompoundButton enabledSwitch;
    private Button appButton;
    private Button delayButton;
    private View permissionGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);

        enabledSwitch = findViewById(R.id.switch_enabled);
        appButton = findViewById(R.id.button_app);
        delayButton = findViewById(R.id.button_delay);
        permissionGroup = findViewById(R.id.group_permission);
        Button testButton = findViewById(R.id.button_test);
        Button permissionButton = findViewById(R.id.button_permission);

        enabledSwitch.setChecked(settings.isEnabled());
        enabledSwitch.setOnCheckedChangeListener((button, checked) -> settings.setEnabled(checked));

        appButton.setOnClickListener(v -> chooseApp());
        delayButton.setOnClickListener(v -> chooseDelay());
        testButton.setOnClickListener(v -> testLaunch());
        permissionButton.setOnClickListener(v -> openPermissionSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private void updateViews() {
        String label = AppLauncher.getLabel(this, settings.getPackage());
        appButton.setText(getString(R.string.app_selected,
                label != null ? label : getString(R.string.app_none)));
        delayButton.setText(getString(R.string.delay_value, settings.getDelaySeconds()));
        permissionGroup.setVisibility(needsPermission() ? View.VISIBLE : View.GONE);
    }

    private void chooseApp() {
        List<AppLauncher.App> apps = AppLauncher.listApps(this);
        if (apps.isEmpty()) {
            Toast.makeText(this, R.string.no_apps, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[apps.size()];
        int selected = -1;
        for (int i = 0; i < apps.size(); i++) {
            labels[i] = apps.get(i).label;
            if (apps.get(i).packageName.equals(settings.getPackage())) {
                selected = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_app)
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    settings.setPackage(apps.get(which).packageName);
                    if (!settings.isEnabled()) {
                        settings.setEnabled(true);
                        enabledSwitch.setChecked(true);
                    }
                    dialog.dismiss();
                    updateViews();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void chooseDelay() {
        int[] delays = Settings.DELAYS;
        String[] labels = new String[delays.length];
        int selected = -1;
        for (int i = 0; i < delays.length; i++) {
            labels[i] = getString(R.string.seconds, delays[i]);
            if (delays[i] == settings.getDelaySeconds()) {
                selected = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delay)
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    settings.setDelaySeconds(delays[which]);
                    dialog.dismiss();
                    updateViews();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void testLaunch() {
        if (settings.getPackage() == null) {
            Toast.makeText(this, R.string.choose_app_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AppLauncher.launch(this, settings.getPackage())) {
            Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean needsPermission() {
        return Build.VERSION.SDK_INT >= 23 && !android.provider.Settings.canDrawOverlays(this);
    }

    private void openPermissionSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_manual)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }
}
