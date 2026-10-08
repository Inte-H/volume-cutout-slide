package io.github.hit.volcutout;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;

// 시스템 UI 안에서 모듈 설정의 앱 목록을 캐시해 둔다.
final class PickedApps {
    private final Context context;
    private final Handler handler;
    private volatile Set<String> apps = Collections.emptySet();

    PickedApps(Context context) {
        this.context = context;
        HandlerThread thread = new HandlerThread("VolumeCutoutSlide-apps");
        thread.start();
        handler = new Handler(thread.getLooper());
        context.getContentResolver().registerContentObserver(Prefs.APPS_URI, false, new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                reload();
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                reload();
            }
        });
    }

    boolean contains(String pkg) {
        return pkg != null && apps.contains(pkg);
    }

    private void reload() {
        Set<String> loaded = new HashSet<>();
        try (Cursor c = context.getContentResolver().query(Prefs.APPS_URI, null, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    loaded.add(c.getString(0));
                }
            }
            apps = Collections.unmodifiableSet(loaded);
            XposedBridge.log("VolumeCutoutSlide: picked apps " + apps);
        } catch (Throwable t) {
            XposedBridge.log("VolumeCutoutSlide: reading picked apps failed: " + t);
        }
    }
}
