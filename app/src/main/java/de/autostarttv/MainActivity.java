package de.autostarttv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private Settings settings;

    private TextView enabledValue;
    private TextView appValue;
    private TextView delayValue;
    private View permissionRow;
    private boolean appDialogPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);

        // Load the app list in the background right away, so the picker opens instantly.
        AppLauncher.preload(this);

        enabledValue = setupRow(R.id.row_enabled, "01", R.string.autostart, v -> toggleEnabled());
        appValue = setupRow(R.id.row_app, "02", R.string.app, v -> chooseApp());
        delayValue = setupRow(R.id.row_delay, "03", R.string.delay, v -> chooseDelay());
        TextView testValue = setupRow(R.id.row_test, "04", R.string.test, v -> testLaunch());
        testValue.setText(R.string.test_value);
        TextView permissionValue = setupRow(R.id.row_permission, "!", R.string.permission,
                v -> openPermissionSettings());
        permissionValue.setText(R.string.permission_value);
        permissionRow = findViewById(R.id.row_permission);
        setAccent(permissionValue, true);
        setAccent((TextView) permissionRow.findViewById(R.id.row_number), true);

        findViewById(R.id.text_imprint).setOnClickListener(v -> openImprint());
        ((TextView) findViewById(R.id.text_version))
                .setText(getString(R.string.version, versionName()));

        if (savedInstanceState == null) {
            findViewById(R.id.row_enabled).requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private TextView setupRow(int rowId, String number, int labelRes, View.OnClickListener click) {
        View row = findViewById(rowId);
        ((TextView) row.findViewById(R.id.row_number)).setText(number);
        ((TextView) row.findViewById(R.id.row_label)).setText(labelRes);
        row.setOnClickListener(click);
        return row.findViewById(R.id.row_value);
    }

    @SuppressWarnings("deprecation")
    private void setAccent(TextView view, boolean accent) {
        view.setTextColor(getResources().getColorStateList(
                accent ? R.color.text_accent : R.color.text_primary));
    }

    private void updateViews() {
        boolean enabled = settings.isEnabled();
        enabledValue.setText(enabled ? R.string.on : R.string.off);
        setAccent(enabledValue, enabled);

        String label = AppLauncher.getLabel(this, settings.getPackage());
        appValue.setText(label != null ? label : getString(R.string.app_none));
        delayValue.setText(getString(R.string.delay_value, settings.getDelaySeconds()));
        permissionRow.setVisibility(needsPermission() ? View.VISIBLE : View.GONE);
    }

    private void toggleEnabled() {
        settings.setEnabled(!settings.isEnabled());
        updateViews();
    }

    private void chooseApp() {
        if (appDialogPending) {
            return;
        }
        appDialogPending = true;
        appValue.setText(R.string.loading);
        AppLauncher.getApps(this, apps -> {
            appDialogPending = false;
            if (isFinishing()) {
                return;
            }
            updateViews();
            if (apps.isEmpty()) {
                Toast.makeText(this, R.string.no_apps, Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> labels = new ArrayList<>(apps.size());
            List<Drawable> icons = new ArrayList<>(apps.size());
            int selected = -1;
            for (int i = 0; i < apps.size(); i++) {
                AppLauncher.App app = apps.get(i);
                labels.add(app.label);
                icons.add(app.icon);
                if (app.packageName.equals(settings.getPackage())) {
                    selected = i;
                }
            }
            ChoiceDialog.show(this, getString(R.string.choose_app), null, labels, icons, selected,
                    index -> {
                        settings.setPackage(apps.get(index).packageName);
                        settings.setEnabled(true);
                        updateViews();
                    });
        });
    }

    private void chooseDelay() {
        int[] delays = Settings.DELAYS;
        List<String> labels = new ArrayList<>(delays.length);
        int selected = -1;
        for (int i = 0; i < delays.length; i++) {
            labels.add(getResources().getQuantityString(R.plurals.seconds, delays[i], delays[i]));
            if (delays[i] == settings.getDelaySeconds()) {
                selected = i;
            }
        }
        ChoiceDialog.show(this, getString(R.string.choose_delay), null, labels, null, selected,
                index -> {
                    settings.setDelaySeconds(delays[index]);
                    updateViews();
                });
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

    // Only reachable on API 23+, the row is hidden on older versions.
    @android.annotation.TargetApi(23)
    private void openPermissionSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(R.string.permission_title, R.string.permission_manual);
        }
    }

    private void openImprint() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.imprint_url)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(R.string.imprint_title, R.string.imprint_no_browser);
        }
    }

    private void showMessage(int titleRes, int messageRes) {
        ChoiceDialog.show(this, getString(titleRes), getString(messageRes),
                Collections.singletonList(getString(R.string.ok)), null, -1, null);
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
