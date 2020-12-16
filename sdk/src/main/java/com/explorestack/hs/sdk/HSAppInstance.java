package com.explorestack.hs.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class HSAppInstance {

    private static final String TAG = HSApp.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static HSAppInstance instance;

    public static HSAppInstance getInstance() {
        if (instance == null) {
            instance = new HSAppInstance();
        }
        return instance;
    }

    @NonNull
    private final HSEventsDispatcher eventsDispatcher = new HSEventsDispatcher(this);
    @NonNull
    private final HSIAPValidateDispatcher inAppPurchaseValidateDispatcher =
            new HSIAPValidateDispatcher(this);
    @NonNull
    private final HSConnectorDelegate connectorDelegate = new HSConnectorDelegate(this);
    @NonNull
    private final List<HSAppInitializeListener> listeners = new CopyOnWriteArrayList<>();

    @Nullable
    private Context context;
    @Nullable
    private HSAppInitializer initializer;
    private boolean isInitialized = false;

    public boolean isInitialized() {
        return isInitialized;
    }

    public void initialize(@NonNull Context context,
                           @NonNull HSAppConfig config,
                           @Nullable final HSAppInitializeListener listener) {
        if (initializer == null) {
            final HSAppInitializeListener listenerDelegate = new HSAppInitializeListener() {
                @Override
                public void onAppInitialized(@Nullable List<HSError> errors) {
                    HSLogger.logInfo(TAG, "Initialized");
                    if (errors != null) {
                        for (HSError error : errors) {
                            HSLogger.logError("Error", error.toString());
                        }
                    }
                    isInitialized = true;
                    initializer = null;
                    if (listener != null) {
                        listener.onAppInitialized(errors);
                    }
                    notifyInitialized(errors);
                    eventsDispatcher.dispatchPendingEvents();
                    inAppPurchaseValidateDispatcher.dispatchPendingPurchase();
                }
            };
            this.context = context.getApplicationContext();
            initializer = new HSAppInitializer(this.context, this, config, connectorDelegate, listenerDelegate);
            initializer.start();
        }
    }

    public void addInitializeListener(@NonNull HSAppInitializeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeInitializeListener(@NonNull HSAppInitializeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifyInitialized(@Nullable final List<HSError> errors) {
        final List<HSAppInitializeListener> targetListeners = new ArrayList<>(listeners);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (HSAppInitializeListener listener : targetListeners) {
                    listener.onAppInitialized(errors);
                }
            }
        });
    }

    public void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        eventsDispatcher.logEvent(eventName, params);
    }

    public void validateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                      @Nullable HSInAppPurchaseValidateListener listener) {
        inAppPurchaseValidateDispatcher.validateInAppPurchase(purchase, listener);
    }

    @NonNull
    public HSEventsDispatcher getEventsDispatcher() {
        return eventsDispatcher;
    }

    @NonNull
    public HSIAPValidateDispatcher getInAppPurchaseValidateDispatcher() {
        return inAppPurchaseValidateDispatcher;
    }

    @NonNull
    public HSConnectorDelegate getConnectorDelegate() {
        return connectorDelegate;
    }

    @Nullable
    public Context getContext() {
        return context;
    }

    private static final class HSAppInitializer extends Thread {

        private static final Executor executor = Executors.newCachedThreadPool();

        @NonNull
        private final Context context;
        @NonNull
        private final HSAppInstance app;
        @NonNull
        private final HSAppConfig appConfig;
        @NonNull
        private final HSConnectorDelegate connectorDelegate;
        @NonNull
        private final HSAppInitializeListener listener;
        @Nullable
        private List<HSError> errors;

        public HSAppInitializer(@NonNull Context context,
                                @NonNull HSAppInstance app,
                                @NonNull HSAppConfig appConfig,
                                @NonNull HSConnectorDelegate connectorDelegate,
                                @NonNull HSAppInitializeListener listener) {
            this.context = context;
            this.app = app;
            this.appConfig = appConfig;
            this.connectorDelegate = connectorDelegate;
            this.listener = listener;
        }

        @Override
        public void run() {
            final List<HSService> services = appConfig.getServices();
            final List<HSConnector> connectors = appConfig.getConnectors();
            if (isListNullOrEmpty(services)) {
                addError(HSError.NoServices);
            }
            if (isListNullOrEmpty(connectors)) {
                addError(HSError.NoConnectors);
            }
            final HSAppParamsImpl appParams = new HSAppParamsImpl(appConfig);
            //Connectors initialization
            initializeComponents(connectors, new HSComponentInitializerBuilder<HSConnector>() {
                @Override
                public HSComponentInitializer<HSConnector> build(@NonNull HSConnector component,
                                                                 @NonNull HSComponentCallback callback) {
                    return new HSConnectorInitializer(context, app, component, appParams, callback);
                }
            });
            // Service initialization
            connectorDelegate.setConnectors(connectors);
            initializeComponents(services, new HSComponentInitializerBuilder<HSService>() {
                @Override
                public HSComponentInitializer<HSService> build(@NonNull HSService component,
                                                               @NonNull HSComponentCallback callback) {
                    return new HSServiceInitializer(
                            context, app, component, appParams, callback, connectorDelegate);
                }
            });
            listener.onAppInitialized(getErrors());
        }

        private <T extends HSComponent> void initializeComponents(
                @NonNull List<T> components,
                @NonNull HSComponentInitializerBuilder<T> initializerBuilder
        ) {
            if (components.isEmpty()) {
                return;
            }
            final CountDownLatch componentsWaiter = new CountDownLatch(components.size());
            final HSComponentCallback componentsCallback = new HSComponentCallback() {
                @Override
                public void onFinished() {
                    componentsWaiter.countDown();
                }

                @Override
                public void onFail(@NonNull HSError error) {
                    addError(error);
                    componentsWaiter.countDown();
                }
            };
            for (T component : components) {
                HSComponentInitializer<T> initializer =
                        initializerBuilder.build(component, componentsCallback);
                executor.execute(initializer);
            }
            try {
                componentsWaiter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void addError(@NonNull HSError error) {
            if (errors == null) {
                errors = new ArrayList<>();
            }
            errors.add(error);
        }

        @Nullable
        public List<HSError> getErrors() {
            return errors != null ? Collections.unmodifiableList(errors) : null;
        }

        private <T> boolean isListNullOrEmpty(@Nullable List<T> list) {
            return list == null || list.isEmpty();
        }

        private interface HSComponentInitializerBuilder<T extends HSComponent> {
            HSComponentInitializer<T> build(@NonNull T component,
                                            @NonNull HSComponentCallback callback);
        }
    }

    private static abstract class HSComponentInitializer<T extends HSComponent> implements Runnable {

        @NonNull
        protected final HSAppInstance app;
        @NonNull
        protected final Context context;
        @NonNull
        protected final T component;
        @NonNull
        protected final HSAppParams appParams;
        @NonNull
        protected final HSComponentCallback callback;

        private boolean isFinished = false;

        public HSComponentInitializer(@NonNull HSAppInstance app,
                                      @NonNull Context context,
                                      @NonNull T component,
                                      @NonNull HSAppParams appParams,
                                      @NonNull HSComponentCallback callback) {
            this.app = app;
            this.context = context;
            this.component = component;
            this.appParams = appParams;
            this.callback = callback;
        }

        @Override
        public void run() {
            HSLogger.logInfo(component.getName(), "Version: " + component.getVersion());
            HSLogger.logInfo(component.getName(), "Initialization start");
            HSUtils.startTimeout(appParams.getComponentInitializeTimeoutMs(), new TimerTask() {
                @Override
                public void run() {
                    if (!isFinished) {
                        HSLogger.logInfo(component.getName(), "Initialization timeout");
                        callback.onFail(HSError.forComponent(component, "Timeout"));
                        isFinished = true;
                    }
                }
            });
            HSComponentCallback componentCallback = new HSComponentCallback() {
                @Override
                public void onFinished() {
                    HSLogger.logInfo(component.getName(), "Initialization finished");
                    if (!isFinished) {
                        callback.onFinished();
                        isFinished = true;
                    }
                }

                @Override
                public void onFail(@NonNull HSError error) {
                    HSLogger.logInfo(component.getName(), "Initialization fail: " + error);
                    if (!isFinished) {
                        callback.onFail(error);
                        isFinished = true;
                    }
                }
            };
            final Context targetContext = context.getApplicationContext();
            if (component.isEventsEnabled()) {
                app.getEventsDispatcher().addHandler(
                        component, component.createEventsHandler(targetContext));
            }
            app.getInAppPurchaseValidateDispatcher().addHandler(
                    component, component.createIAPValidateHandler(targetContext));
            doProcess(componentCallback);
        }

        abstract void doProcess(@NonNull HSComponentCallback callback);
    }

    private static final class HSConnectorInitializer extends HSComponentInitializer<HSConnector> implements Runnable {

        public HSConnectorInitializer(@NonNull Context context,
                                      @NonNull HSAppInstance app,
                                      @NonNull HSConnector component,
                                      @NonNull HSAppParams appParams,
                                      @NonNull HSComponentCallback callback) {
            super(app, context, component, appParams, callback);
        }

        @Override
        void doProcess(@NonNull HSComponentCallback callback) {
            component.initialize(context, appParams, callback);
        }
    }

    private static final class HSServiceInitializer extends HSComponentInitializer<HSService> implements Runnable {

        @NonNull
        private final HSConnectorCallback connectorCallback;

        public HSServiceInitializer(@NonNull Context context,
                                    @NonNull HSAppInstance app,
                                    @NonNull HSService component,
                                    @NonNull HSAppParams appParams,
                                    @NonNull HSComponentCallback callback,
                                    @NonNull HSConnectorCallback connectorCallback) {
            super(app, context, component, appParams, callback);
            this.connectorCallback = connectorCallback;
        }

        @Override
        void doProcess(@NonNull HSComponentCallback callback) {
            component.start(context, appParams, callback, connectorCallback);
        }
    }
}
