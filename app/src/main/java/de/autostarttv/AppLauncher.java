package de.autostarttv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Helpers to list installed apps and start one of them. */
final class AppLauncher {

    /** An installed, launchable app. */
    static final class App {
        final String packageName;
        final String label;

        App(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }

    private AppLauncher() {
    }

    static List<App> listApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Map<String, App> apps = new LinkedHashMap<>();
        addApps(pm, apps, Intent.CATEGORY_LEANBACK_LAUNCHER, context.getPackageName());
        addApps(pm, apps, Intent.CATEGORY_LAUNCHER, context.getPackageName());

        List<App> list = new ArrayList<>(apps.values());
        Collator collator = Collator.getInstance();
        Collections.sort(list, (a, b) -> collator.compare(a.label, b.label));
        return list;
    }

    private static void addApps(PackageManager pm, Map<String, App> apps,
                                String category, String ownPackage) {
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo info : pm.queryIntentActivities(intent, 0)) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(ownPackage) || apps.containsKey(pkg)) {
                continue;
            }
            apps.put(pkg, new App(pkg, info.loadLabel(pm).toString()));
        }
    }

    /** Returns the app's name, or null if it is no longer installed. */
    static String getLabel(Context context, String packageName) {
        if (packageName == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /** Starts the app. Returns false if it could not be started. */
    static boolean launch(Context context, String packageName) {
        if (packageName == null) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= 21) {
            intent = pm.getLeanbackLaunchIntentForPackage(packageName);
        }
        if (intent == null) {
            intent = pm.getLaunchIntentForPackage(packageName);
        }
        if (intent == null) {
            return false;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
