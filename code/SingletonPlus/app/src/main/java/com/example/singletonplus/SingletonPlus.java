package com.example.singletonplus;

import androidx.activity.ComponentActivity;
import androidx.annotation.CallSuper;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class SingletonPlus implements LifecycleObserver {
    enum STATES {
        STARTING,
        STARTED,
        END_RELEASED,
        END_UNRELEASED
    }

    protected static STATES state;
    private static ComponentActivity activity;
    private static Object resource;

    private volatile static SingletonPlus instance = null;

    public static SingletonPlus getInstance() {
        if (instance == null) {
            synchronized (SingletonPlus.class) {
                if (instance == null) {
                    instance = new SingletonPlus();
                }
            }
        }
        return instance;
    }

    @CallSuper
    public void initRes() {
        resource = new Object();
        state = STATES.STARTED;
    }

    @CallSuper
    public void releaseRes() {
        if (state == STATES.STARTING) {
            state = STATES.END_RELEASED;
            return;
        }
        resource = null;
        state = STATES.END_RELEASED;
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onActivityCreate() {
        if (state == STATES.END_UNRELEASED)
            return;
        state = STATES.STARTING;
        initRes();
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onActivityDestroy() {
        state = STATES.END_UNRELEASED;
    }
}
