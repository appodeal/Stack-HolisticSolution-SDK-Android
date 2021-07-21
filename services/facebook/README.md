# Facebook Service for Holistic Solution SDK

> Note that HS Facebook Service will include only 'facebook-core' dependency independently

## 1. Configure Your Facebook App

Please follow this [guide](https://developers.facebook.com/docs/app-events/getting-started-app-events-android) to configure you Facebook app

## 2. Add Your Facebook App ID

> You can find more info about Facebook integrations in this [guide](https://developers.facebook.com/docs/app-events/getting-started-app-events-android)

Open your `/app/res/values/strings.xml` file and add the following lines (remember to replace `[APP_ID]` with your actual Facebook app ID):

```xml
<string name="facebook_app_id">[APP_ID]</string>
```

Add a `meta-data` element to the application element:

```xml
<application ...>
    ...
    <meta-data
        android:name="com.facebook.sdk.ApplicationId"
        android:value="@string/facebook_app_id"/>
    ...
</application>
```

> If you use Facebook Unity SDK and configure it via UI, you probably can skip the last step, since Facebook Unity SDK automatically add required meta-data to AndroidManifest.xml