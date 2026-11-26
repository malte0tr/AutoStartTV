package de.autostarttv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;

/** Settings page: shows and grants the permission needed for autostart. */
public class SettingsActivity extends Activity {

    private TextView permissionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        View row = findViewById(R.id.row_permission);
        ((TextView) row.findViewById(R.id.row_label)).setText(R.string.permission);
        permissionValue = row.findViewById(R.id.row_value);
        row.setOnClickListener(v -> grantPermission());
        if (savedInstanceState == null) {
            row.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean missing = Permissions.isMissing(this);
        if (!Permissions.isNeeded()) {
            permissionValue.setText(R.string.permission_not_needed);
        } else {
            permissionValue.setText(missing ? R.string.permission_missing : R.string.permission_granted);
        }
        Ui.setAccent(permissionValue, missing);
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
