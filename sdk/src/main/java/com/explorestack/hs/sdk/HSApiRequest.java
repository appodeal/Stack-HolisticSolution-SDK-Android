package com.explorestack.hs.sdk;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.hs.sdk.HSAdvertisingInfo.AdvertisingProfile;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

class HSApiRequest<RequestDataType, ResponseDataType> extends HSNetworkRequest<RequestDataType, ResponseDataType, HSError> {

    private static final int REQUEST_TIMEOUT = 10 * 1000;
    private String requiredUrl;
    private int timeOut;

    private HSApiRequest(@NonNull Method method,
                         @Nullable RequestDataType requestData) {
        super(method, null, requestData);
        addContentEncoder(new GZIPRequestDataEncoder<RequestDataType, ResponseDataType, HSError>());
    }

    @Override
    protected HSError obtainError(@NonNull URLConnection connection,
                                  @Nullable ResponseDataType responseType,
                                  int responseCode) {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return null;
        }
        return getErrorFromCode(connection, responseCode);
    }

    @Override
    protected HSError obtainError(URLConnection connection,
                                  @Nullable InputStream errorStream,
                                  int responseCode) {
//        Logger.log("Request error (" + responseCode + "), headers:", connection.getHeaderFields());
//        final String errorReason = connection.getHeaderField("ad-exchange-error-reason");
//        final String errorMessage = connection.getHeaderField("ad-exchange-error-message");
//        return !TextUtils.isEmpty(errorMessage) && !TextUtils.isEmpty(errorReason)
//                ? HSError.requestError(String.format("%s - %s", errorReason, errorMessage))
//                : !TextUtils.isEmpty(errorMessage) ? HSError.requestError(errorMessage)
//                : !TextUtils.isEmpty(errorReason) ? HSError.requestError(errorReason)
//                : getErrorFromCode(connection, responseCode);
        return null;
    }

    @Override
    protected HSError obtainError(URLConnection connection, @Nullable Throwable t) {
//        Logger.log("obtainError: " + t + "(" + connection + ")");
//        //TODO: not checked
//        if (t instanceof UnknownHostException) {
//            return HSError.Connection;
//        } else if (t instanceof SocketTimeoutException || t instanceof ConnectTimeoutException) {
//            return HSError.TimeoutError;
//        }
//        return HSError.Internal;
        return null;
    }

    @Override
    protected String getBaseUrl() {
        return requiredUrl;
    }

    @Override
    protected void prepareRequestParams(URLConnection connection) {
        super.prepareRequestParams(connection);
        connection.setConnectTimeout(timeOut);
        connection.setReadTimeout(timeOut);
    }

    private HSError getErrorFromCode(URLConnection connection, int responseCode) {
//        if (responseCode >= 200 && responseCode < 300) {
//            return HSError.NoContent;
//        } else if (responseCode >= 400 && responseCode < 500) {
//            return HSError.requestError(String.valueOf(responseCode));
//        } else if (responseCode >= 500 && responseCode < 600) {
//            return HSError.Server;
//        }
//        return HSError.Internal;
        return null;
    }

    public static class Builder<RequestDataType, ResponseDataType> {

        private String url;
        private RequestDataType requestData;
        private int timeOut = REQUEST_TIMEOUT;
        private HSNetworkRequest.RequestDataBinder<RequestDataType, ResponseDataType, HSError> dataBinder;
        private HSNetworkRequest.Callback<ResponseDataType, HSError> callback;
        private HSNetworkRequest.CancelCallback cancelCallback;

        private Method method = Method.Post;

        public Builder<RequestDataType, ResponseDataType> setUrl(@NonNull String url) {
            this.url = url;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setDataBinder(
                HSNetworkRequest.RequestDataBinder<RequestDataType, ResponseDataType, HSError> dataBinder
        ) {
            this.dataBinder = dataBinder;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setRequestData(RequestDataType requestData) {
            this.requestData = requestData;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setLoadingTimeOut(int timeOut) {
            this.timeOut = timeOut > 0 ? timeOut : REQUEST_TIMEOUT;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setCallback(
                HSNetworkRequest.Callback<ResponseDataType, HSError> callback
        ) {
            this.callback = callback;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setCancelCallback(
                HSNetworkRequest.CancelCallback cancelCallback
        ) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setMethod(@NonNull Method method) {
            this.method = method;
            return this;
        }

        public HSApiRequest<RequestDataType, ResponseDataType> build() {
            HSApiRequest<RequestDataType, ResponseDataType> request = new HSApiRequest<>(method, requestData);
            request.setCallback(callback);
            request.setCancelCallback(cancelCallback);
            request.setDataBinder(dataBinder);
            request.requiredUrl = url;
            request.timeOut = timeOut;
            return request;
        }

        public HSApiRequest<RequestDataType, ResponseDataType> request() {
            HSApiRequest<RequestDataType, ResponseDataType> request = build();
            request.request();
            return request;
        }
    }

    static void initRequest(@NonNull Context context,
                            @NonNull HSAppParams appParams,
                            @NonNull Callback<JSONObject, HSError> callback) {
        new HSApiRequest.Builder<JSONObject, JSONObject>()
                .setUrl("http://herokuapp.appodeal.com/android_hs_init")
                .setMethod(HSNetworkRequest.Method.Post)
                .setDataBinder(new HSApiRequest.InitRequestDataBinder(context, appParams))
                .setCallback(callback)
                .request();
    }

    private static final class InitRequestDataBinder extends SimpleJsonObjectDataBinder<HSError> {

        @NonNull
        private static final List<RequestDataBinder> defaultDataBinders = new ArrayList<>();

        static {
            defaultDataBinders.add(new BaseDataBinder());
            defaultDataBinders.add(new ComponentsDataBinder());
        }

        @NonNull
        private final Context context;
        @NonNull
        private final HSAppParams appParams;

        public InitRequestDataBinder(@NonNull Context context,
                                     @NonNull HSAppParams app) {
            this.context = context;
            this.appParams = app;
        }

        @Nullable
        @Override
        protected byte[] obtainData(HSNetworkRequest<JSONObject, JSONObject, HSError> request,
                                    URLConnection connection,
                                    @Nullable JSONObject requestData) throws Exception {
            requestData = requestData == null ? new JSONObject() : requestData;
            return super.obtainData(request, connection, buildRequestData(requestData));
        }

        private JSONObject buildRequestData(@NonNull JSONObject requestData) throws Exception {
            for (RequestDataBinder binder : defaultDataBinders) {
                binder.bind(context, appParams, requestData);
            }
            return requestData;
        }
    }

    private interface RequestDataBinder {
        void bind(@NonNull Context context, @NonNull HSAppParams appParams, @NonNull JSONObject target) throws Exception;
    }

    private static final class BaseDataBinder implements RequestDataBinder {

        @Override
        public void bind(@NonNull Context context,
                         @NonNull HSAppParams appParams,
                         @NonNull JSONObject target) throws Exception {
            HSAppInstance app = HSAppInstance.get();
            target.put("sdk_version", app.getVersion());
            target.put("track_id", app.getTrackId());
            target.put("app_key", appParams.getAppKey());

            String packageName = context.getPackageName();
            target.put("package", packageName);
            try {
                String installer = context.getPackageManager().getInstallerPackageName(packageName);
                target.put("installer", TextUtils.isEmpty(installer) ? "unknown" : installer);
            } catch (Exception ignored) {
                // ignore
            }

            HSConnectionData connectionData = HSCoreUtils.getConnectionData(context);
            target.put("connection", connectionData.type);

            Calendar calendarGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
            String localTime = new SimpleDateFormat("Z", Locale.ENGLISH).format(calendarGMT.getTime());
            target.put("timezone", localTime);
            target.put("local_time", System.currentTimeMillis() / 1000);

            AdvertisingProfile advertisingProfile = HSAdvertisingInfo.updateInfo(context);
            if (advertisingProfile != null) {
                target.put("ifa", advertisingProfile.getId(context));
                target.put("advertising_tracking", advertisingProfile.isLimitAdTrackingEnabled() ? "0" : "1");
            }

            target.put("http_allowed", HSCoreUtils.hasUsesCleartextTraffic());
            target.put("manufacturer", Build.MANUFACTURER);
            target.put("model", Build.MODEL);
            target.put("osv", Build.VERSION.RELEASE);
            target.put("os", "Android");
            target.put("locale", Locale.getDefault().toString());
            target.put("device_type", HSCoreUtils.isTablet(context) ? "tablet" : "phone");
            target.put("user_agent", HSCoreUtils.getHttpAgentString(context));
        }
    }

    private static final class ComponentsDataBinder implements RequestDataBinder {
        @Override
        public void bind(@NonNull Context context,
                         @NonNull HSAppParams appParams,
                         @NonNull JSONObject target) throws Exception {
            Map<String, HSComponentAssetParams> services = HSComponentAssetManager.getServices();
            JSONObject servicesJson = new JSONObject();
            for (HSComponentAssetParams assetParams : services.values()) {
                JSONObject componentJson = new JSONObject();
                componentJson.put("ver", assetParams.getVersion());
                componentJson.put("sdk", assetParams.getVersion());
                servicesJson.put(assetParams.getName(), componentJson);
            }
            target.put("services", servicesJson);
        }
    }
}