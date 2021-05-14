package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.explorestack.hs.sdk.HSCoreUtils.close;
import static com.explorestack.hs.sdk.HSCoreUtils.flush;

abstract class HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> {

    private static final Executor executor = Executors.newFixedThreadPool(2);

    public enum Method {

        Get("GET"),
        Post("POST");

        private final String methodString;

        Method(@NonNull String methodString) {
            this.methodString = methodString;
        }

        public void apply(URLConnection connection) throws ProtocolException {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod(methodString);
            }
        }

    }

    public enum State {
        Idle, Running, Success, Fail, Canceled
    }

    @NonNull
    private final Method method;
    @Nullable
    private final String path;
    @Nullable
    private final RequestDataType requestData;

    @Nullable
    private ResponseDataType requestResult;
    @Nullable
    private ErrorResultType errorResult;
    @Nullable
    private URLConnection currentConnection;

    @Nullable
    private RequestDataBinder<RequestDataType, ResponseDataType, ErrorResultType> dataBinder;
    @Nullable
    private List<RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType>> dataEncoders;
    @Nullable
    private List<RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType>> contentEncoders;
    @Nullable
    private Callback<ResponseDataType, ErrorResultType> callback;
    @Nullable
    private CancelCallback cancelCallback;

    private State currentState = State.Idle;

    public HSNetworkRequest(@NonNull Method method,
                            @Nullable String path,
                            @Nullable RequestDataType requestData) {
        this.method = method;
        this.path = path;
        this.requestData = requestData;
    }

    public void setDataBinder(@Nullable RequestDataBinder<RequestDataType, ResponseDataType, ErrorResultType> dataBinder) {
        this.dataBinder = dataBinder;
    }

    public void addDataEncoder(RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> encoder) {
        if (dataEncoders == null) {
            dataEncoders = new ArrayList<>();
        }
        dataEncoders.add(encoder);
    }

    public void addContentEncoder(RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> encoder) {
        if (contentEncoders == null) {
            contentEncoders = new ArrayList<>();
        }
        contentEncoders.add(encoder);
    }

    public void setCallback(@Nullable Callback<ResponseDataType, ErrorResultType> callback) {
        this.callback = callback;
    }

    public void setCancelCallback(@Nullable CancelCallback cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    @NonNull
    public Method getMethod() {
        return method;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void request() {
        executor.execute(new HSNetworkRequestRunner());
    }

    private void process() {
        currentState = State.Running;

        URLConnection connection = null;
        try {
            URL url = path != null
                    ? new URL(String.format("%s/%s", getBaseUrl(), path))
                    : new URL(getBaseUrl());
            currentConnection = connection = url.openConnection();

            method.apply(connection);
            prepareRequestParams(connection);

            byte[] contentBytes = obtainRequestData(connection);

            if (contentBytes != null) {
                contentBytes = encodeRequestData(connection, contentBytes);

                connection.setDoOutput(true);
                BufferedOutputStream writer = null;
                try {
                    writer = new BufferedOutputStream(connection.getOutputStream());
                    writer.write(contentBytes);
                } finally {
                    flush(writer);
                    close(writer);
                }
            }

            InputStream isResponse = null;
            ByteArrayOutputStream osBytes = null;

            try {
                int responseCode = obtainResponseCode(connection);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    errorResult = obtainError(connection,
                            obtainErrorStream(connection),
                            responseCode);
                } else {
                    isResponse = connection.getInputStream();
                    osBytes = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = isResponse.read(buffer)) != -1) {
                        osBytes.write(buffer, 0, length);
                    }
                    byte[] responseBytes = osBytes.toByteArray();
                    if (responseBytes != null) {
                        responseBytes = decodeResponseData(connection, responseBytes);
                    }
                    if (responseBytes == null || responseBytes.length == 0) {
                        errorResult = obtainError(connection,
                                (ResponseDataType) null,
                                responseCode);
                    } else if (dataBinder != null) {
                        requestResult = dataBinder.createSuccessResult(this,
                                connection,
                                responseBytes);
                        if (requestResult == null) {
                            errorResult = dataBinder.createFailResult(this,
                                    connection,
                                    responseBytes);
                        }
                    }
                }
            } finally {
                flush(osBytes);
                close(osBytes);

                close(isResponse);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            errorResult = obtainError(connection, t);
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
            currentConnection = null;
            if (!isCanceled()) {
                currentState = errorResult == null ? State.Success : State.Fail;
            }
        }
    }

    protected void prepareRequestParams(URLConnection connection) {
        connection.setConnectTimeout(40 * 1000);
        connection.setReadTimeout(40 * 1000);
    }

    protected byte[] obtainRequestData(URLConnection connection) throws Exception {
        if (dataBinder != null) {
            dataBinder.prepareRequest(this, connection);
            dataBinder.prepareHeaders(this, connection);
            return dataBinder.obtainData(this, connection, requestData);
        }
        return null;
    }

    protected byte[] encodeRequestData(URLConnection connection,
                                       byte[] requestData) throws Exception {
        byte[] result = requestData;
        if (dataEncoders != null) {
            for (RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> encoder
                    : dataEncoders) {
                encoder.prepareHeaders(this, connection);
                result = encoder.encode(this, connection, result);
            }
        }
        if (contentEncoders != null) {
            for (RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> encoder
                    : contentEncoders) {
                encoder.prepareHeaders(this, connection);
                result = encoder.encode(this, connection, result);
            }
        }
        return result;
    }

    protected byte[] decodeResponseData(URLConnection connection,
                                        byte[] responseData) throws Exception {
        byte[] result = responseData;
        if (contentEncoders != null) {
            for (RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> encoder
                    : contentEncoders) {
                result = encoder.decode(this, connection, result);
            }
        }
        if (dataEncoders != null) {
            for (RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> encoder
                    : dataEncoders) {
                result = encoder.decode(this, connection, result);
            }
        }
        return result;
    }

    protected abstract ErrorResultType obtainError(@NonNull URLConnection connection,
                                                   @Nullable ResponseDataType resultType,
                                                   int responseCode);

    protected abstract ErrorResultType obtainError(URLConnection connection,
                                                   @Nullable InputStream errorStream,
                                                   int responseCode);

    protected abstract ErrorResultType obtainError(URLConnection connection, @Nullable Throwable t);

    private int obtainResponseCode(URLConnection connection) throws IOException {
        if (connection instanceof HttpURLConnection) {
            return ((HttpURLConnection) connection).getResponseCode();
        }
        return -1;
    }

    private InputStream obtainErrorStream(URLConnection connection) {
        return connection instanceof HttpURLConnection
                ? ((HttpURLConnection) connection).getErrorStream()
                : null;
    }

    protected String getBaseUrl() throws Exception {
        return "TODO: implement url";
    }

    public void cancel() {
        currentState = State.Canceled;
        if (currentConnection instanceof HttpURLConnection) {
            ((HttpURLConnection) currentConnection).disconnect();
        }
        if (cancelCallback != null) {
            cancelCallback.onCanceled();
        }
    }

    public boolean isCanceled() {
        return currentState == State.Canceled;
    }


    public interface Callback<ResponseDataType, ErrorResultType> {

        void onSuccess(@Nullable ResponseDataType result);

        void onFail(@Nullable ErrorResultType result);

    }

    public interface CancelCallback {

        void onCanceled();

    }
    
    /*
    Request data/params binders
     */

    public static abstract class RequestDataBinder<RequestDataType, ResponseDataType, ErrorResultType> {

        protected void prepareRequest(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                      URLConnection connection) {

        }

        protected abstract void prepareHeaders(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                               URLConnection connection);

        @Nullable
        protected abstract byte[] obtainData(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                             URLConnection connection,
                                             @Nullable RequestDataType requestData) throws Exception;

        protected abstract ResponseDataType createSuccessResult(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                                                URLConnection connection,
                                                                byte[] resultData) throws Exception;

        protected ErrorResultType createFailResult(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                                   URLConnection connection,
                                                   byte[] resultData) throws Exception {
            return null;
        }

    }

    abstract static class JsonDataBinder<ResponseDataType, ErrorResultType>
            extends RequestDataBinder<JSONObject, ResponseDataType, ErrorResultType> {

        @Override
        protected void prepareHeaders(HSNetworkRequest<JSONObject, ResponseDataType, ErrorResultType> request,
                                      URLConnection connection) {
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }

        @Nullable
        @Override
        protected byte[] obtainData(HSNetworkRequest<JSONObject, ResponseDataType, ErrorResultType> request,
                                    URLConnection connection,
                                    @Nullable JSONObject requestData) throws Exception {
            return requestData != null
                    ? requestData.toString().getBytes("UTF-8")
                    : null;
        }

    }

    public static class SimpleJsonObjectDataBinder<ErrorResultType> extends JsonDataBinder<JSONObject, ErrorResultType> {

        @Override
        protected JSONObject createSuccessResult(HSNetworkRequest<JSONObject, JSONObject, ErrorResultType> request,
                                                 URLConnection connection,
                                                 byte[] resultData) throws Exception {
            return new JSONObject(new String(resultData));
        }

    }

    /*
    Encoders
     */

    public static abstract class RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> {

        protected void prepareHeaders(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                      URLConnection connection) {

        }

        protected abstract byte[] encode(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                         URLConnection connection, byte[] data) throws Exception;

        protected abstract byte[] decode(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                         URLConnection connection, byte[] data) throws Exception;

    }

    public static class GZIPRequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType>
            extends RequestDataEncoder<RequestDataType, ResponseDataType, ErrorResultType> {

        @Override
        protected void prepareHeaders(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                      URLConnection connection) {
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Content-Encoding", "gzip");
        }

        @Override
        protected byte[] encode(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                URLConnection connection, byte[] data) throws Exception {
            ByteArrayOutputStream osBytes = null;
            GZIPOutputStream osGzip = null;
            try {
                osBytes = new ByteArrayOutputStream();
                osGzip = new GZIPOutputStream(osBytes);
                osGzip.write(data);
                // required for write all pending bytes
                close(osGzip);
                osGzip = null;
                return osBytes.toByteArray();
            } finally {
                flush(osBytes);
                close(osBytes);

                flush(osGzip);
                close(osGzip);
            }
        }

        @Override
        protected byte[] decode(HSNetworkRequest<RequestDataType, ResponseDataType, ErrorResultType> request,
                                URLConnection connection, byte[] data) throws Exception {
            if ("gzip".equals(connection.getContentEncoding())) {
                ByteArrayOutputStream osBytes = null;
                ByteArrayInputStream isBytes = null;
                GZIPInputStream isGzip = null;
                try {
                    osBytes = new ByteArrayOutputStream();
                    isBytes = new ByteArrayInputStream(data);
                    isGzip = new GZIPInputStream(isBytes);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = isGzip.read(buffer)) != -1) {
                        osBytes.write(buffer, 0, bytesRead);
                    }
                    return osBytes.toByteArray();
                } finally {
                    flush(osBytes);
                    close(osBytes);

                    close(isBytes);
                    close(isGzip);
                }
            }
            return data;
        }

    }

    private final class HSNetworkRequestRunner implements Runnable {

        @Override
        public void run() {
            process();
            if (callback != null && !isCanceled()) {
                if (currentState == State.Success) {
                    callback.onSuccess(requestResult);
                } else {
                    callback.onFail(errorResult);
                }
            }
        }
    }
}