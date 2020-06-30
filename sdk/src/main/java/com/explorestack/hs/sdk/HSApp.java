package com.explorestack.hs.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HSApp {

    private static final String TAG = HSApp.class.getSimpleName();

    @NonNull
    private static final List<HSAppInitializeListener> listeners =
            new CopyOnWriteArrayList<>();

    @SuppressLint("StaticFieldLeak")
    @Nullable
    private static HSAppInitializer initializer;
    private static boolean isInitialized = false;

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void initialize(@NonNull Context context, @NonNull HSAppConfig config) {
        initialize(context, config, null);
    }

    public static void initialize(@NonNull Context context,
                                  @NonNull HSAppConfig config,
                                  @Nullable final HSAppInitializeListener listener) {
        if (initializer == null) {
            final Context targetContext = context.getApplicationContext();
            final HSAppInitializeListener listenerDelegate = new HSAppInitializeListener() {
                @Override
                public void onAppInitialized() {
                    HSLogger.logInfo(TAG, "onAppInitialized");
                    isInitialized = true;
                    initializer = null;
                    if (listener != null) {
                        listener.onAppInitialized();
                    }
                    notifyInitialized();
                }

                @Override
                public void onAppInitializationFailed(@NonNull HSError error) {
                    HSLogger.logInfo(TAG, "onAppInitializationFailed: " + error.toString());
                    isInitialized = false;
                    initializer = null;
                    if (listener != null) {
                        listener.onAppInitializationFailed(error);
                    }
                    notifyInitializationFailed(error);
                }
            };
            initializer = new HSAppInitializer(targetContext, config, listenerDelegate);
            initializer.start();
        }
    }

    public static void addInitializeListener(@NonNull HSAppInitializeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public static void removeInitializeListener(@NonNull HSAppInitializeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private static void notifyInitialized() {
        final List<HSAppInitializeListener> targetListeners = new ArrayList<>(listeners);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (HSAppInitializeListener listener : targetListeners) {
                    listener.onAppInitialized();
                }
            }
        });
    }

    private static void notifyInitializationFailed(@NonNull final HSError error) {
        final List<HSAppInitializeListener> targetListeners = new ArrayList<>(listeners);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (HSAppInitializeListener listener : targetListeners) {
                    listener.onAppInitializationFailed(error);
                }
            }
        });
    }

    private static class HSAppInitializer extends Thread {

        private static final Executor executor = Executors.newCachedThreadPool();

        @NonNull
        private Context context;
        @NonNull
        private HSAppConfig appConfig;
        @NonNull
        private HSAppInitializeListener listener;

        public HSAppInitializer(@NonNull Context context,
                                @NonNull HSAppConfig appConfig,
                                @NonNull HSAppInitializeListener listener) {
            this.context = context;
            this.appConfig = appConfig;
            this.listener = listener;
        }

        @Override
        public void run() {
            final List<HSService> services = appConfig.getServices();
            final List<HSConnector> connectors = appConfig.getConnectors();

            if (isListNullOrEmpty(services)) {
                listener.onAppInitializationFailed(HSError.NoServices);
                return;
            }
            if (isListNullOrEmpty(connectors)) {
                listener.onAppInitializationFailed(HSError.NoConnectors);
                return;
            }

            final CountDownLatch latch = new CountDownLatch(services.size());
            final HSAppParamsImpl appParamsHolder = new HSAppParamsImpl(appConfig);
            final HSConnectorDelegate connectorDelegate = new HSConnectorDelegate(connectors);
            final HSServiceCallback serviceCallback = new HSServiceCallback() {
                @Override
                public void onFinished() {
                    latch.countDown();
                }

                @Override
                public void onFail(@NonNull HSError error) {
                    latch.countDown();
                }
            };
            for (final HSService service : services) {
                HSServiceInitializer serviceInitializer = new HSServiceInitializer(
                        context, service, appParamsHolder, serviceCallback, connectorDelegate);
                executor.execute(serviceInitializer);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listener.onAppInitialized();
        }

        private <T> boolean isListNullOrEmpty(@Nullable List<T> list) {
            return list == null || list.isEmpty();
        }
    }

    private static final class HSServiceInitializer implements Runnable {

        @NonNull
        private Context context;
        @NonNull
        private HSService service;
        @NonNull
        private HSAppParams appParams;
        @NonNull
        private HSServiceCallback serviceCallback;
        @NonNull
        private HSConnectorCallback connectorCallback;

        private boolean isFinished = false;

        public HSServiceInitializer(@NonNull Context context,
                                    @NonNull HSService service,
                                    @NonNull HSAppParams appParams,
                                    @NonNull HSServiceCallback serviceCallback,
                                    @NonNull HSConnectorCallback connectorCallback) {
            this.context = context;
            this.service = service;
            this.appParams = appParams;
            this.serviceCallback = serviceCallback;
            this.connectorCallback = connectorCallback;
        }

        @Override
        public void run() {
            HSLogger.logInfo(service.getName(), "Version: " + service.getVersion());
            HSLogger.logInfo(service.getName(), "Initialization start");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isFinished) {
                        HSLogger.logInfo(service.getName(), "Initialization timeout");
                        serviceCallback.onFail(HSError.forService(service, "Timeout"));
                        isFinished = true;
                    }
                }
            }, appParams.getServiceInitializeTimeoutMs());
            service.start(context, appParams, new HSServiceCallback() {
                @Override
                public void onFinished() {
                    HSLogger.logInfo(service.getName(), "Initialization finished");
                    if (!isFinished) {
                        serviceCallback.onFinished();
                        isFinished = true;
                    }
                }

                @Override
                public void onFail(@NonNull HSError error) {
                    HSLogger.logInfo(service.getName(), "Initialization fail: " + error);
                    if (!isFinished) {
                        serviceCallback.onFinished();
                    }
                }
            }, connectorCallback);
        }
    }

}
