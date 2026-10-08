package io.github.hit.volcutout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class AppPickerActivity extends Activity {
    private SharedPreferences prefs;
    private final List<String> packages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("카메라 영역을 스스로 채우는 앱");
        prefs = getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE);

        PackageManager pm = getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        TreeMap<String, String> byLabel = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ResolveInfo ri : pm.queryIntentActivities(launcher, 0)) {
            String pkg = ri.activityInfo.packageName;
            byLabel.put(ri.loadLabel(pm) + "  (" + pkg + ")", pkg);
        }
        List<String> labels = new ArrayList<>(byLabel.keySet());
        packages.addAll(byLabel.values());

        final ListView list = new ListView(this);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, labels));
        Set<String> picked = prefs.getStringSet(Prefs.EXTRA_APPS, Collections.<String>emptySet());
        for (int i = 0; i < packages.size(); i++) {
            list.setItemChecked(i, picked.contains(packages.get(i)));
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                save(list);
            }
        });
        setContentView(list);
    }

    private void save(ListView list) {
        Set<String> picked = new HashSet<>();
        for (int i = 0; i < packages.size(); i++) {
            if (list.isItemChecked(i)) {
                picked.add(packages.get(i));
            }
        }
        prefs.edit().putStringSet(Prefs.EXTRA_APPS, picked).commit();
        getContentResolver().notifyChange(Prefs.APPS_URI, null);
    }
}
