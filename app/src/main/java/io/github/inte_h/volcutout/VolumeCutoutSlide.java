package io.github.inte_h.volcutout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.github.libxposed.api.XposedModule;

public class VolumeCutoutSlide extends XposedModule {
    static final String TAG = "VolumeCutoutSlide";
    private static final String PANEL_TITLE = "VolumeStarStandardWindow";
    private static final String COMMAND_QUEUE = "com.android.systemui.statusbar.CommandQueue";
    private static final String APPEARANCE_REGION = "com.android.internal.view.AppearanceRegion";
    private static final String LETTERBOX_DETAILS = "com.android.internal.statusbar.LetterboxDetails";
    // 삼성 앱별 카메라 영역 설정에서 '카메라 영역까지 채우기'에 해당하는 값
    private static final int CUTOUT_POLICY_FILL = 1;

    // 이 창을 고쳐 둔 동안만 들어 있다.
    private final Map<Window, Integer> originalCutoutModes = new WeakHashMap<>();
    private final Map<String, String> lastLoggedDecisions = new HashMap<>();

    // 기본 화면에 앞에 떠 있는 앱 창들의 영역. 상태 표시줄 정보가 바뀔 때마다 새로 받는다.
    private volatile Rect[] appBounds;
    private volatile String foregroundPackage;
    private volatile PickedApps pickedApps;
    private boolean pickedAppsTried;

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!AppsContract.SYSTEM_UI.equals(param.getPackageName()) || !param.isFirstPackage()) {
            return;
        }
        try {
            hookSystemUi(param.getClassLoader());
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "hooking failed", t);
        }
    }

    private void hookSystemUi(ClassLoader cl) throws ReflectiveOperationException {
        Class<?> regionClass = Class.forName(APPEARANCE_REGION, false, cl);
        final Method getBounds = regionClass.getMethod("getBounds");
        Method barsChanged = Class.forName(COMMAND_QUEUE, false, cl).getMethod("onSystemBarAttributesChanged",
                int.class, int.class, Class.forName("[L" + APPEARANCE_REGION + ";", false, cl), boolean.class,
                int.class, int.class, String.class, Class.forName("[L" + LETTERBOX_DETAILS + ";", false, cl));
        hook(barsChanged).intercept(chain -> {
            try {
                onBarsChanged(chain.getArgs(), getBounds);
            } catch (Throwable t) {
                log(Log.WARN, TAG, "onSystemBarAttributesChanged", t);
            }
            return chain.proceed();
        });
        hook(Dialog.class.getMethod("show")).intercept(chain -> {
            try {
                adjust(((Dialog) chain.getThisObject()).getWindow());
            } catch (Throwable t) {
                log(Log.WARN, TAG, "Dialog.show", t);
            }
            return chain.proceed();
        });
    }

    private void onBarsChanged(List<Object> args, Method getBounds) throws ReflectiveOperationException {
        if ((int) args.get(0) != 0 || args.get(2) == null) {
            return;
        }
        String pkg = (String) args.get(6);
        boolean created = startPickedApps();
        if (created || (pkg != null && !pkg.equals(foregroundPackage))) {
            PickedApps picked = pickedApps;
            if (picked != null) {
                picked.refresh();
            }
        }
        foregroundPackage = pkg;
        Object[] regions = (Object[]) args.get(2);
        Rect[] bounds = new Rect[regions.length];
        for (int i = 0; i < regions.length; i++) {
            bounds[i] = new Rect((Rect) getBounds.invoke(regions[i]));
        }
        appBounds = bounds;
    }

    // 시스템은 이 창을 카메라 영역 앞에서 끊어 배치한다. 창을 카메라 영역까지 넓히고 그 폭만큼 안쪽 여백을 줘서
    // 패널이 멈추는 위치는 그대로 두고, 밀려나가는 경로만 화면 밖까지 이어지게 한다.
    private void adjust(Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams lp = window.getAttributes();
        if (lp.getTitle() == null || !PANEL_TITLE.contentEquals(lp.getTitle())) {
            return;
        }
        View content = window.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        boolean alignedRight = (lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT;
        DisplayCutout cutout = window.getContext().getDisplay().getCutout();
        int strip = cutout == null ? 0 : cutout.getSafeInsetRight();
        boolean cameraSide = alignedRight && strip > 0;
        boolean appFillsStrip = false;
        if (cameraSide) {
            String pkg = foregroundPackage;
            int policy = cutoutPolicy(pkg);
            PickedApps picked = pickedApps;
            boolean inList = picked != null && picked.contains(pkg);
            boolean covers = appCoversRightEdge(window);
            appFillsStrip = covers && (policy == CUTOUT_POLICY_FILL || inList);
            String decision = "policy=" + policy + " picked=" + inList
                    + " coversStrip=" + covers + " apply=" + appFillsStrip;
            if (!decision.equals(lastLoggedDecisions.put(pkg, decision))) {
                log(Log.INFO, TAG, "pkg=" + pkg + " " + decision);
            }
        }
        if (appFillsStrip) {
            if (!originalCutoutModes.containsKey(window)) {
                originalCutoutModes.put(window, lp.layoutInDisplayCutoutMode);
            }
            int inset = strip + lp.x;
            lp.x = 0;
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            content.setPadding(0, 0, inset, 0);
        } else {
            Integer original = originalCutoutModes.remove(window);
            if (original != null) {
                lp.layoutInDisplayCutoutMode = original;
                content.setPadding(0, 0, 0, 0);
            }
        }
    }

    // 시스템 UI 앱 객체가 준비된 뒤에야 목록을 읽을 수 있어서, 상태 표시줄 정보가 처음 들어올 때 시작한다.
    private boolean startPickedApps() {
        if (pickedApps != null) {
            return false;
        }
        synchronized (this) {
            if (pickedAppsTried) {
                return false;
            }
            try {
                Context app = (Context) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication").invoke(null);
                if (app == null) {
                    return false;
                }
                pickedAppsTried = true;
                pickedApps = new PickedApps(app, this);
                return true;
            } catch (Throwable t) {
                pickedAppsTried = true;
                log(Log.WARN, TAG, "picked apps unavailable", t);
                return false;
            }
        }
    }

    private int cutoutPolicy(String pkg) {
        if (pkg == null) {
            return -1;
        }
        try {
            Object atm = Class.forName("android.app.ActivityTaskManager").getMethod("getService").invoke(null);
            int user = (int) Class.forName("android.app.ActivityManager").getMethod("getCurrentUser").invoke(null);
            return (int) Class.forName("android.app.IActivityTaskManager")
                    .getMethod("getCutoutPolicy", int.class, String.class).invoke(atm, user, pkg);
        } catch (Throwable t) {
            log(Log.WARN, TAG, "getCutoutPolicy", t);
            return -1;
        }
    }

    private boolean appCoversRightEdge(Window window) {
        Rect[] bounds = appBounds;
        if (bounds == null) {
            return false;
        }
        int displayWidth = window.getWindowManager().getMaximumWindowMetrics().getBounds().width();
        for (Rect r : bounds) {
            if (r.right >= displayWidth) {
                return true;
            }
        }
        return false;
    }
}
