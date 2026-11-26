package de.autostarttv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
    private TextView settingsLink;
    private boolean appDialogPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);

        // Load the app list in the background right away, so the picker opens instantly.
        AppLauncher.preload(this);

        enabledValue = setupRow(R.id.row_enabled, R.string.autostart, v -> toggleEnabled());
        appValue = setupRow(R.id.row_app, R.string.app, v -> chooseApp());
        setupRow(R.id.row_test, R.string.test, v -> testLaunch()).setText(R.string.test_value);

        settingsLink = findViewById(R.id.text_settings);
        settingsLink.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.text_feedback).setOnClickListener(v -> sendFeedback());
        findViewById(R.id.text_help).setOnClickListener(v ->
                openUrl(R.string.help_url, R.string.help_title));
        findViewById(R.id.text_imprint).setOnClickListener(v ->
                openUrl(R.string.imprint_url, R.string.imprint_title));
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
        // Make sure the background service runs (e.g. after it was stopped by the system).
        WakeService.sync(this);
    }

    private TextView setupRow(int rowId, int labelRes, View.OnClickListener click) {
        View row = findViewById(rowId);
        ((TextView) row.findViewById(R.id.row_label)).setText(labelRes);
        row.setOnClickListener(click);
        return row.findViewById(R.id.row_value);
    }

    private void updateViews() {
        boolean enabled = settings.isEnabled();
        enabledValue.setText(enabled ? R.string.on : R.string.off);
        Ui.setAccent(enabledValue, enabled);

        String label = AppLauncher.getLabel(this, settings.getPackage());
        appValue.setText(label != null ? label : getString(R.string.app_none));

        // Highlight the settings link while the permission is missing.
        Ui.setAccent(settingsLink, Permissions.isMissing(this));
    }

    private void toggleEnabled() {
        settings.setEnabled(!settings.isEnabled());
        updateViews();
        WakeService.sync(this);
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
                        WakeService.sync(this);
                    });
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

    /** Opens the e-mail app with address and subject already filled in. */
    private void sendFeedback() {
        String email = getString(R.string.feedback_email);
        String subject = getString(R.string.feedback_subject);
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:" + email + "?subject=" + Uri.encode(subject)));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ChoiceDialog.show(this, getString(R.string.feedback_title),
                    getString(R.string.feedback_no_mail_app),
                    Collections.singletonList(getString(R.string.ok)), null, -1, null);
        }
    }

    /** Opens a web page; shows the address if there is no browser (common on TVs). */
    private void openUrl(int urlRes, int titleRes) {
        String url = getString(urlRes);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            String address = url.replaceFirst("^https?://", "");
            ChoiceDialog.show(this, getString(titleRes), getString(R.string.no_browser, address),
                    Collections.singletonList(getString(R.string.ok)), null, -1, null);
        }
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
