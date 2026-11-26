package de.autostarttv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Settings page: start delay and the permission needed for autostart. */
public class SettingsActivity extends Activity {

    private Settings settings;
    private TextView delayValue;
    private TextView permissionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        View delayRow = findViewById(R.id.row_delay);
        ((TextView) delayRow.findViewById(R.id.row_label)).setText(R.string.delay);
        delayValue = delayRow.findViewById(R.id.row_value);
        delayRow.setOnClickListener(v -> chooseDelay());

        View row = findViewById(R.id.row_permission);
        ((TextView) row.findViewById(R.id.row_label)).setText(R.string.permission);
        permissionValue = row.findViewById(R.id.row_value);
        row.setOnClickListener(v -> grantPermission());
        if (savedInstanceState == null) {
            delayRow.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDelay();
        boolean missing = Permissions.isMissing(this);
        if (!Permissions.isNeeded()) {
            permissionValue.setText(R.string.permission_not_needed);
        } else {
            permissionValue.setText(missing ? R.string.permission_missing : R.string.permission_granted);
        }
        Ui.setAccent(permissionValue, missing);
    }

    private void updateDelay() {
        delayValue.setText(getString(R.string.delay_value, settings.getDelaySeconds()));
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
                    updateDelay();
                });
    }

    private void grantPermission() {
        if (!Permissions.isNeeded()) {
            return;
        }
        // Also open the screen when already granted, so it can be revoked there.
        if (!Permissions.openSettings(this)) {
            ChoiceDialog.show(this, getString(R.string.permission_title),
                    getString(R.string.permission_manual),
                    Collections.singletonList(getString(R.string.ok)), null, -1, null);
        }
    }
}
