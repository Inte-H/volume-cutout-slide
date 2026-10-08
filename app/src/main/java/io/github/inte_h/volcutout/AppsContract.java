package io.github.inte_h.volcutout;

import android.net.Uri;

final class AppsContract {
    static final String AUTHORITY = "io.github.inte_h.volcutout.apps";
    static final Uri APPS_URI = Uri.parse("content://" + AUTHORITY + "/apps");
    static final String COLUMN_PACKAGE = "package";
    static final String SYSTEM_UI = "com.android.systemui";

    private AppsContract() {
    }
}
