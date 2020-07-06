# AppsFlyer Service for Holistic Solution SDK

>Service will independently add all the necessary dependencies and initialize the AppsFlyer SDK. 
However, if necessary, you can find information about AppsFlyer SDK integration at this [link](https://support.appsflyer.com/hc/en-us/articles/207032126-Android-SDK-integration-for-developers#integration)

## Integrate AppFlyer

Please, follow this [guide](https://support.appsflyer.com/hc/en-us/articles/207033486-Getting-started-step-by-step#basic-attribution) for register and create app in AppsFlyer

## Retrieve AppsFlyer SDK dev key

Please, visit this [link](https://support.appsflyer.com/hc/en-us/articles/207032126-Android-SDK-integration-for-developers#integration-31-retrieving-your-dev-key) to get more info about retrieving AppsFlyer dev key

## Import Service

```groovy
dependencies {
    // ... other project dependencies

    implementation 'com.explorestack.hs.sdk.service:appsflyer:1.0.0'
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
        HSAppsflyerService appsflyerService = new HSAppsflyerService(YOUR_APPSFLYER_DEV_KEY);
    
        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                //Include AppsFlyer service to HSApp config
                .withServices(appsflyerService, ...);        
    }
    ...
}
```

#### Service parameters

| Parameter      | Required | Description                                                                                                                                                                                                      |
|----------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| devKey         | Yes      | AppsFlyer dev key. You can find more info about it's retrieving [here](https://support.appsflyer.com/hc/en-us/articles/207032126-Android-SDK-integration-for-developers#integration-31-retrieving-your-dev-key)  |
| conversionKeys | No       | Keys, which should be collected from AppsFlyer 'onConversionDataSuccess' response. If it's not provided or it's empty, all keys will be collected                                                                |

#### Service methods

| Method                         | Description                                                                                                                 |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| setAppsFlyerConversionListener | Sets custom AppsFlyer `AppsFlyerConversionListener` which will be called when service receives appropriate AppsFlyer events |

## Known issues

#### Issue with 'fullBackupContent' merging

If you add android:fullBackupContent="true" inside the <application> tag in the AndroidManifest.xml and you use Appodeal SDK version with Vungle SDK, you might get the error:

```groovy
Manifest merger failed : Attribute application@fullBackupContent value=(@xml/vungle_backup_rule) from [com.vungle:publisher-sdk-android:6.4.11] AndroidManifest.xml:19:9-60
	is also present at [com.appsflyer:af-android-sdk:5.2.0] AndroidManifest.xml:14:18-73 value=(@xml/appsflyer_backup_rules).
	Suggestion: add 'tools:replace="android:fullBackupContent"' to <application> element at AndroidManifest.xml:9:5-25:19 to override.
```

It's caused because of failing merging manifests from Vungle SDK and AppsFlyer SDK. To fix this issue you should provide you own merged backup rules:

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    ...
    <!-- Required for AppsFlyer-->
    <exclude domain="sharedpref" path="appsflyer-data"/>

    <!-- Required for Vungle SDK -->
    <exclude domain="file" path="vungle" />
    <exclude domain="file" path="vungle_cache" />
    <exclude domain="external" path="vungle_cache" />
    <exclude domain="database" path="vungle_db" />
    <exclude domain="sharedpref" path="com.vungle.sdk.xml" />
</full-backup-content>
```

More info about this issue on [AppsFlyer SDK page](https://support.appsflyer.com/hc/en-us/articles/207032126-Android-SDK-integration-for-developers#integration-known-issues-with-integrating-the-sdk)