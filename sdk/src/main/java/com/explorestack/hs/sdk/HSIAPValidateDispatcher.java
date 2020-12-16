package com.explorestack.hs.sdk;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HSIAPValidateDispatcher {

    private static final Executor validateExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private final HSAppInstance app;
    @NonNull
    private final Map<HSComponent, HSIAPValidateHandler> handlers = new HashMap<>();
    @Nullable
    private List<Pair<HSInAppPurchase, HSInAppPurchaseValidateListener>> pendingPurchase;

    public HSIAPValidateDispatcher(@NonNull HSAppInstance app) {
        this.app = app;
    }

    void dispatchPendingPurchase() {
        if (pendingPurchase != null) {
            for (Pair<HSInAppPurchase, HSInAppPurchaseValidateListener> entry : pendingPurchase) {
                validateInAppPurchase(entry.first, entry.second);
            }
            pendingPurchase = null;
        }
    }

    public void validateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                      @Nullable HSInAppPurchaseValidateListener listener) {
        if (app.isInitialized()) {
            validateExecutor.execute(new HSIAPValidateTask(this, purchase, listener));
        } else {
            if (pendingPurchase == null) {
                pendingPurchase = new ArrayList<>();
            }
            pendingPurchase.add(Pair.create(purchase, listener));
        }
    }

    public void addHandler(@NonNull HSComponent component,
                           @Nullable HSIAPValidateHandler callback) {
        if (callback != null) {
            handlers.put(component, callback);
        }
    }

    public void removeHandler(@NonNull HSComponent component) {
        handlers.remove(component);
    }

    private void onInAppPurchaseValidateSuccess(@NonNull HSInAppPurchase purchase,
                                                @Nullable List<HSError> errors) {
        HSConnectorDelegate connectorDelegate = app.getConnectorDelegate();
        if (connectorDelegate != null) {
            connectorDelegate.trackInApp(app.getContext(), purchase);
        }
    }

    private static final class HSIAPValidateTask implements Runnable {

        private static final Executor executor = Executors.newCachedThreadPool();

        @NonNull
        private final HSIAPValidateDispatcher dispatcher;
        @NonNull
        private final HSInAppPurchase purchase;
        @Nullable
        private final HSInAppPurchaseValidateListener listener;

        public HSIAPValidateTask(@NonNull HSIAPValidateDispatcher dispatcher,
                                 @NonNull HSInAppPurchase purchase,
                                 @Nullable HSInAppPurchaseValidateListener listener) {
            this.dispatcher = dispatcher;
            this.purchase = purchase;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (dispatcher.handlers.isEmpty()) {
                if (listener != null) {
                    listener.onInAppPurchaseValidateFail(Collections.singletonList(HSError.NoIAPValidateHandlers));
                }
                return;
            }
            final int handlersCount = dispatcher.handlers.size();
            final List<HSError> errors = new ArrayList<>();
            final CountDownLatch waiter = new CountDownLatch(handlersCount);
            final HSIAPValidateCallback validateCallback = new HSIAPValidateCallback() {
                @Override
                public void onSuccess() {
                    waiter.countDown();
                }

                @Override
                public void onFail(@NonNull HSError error) {
                    errors.add(error);
                    waiter.countDown();
                }
            };
            for (Map.Entry<HSComponent, HSIAPValidateHandler> entry : dispatcher.handlers.entrySet()) {
                startValidate(entry.getKey(), entry.getValue(), validateCallback);
            }
            try {
                waiter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (listener != null) {
                if (errors.size() == handlersCount) {
                    listener.onInAppPurchaseValidateFail(errors);
                } else {
                    dispatcher.onInAppPurchaseValidateSuccess(purchase, errors);
                    listener.onInAppPurchaseValidateSuccess(purchase, errors);
                }
            }
        }

        private void startValidate(@NonNull final HSComponent component,
                                   @NonNull final HSIAPValidateHandler handler,
                                   @NonNull final HSIAPValidateCallback callback) {
            final AtomicBoolean isFinished = new AtomicBoolean(false);
            HSUtils.startTimeout(TimeUnit.MINUTES.toMillis(1), new TimerTask() {
                @Override
                public void run() {
                    if (!isFinished.get()) {
                        HSLogger.logInfo(component.getName(), "IAP validation timeout");
                        callback.onFail(HSError.NoIAPValidateTimeout);
                        isFinished.set(true);
                    }
                }
            });
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onValidateInAppPurchase(purchase, new HSIAPValidateCallback() {
                        @Override
                        public void onSuccess() {
                            isFinished.set(true);
                            callback.onSuccess();
                        }

                        @Override
                        public void onFail(@NonNull HSError error) {
                            isFinished.set(true);
                            callback.onFail(error);
                        }
                    });
                }
            });
        }
    }
}
