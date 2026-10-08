package io.github.inte_h.volcutout;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;

final class PickedApps {
    private static final long MIN_RELOAD_INTERVAL_MS = 30_000;

    private final Context context;
    private final XposedInterface xposed;
    private final Handler handler;
    private volatile Set<String> apps = Collections.emptySet();
    private final Runnable reload = this::reloadIfStale;
    private long lastAttemptAt = -MIN_RELOAD_INTERVAL_MS;

    PickedApps(Context context, XposedInterface xposed) {
        this.context = context;
        this.xposed = xposed;
        HandlerThread thread = new HandlerThread("VolumeCutoutSlide-apps");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    boolean contains(String pkg) {
        return pkg != null && apps.contains(pkg);
    }

    void refresh() {
        handler.post(reload);
    }

    private void reloadIfStale() {
        long now = SystemClock.uptimeMillis();
        long wait = lastAttemptAt + MIN_RELOAD_INTERVAL_MS - now;
        if (wait > 0) {
            if (!handler.hasCallbacks(reload)) {
                handler.postDelayed(reload, wait);
            }
            return;
        }
        lastAttemptAt = now;
        read();
    }

    private void read() {
        Set<String> fresh = new HashSet<>();
        try (Cursor c = context.getContentResolver().query(AppsContract.APPS_URI, null, null, null, null)) {
            if (c == null) {
                xposed.log(Log.WARN, VolumeCutoutSlide.TAG, "picked apps provider missing");
                return;
            }
            int column = c.getColumnIndexOrThrow(AppsContract.COLUMN_PACKAGE);
            while (c.moveToNext()) {
                fresh.add(c.getString(column));
            }
        } catch (Throwable t) {
            xposed.log(Log.WARN, VolumeCutoutSlide.TAG, "reading picked apps failed", t);
            return;
        }
        if (!fresh.equals(apps)) {
            apps = Collections.unmodifiableSet(fresh);
            xposed.log(Log.INFO, VolumeCutoutSlide.TAG, "picked apps " + apps);
        }
    }
}
