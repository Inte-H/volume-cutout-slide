package io.github.hit.volcutout;

import android.net.Uri;

final class Prefs {
    static final String AUTHORITY = "io.github.hit.volcutout.apps";
    static final Uri APPS_URI = Uri.parse("content://" + AUTHORITY + "/apps");
    static final String COLUMN_PACKAGE = "package";
    static final String FILE = "apps";
    static final String EXTRA_APPS = "extra_apps";

    private Prefs() {
    }
}
