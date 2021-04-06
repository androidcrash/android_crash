// Copyright (c) 2019-present, iQIYI, Inc. All rights reserved.
//
package crashcapturer;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.LinkedList;

class ActivityMonitor {

    private static final ActivityMonitor instance = new ActivityMonitor();

    private LinkedList<Activity> activities = null;
    private boolean isAppForeground = false;

    private static final int MAX_ACTIVITY_NUM = 100;

    private ActivityMonitor() {
    }

    static ActivityMonitor getInstance() {
        return instance;
    }

    void initialize(Application application) {
        activities = new LinkedList<Activity>();

        application.registerActivityLifecycleCallbacks(
            new Application.ActivityLifecycleCallbacks() {

                private int activityReferences = 0;
                private boolean isActivityChangingConfigurations = false;

                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    activities.addFirst(activity);
                    if (activities.size() > MAX_ACTIVITY_NUM) {
                        activities.removeLast();
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                        isAppForeground = true;
                    }
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    isActivityChangingConfigurations = activity.isChangingConfigurations();
                    if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                        isAppForeground = false;
                    }
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    activities.remove(activity);
                }
            }
        );
    }

    void finishAllActivities() {
        if (activities != null) {
            for (Activity activity : activities) {
                activity.finish();
            }
            activities.clear();
        }
    }

    boolean isApplicationForeground() {
        return this.isAppForeground;
    }
}
