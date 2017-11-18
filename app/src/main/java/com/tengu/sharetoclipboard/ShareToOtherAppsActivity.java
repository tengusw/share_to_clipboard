package com.tengu.sharetoclipboard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ShareToOtherAppsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent baseIntent = getIntent().getParcelableExtra("intent");
        Bundle bundle = baseIntent.getExtras();
        Intent intent = baseIntent.cloneFilter();

        for (String key : bundle.keySet()) {
            intent.putExtra(key, bundle.get(key).toString());
        }
        shareExceptCurrentApp(intent);

        finish();
    }

    private void shareExceptCurrentApp(Intent intent) {
        intent.setPackage(null);
        intent.setComponent(null);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        String packageNameToHide = getPackageName();
        ArrayList<Intent> targetIntents = new ArrayList<>();
        for (ResolveInfo currentInfo : activities) {
            String packageName = currentInfo.activityInfo.packageName;
            if (!packageNameToHide.equals(packageName)) {
                Intent targetIntent = new Intent(intent);
                targetIntent.setPackage(packageName);
                targetIntents.add(targetIntent);
            }
        }
        if (targetIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0),
                    getString(R.string.share_chooser_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    targetIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        }
    }
}
