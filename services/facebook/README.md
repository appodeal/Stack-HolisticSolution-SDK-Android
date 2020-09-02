# Facebook Service for Holistic Solution SDK

> Note that HS Facebook Service will include only 'facebook-core' dependency independently

## Configure Your Facebook App

Please follow [Step 1](https://developers.facebook.com/docs/app-events/getting-started-app-events-android#step-1--configure-your-facebook-app) and [Step 2](https://developers.facebook.com/docs/app-events/getting-started-app-events-android#step-2--link-your-facebook-ad-account-with-your-app) from this [guide](https://developers.facebook.com/docs/app-events/getting-started-app-events-android#step-1--configure-your-facebook-app) for configure you Facebook app

## Add Your Facebook App ID

> You can find more info about Facebook integrations in this [guide](https://developers.facebook.com/docs/app-events/getting-started-app-events-android)

Open your `/app/res/values/strings.xml` file and add the following lines (remember to replace `[APP_ID]` with your actual Facebook app ID):

```xml
<string name="facebook_app_id">[APP_ID]</string>
```

Add a `meta-data` element to the application element:

```xml
<application android:label="@string/app_name" ...>
    ...
    <meta-data
        android:name="com.facebook.sdk.ApplicationId"
        android:value="@string/facebook_app_id"/>
    ...
</application>
```

> If you use Facebook Unity SDK and configure it via UI, you probably can skip the last step, since Facebook Unity SDK automatically add required meta-data to AndroidManifest.xml

## Import Service

```groovy
dependencies {
    // ... other project dependencies

    implementation 'com.explorestack.hs.sdk.service:facebook:1.0.1'
}
```

## Register Service

```java
public class MainActivity extends AppCompatActivity {
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create service for AppsFlyer
        HSFacebookService facebookService = new HSFacebookService();
    
        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                //Include AppsFlyer service to HSApp config
                .withServices(facebookService, ...);        
    }
    ...
}
```

#### Service methods

| Method          | Description                                                                   |
|-----------------|-------------------------------------------------------------------------------|
| setEventsLogger | Sets custom Facebook `AppEventsLogger` which will be used for dispatch events |