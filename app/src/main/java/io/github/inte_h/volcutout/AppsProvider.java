package io.github.inte_h.volcutout;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

// LSPosed는 이 앱 프로세스가 뜬 뒤에 서비스를 넘겨주므로, 프로세스가 막 시작된 조회는 잠시 기다린다.
public class AppsProvider extends ContentProvider {
    private static final long SERVICE_WAIT_MS = 3000;

    private final Object lock = new Object();
    private XposedService service;

    @Override
    public boolean onCreate() {
        XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
            @Override
            public void onServiceBind(XposedService bound) {
                synchronized (lock) {
                    service = bound;
                    lock.notifyAll();
                }
            }

            @Override
            public void onServiceDied(XposedService died) {
                synchronized (lock) {
                    if (service == died) {
                        service = null;
                    }
                }
            }
        });
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!callerAllowed()) {
            throw new SecurityException("not allowed: uid " + Binder.getCallingUid());
        }
        XposedService bound = awaitService();
        if (bound == null) {
            throw new IllegalStateException("LSPosed service not bound");
        }
        MatrixCursor cursor = new MatrixCursor(new String[] {AppsContract.COLUMN_PACKAGE});
        for (String pkg : bound.getScope()) {
            if (!AppsContract.SYSTEM_UI.equals(pkg)) {
                cursor.addRow(new Object[] {pkg});
            }
        }
        return cursor;
    }

    private XposedService awaitService() {
        long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
        synchronized (lock) {
            while (service == null) {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) {
                    break;
                }
                try {
                    lock.wait(left);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return service;
        }
    }

    private boolean callerAllowed() {
        int uid = Binder.getCallingUid();
        if (uid == Process.ROOT_UID || uid == Process.SHELL_UID || uid == Process.myUid()) {
            return true;
        }
        try {
            return uid == getContext().getPackageManager().getPackageUid(AppsContract.SYSTEM_UI, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
