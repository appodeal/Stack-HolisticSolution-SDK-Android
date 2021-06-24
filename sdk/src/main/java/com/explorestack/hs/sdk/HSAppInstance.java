package com.explorestack.hs.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class HSAppInstance {

    private static final String TAG = HSApp.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static HSAppInstance instance;

    public static HSAppInstance get() {
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
    private final HSRegulatorDispatcher regulatorDelegate = new HSRegulatorDispatcher(this);
    @NonNull
    private final HSLifecycleDelegate lifecycleDelegate = new HSLifecycleDelegate();
    @NonNull
    private final List<HSAppInitializeListener> listeners = new CopyOnWriteArrayList<>();
    @NonNull
    private final String trackId = UUID.randomUUID().toString();

    @Nullable
    private Context appContext;
    @Nullable
    private WeakReference<Activity> weakTopActivity;
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
            appContext = context.getApplicationContext();
            ((Application) appContext).registerActivityLifecycleCallbacks(lifecycleDelegate);
            initializer = new HSAppInitializer(new HSSimpleContextProvider(context), this, config, listenerDelegate);
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

    public String getVersion() {
        return BuildConfig.HS_VERSION;
    }

    @NonNull
    String getTrackId() {
        return trackId;
    }

    @NonNull
    HSEventsDispatcher getEventsDispatcher() {
        return eventsDispatcher;
    }

    @NonNull
    HSIAPValidateDispatcher getInAppPurchaseValidateDispatcher() {
        return inAppPurchaseValidateDispatcher;
    }

    @NonNull
    HSConnectorDelegate getConnectorDelegate() {
        return connectorDelegate;
    }

    @NonNull
    HSRegulatorDispatcher getRegulatorDelegate() {
        return regulatorDelegate;
    }

    @NonNull
    HSLifecycleDelegate getLifecycleDelegate() {
        return lifecycleDelegate;
    }

    @Nullable
    Context getAppContext() {
        return appContext;
    }

    @Nullable
    Activity getTopActivity() {
        return weakTopActivity != null ? weakTopActivity.get() : null;
    }

    void setTopActivity(@Nullable Activity activity) {
        if (activity != null) {
            weakTopActivity = new WeakReference<>(activity);
        }
    }

    private static final class HSAppInitializer extends Thread {

        private static final String TAG = "HSAppInitializer";
        private static final Executor executor = Executors.newCachedThreadPool();

        @NonNull
        private final HSContextProvider contextProvider;
        @NonNull
        private final HSAppInstance app;
        @NonNull
        private final HSAppConfig appConfig;
        @NonNull
        private final HSAppInitializeListener listener;
        @Nullable
        private List<HSError> errors;

        public HSAppInitializer(@NonNull HSContextProvider contextProvider,
                                @NonNull HSAppInstance app,
                                @NonNull HSAppConfig appConfig,
                                @NonNull HSAppInitializeListener listener) {
            this.contextProvider = contextProvider;
            this.app = app;
            this.appConfig = appConfig;
            this.listener = listener;
        }

        @Override
        public void run() {
            Context targetContext = contextProvider.getApplicationContext();
            final List<HSService> services = HSComponentRegistry.registerServices(app, targetContext);
            final List<HSRegulator> regulators = HSComponentRegistry.registerRegulators(app, targetContext);
            final List<HSConnector> connectors = HSComponentRegistry.registerConnectors(app, targetContext);

            if (isListNullOrEmpty(services)) {
                addError(HSError.NoServices);
            }
            if (isListNullOrEmpty(regulators)) {
                addError(HSError.NoRegulator);
            }
            if (isListNullOrEmpty(connectors)) {
                addError(HSError.NoConnectors);
            }
            final HSAdvertisingProfile advertisingProfile = HSAdvertisingInfo.updateInfo(targetContext);
            final HSAppParamsImpl appParams = new HSAppParamsImpl(appConfig, app, advertisingProfile);

            // Regulators initialization
            initializeComponents(
                    regulators,
                    new HSRegulatorInitializerBuilder(app, contextProvider, appParams)
            );
            HSNetworkRequest.Callback<JSONObject, HSError> callback
                    = new HSNetworkRequest.Callback<JSONObject, HSError>() {
                @Override
                public void onSuccess(@Nullable JSONObject result) {
                    // Services initialization
                    initializeComponents(
                            services,
                            new HSServiceInitializerBuilder(app, contextProvider, appParams, result)
                    );
                    // Connectors initialization
                    initializeComponents(
                            connectors,
                            new HSConnectorInitializerBuilder(app, contextProvider, appParams)
                    );
                    listener.onAppInitialized(getErrors());
                }

                @Override
                public void onFail(@Nullable HSError result) {
                    if (result != null) {
                        addError(result);
                    }
                    // Connectors initialization
                    initializeComponents(
                            connectors,
                            new HSConnectorInitializerBuilder(app, contextProvider, appParams)
                    );
                    listener.onAppInitialized(getErrors());
                }
            };
            HSApiRequest.initRequest(targetContext, appParams, callback);
        }

        private <T extends HSComponent> void initializeComponents(
                @NonNull List<T> registeredComponents,
                @NonNull HSComponentInitializerBuilder<T> initializerBuilder
        ) {
            if (registeredComponents.isEmpty()) {
                return;
            }
            List<T> components = new ArrayList<>();
            for (T component : registeredComponents) {
                if (initializerBuilder.isComponentEnable(component)) {
                    components.add(component);
                } else {
                    HSLogger.logError(TAG, String.format("HSComponent (%s) disabled on the server!", component.getName()));
                }
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
    }

    private static abstract class HSComponentInitializer<T extends HSComponent> implements Runnable {

        @NonNull
        protected final HSAppInstance app;
        @NonNull
        protected final HSContextProvider contextProvider;
        @NonNull
        protected final T component;
        @NonNull
        protected final HSAppParams appParams;
        @NonNull
        protected final HSComponentCallback callback;

        private boolean isFinished = false;

        public HSComponentInitializer(@NonNull HSAppInstance app,
                                      @NonNull HSContextProvider contextProvider,
                                      @NonNull T component,
                                      @NonNull HSAppParams appParams,
                                      @NonNull HSComponentCallback callback) {
            this.app = app;
            this.contextProvider = contextProvider;
            this.component = component;
            this.appParams = appParams;
            this.callback = callback;
        }

        @Override
        public void run() {
            HSLogger.logInfo(component.getName(), "Version: " + component.getVersion());
            HSLogger.logInfo(component.getName(), "Initialization start");
            HSCoreUtils.startTimeout(appParams.getComponentInitializeTimeoutMs(), new TimerTask() {
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
            final Context targetContext = contextProvider.getApplicationContext();
            if (component.isEventsEnabled()) {
                app.getEventsDispatcher().addHandler(
                        component, component.createEventsHandler(targetContext));
            }
            app.getLifecycleDelegate().addCallback(
                    component, component.getLifecycleCallback(targetContext));
            app.getInAppPurchaseValidateDispatcher().addHandler(
                    component, component.createIAPValidateHandler(targetContext));
            doProcess(componentCallback);
        }

        abstract void doProcess(@NonNull HSComponentCallback callback);
    }

    private abstract static class HSComponentInitializerBuilder<T extends HSComponent> {

        @NonNull
        protected final HSAppInstance app;
        @NonNull
        protected final HSContextProvider contextProvider;
        @NonNull
        protected final HSAppParams appParams;

        HSComponentInitializerBuilder(@NonNull HSAppInstance app,
                                      @NonNull HSContextProvider contextProvider,
                                      @NonNull HSAppParams appParams) {
            this.app = app;
            this.contextProvider = contextProvider;
            this.appParams = appParams;
        }

        boolean isComponentEnable(@NonNull T component) {
            return true;
        }

        abstract HSComponentInitializer<T> build(@NonNull T component,
                                                 @NonNull HSComponentCallback callback);

        @NonNull
        HSComponentParams getParams(@NonNull T component) {
            return new HSComponentParamsImpl(appParams);
        }
    }

    private static final class HSServiceInitializerBuilder extends HSComponentInitializerBuilder<HSService> {

        @NonNull
        private final JSONObject serverParams;

        HSServiceInitializerBuilder(@NonNull HSAppInstance app,
                                    @NonNull HSContextProvider contextProvider,
                                    @NonNull HSAppParams appParams,
                                    @Nullable JSONObject serverParams) {
            super(app, contextProvider, appParams);
            this.serverParams = serverParams == null ? new JSONObject() : serverParams;
        }

        @Override
        boolean isComponentEnable(@NonNull HSService component) {
            return serverParams.has(component.getName().toLowerCase());
        }

        @Override
        HSComponentInitializer<HSService> build(@NonNull HSService component,
                                                @NonNull HSComponentCallback callback) {
            return new HSComponentInitializer<HSService>(app, contextProvider, component, appParams, callback) {
                @Override
                void doProcess(@NonNull HSComponentCallback callback) {
                    component.start(contextProvider.getContext(),
                                    getParams(component),
                                    callback,
                                    app.getConnectorDelegate());
                }
            };
        }

        @NonNull
        @Override
        HSComponentParams getParams(@NonNull HSService component) {
            JSONObject extra = serverParams.optJSONObject(component.getName().toLowerCase());
            extra = extra == null ? new JSONObject() : extra;
            configureComponent(component, extra);
            return new HSComponentParamsImpl(appParams, extra);
        }

        private void configureComponent(@NonNull HSService component,
                                        @NonNull JSONObject extra) {
            if (extra.has("tracking")) {
                boolean tracking = extra.optBoolean("tracking", true);
                component.setEventsEnabled(tracking);
            }
        }
    }

    private static final class HSConnectorInitializerBuilder extends HSComponentInitializerBuilder<HSConnector> {

        HSConnectorInitializerBuilder(@NonNull HSAppInstance app,
                                      @NonNull HSContextProvider contextProvider,
                                      @NonNull HSAppParams appParams) {
            super(app, contextProvider, appParams);
        }

        @Override
        HSComponentInitializer<HSConnector> build(@NonNull HSConnector component,
                                                  @NonNull HSComponentCallback callback) {
            return new HSComponentInitializer<HSConnector>(app, contextProvider, component, appParams, callback) {
                @Override
                void doProcess(@NonNull HSComponentCallback callback) {
                    component.initialize(contextProvider.getActivity(),
                                         getParams(component),
                                         callback,
                                         app.getRegulatorDelegate().getBestRegulator());
                }
            };
        }
    }

    private static final class HSRegulatorInitializerBuilder extends HSComponentInitializerBuilder<HSRegulator> {

        HSRegulatorInitializerBuilder(@NonNull HSAppInstance app,
                                      @NonNull HSContextProvider contextProvider,
                                      @NonNull HSAppParams appParams) {
            super(app, contextProvider, appParams);
        }

        @Override
        HSComponentInitializer<HSRegulator> build(@NonNull HSRegulator component,
                                                  @NonNull HSComponentCallback callback) {
            return new HSComponentInitializer<HSRegulator>(app, contextProvider, component, appParams, callback) {
                @Override
                void doProcess(@NonNull HSComponentCallback callback) {
                    component.start(contextProvider.getActivity(),
                                    getParams(component),
                                    callback);
                }
            };
        }
    }
}