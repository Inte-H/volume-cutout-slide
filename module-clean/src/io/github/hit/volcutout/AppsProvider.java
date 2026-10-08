package io.github.hit.volcutout;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;

import java.util.Collections;
import java.util.Set;

public class AppsProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!callerAllowed()) {
            throw new SecurityException("not allowed: uid " + Binder.getCallingUid());
        }
        Set<String> apps = getContext().getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
                .getStringSet(Prefs.EXTRA_APPS, Collections.<String>emptySet());
        MatrixCursor cursor = new MatrixCursor(new String[] {Prefs.COLUMN_PACKAGE});
        for (String pkg : apps) {
            cursor.addRow(new Object[] {pkg});
        }
        return cursor;
    }

    private boolean callerAllowed() {
        int uid = Binder.getCallingUid();
        if (uid == Process.ROOT_UID || uid == Process.SHELL_UID || uid == Process.myUid()) {
            return true;
        }
        try {
            return uid == getContext().getPackageManager().getPackageUid("com.android.systemui", 0);
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
