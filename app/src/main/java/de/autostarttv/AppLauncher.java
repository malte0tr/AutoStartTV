package de.autostarttv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lists installed apps (in the background, cached) and starts them. */
final class AppLauncher {

    /** An installed, launchable app. */
    static final class App {
        final String packageName;
        final String label;
        final Drawable icon;

        App(String packageName, String label, Drawable icon) {
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
        }
    }

    interface Callback {
        void onApps(List<App> apps);
    }

    /** One background thread: loading never blocks the screen. */
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile List<App> cache;

    private AppLauncher() {
    }

    /** Starts loading the app list in the background so it is ready when needed. */
    static void preload(Context context) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> cache = loadApps(appContext));
    }

    /** Delivers the app list on the main thread; uses the cache if it is ready. */
    static void getApps(Context context, Callback callback) {
        List<App> cached = cache;
        if (cached != null) {
            callback.onApps(cached);
            return;
        }
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<App> apps = cache;
            if (apps == null) {
                apps = loadApps(appContext);
                cache = apps;
            }
            List<App> result = apps;
            MAIN.post(() -> callback.onApps(result));
        });
    }

    private static List<App> loadApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Map<String, ResolveInfo> found = new LinkedHashMap<>();
        collect(pm, found, Intent.CATEGORY_LEANBACK_LAUNCHER, context.getPackageName());
        collect(pm, found, Intent.CATEGORY_LAUNCHER, context.getPackageName());

        List<App> list = new ArrayList<>(found.size());
        for (Map.Entry<String, ResolveInfo> entry : found.entrySet()) {
            ResolveInfo info = entry.getValue();
            Drawable icon;
            try {
                icon = info.loadIcon(pm);
            } catch (RuntimeException e) {
                icon = null;
            }
            list.add(new App(entry.getKey(), info.loadLabel(pm).toString(), icon));
        }
        Collator collator = Collator.getInstance();
        Collections.sort(list, (a, b) -> collator.compare(a.label, b.label));
        return Collections.unmodifiableList(list);
    }

    private static void collect(PackageManager pm, Map<String, ResolveInfo> found,
                                String category, String ownPackage) {
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo info : pm.queryIntentActivities(intent, 0)) {
            String pkg = info.activityInfo.packageName;
            if (!pkg.equals(ownPackage) && !found.containsKey(pkg)) {
                found.put(pkg, info);
            }
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
        Intent intent = pm.getLeanbackLaunchIntentForPackage(packageName);
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
