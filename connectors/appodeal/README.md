# Appodeal SDK Connector for Holistic Solution SDK

## Integrate Appodeal SDK

Please, follow this [guide](https://wiki.appodeal.com/display/DE/Android+SDK.+Integration+Guide) for integrating Appodeal SDK to your app

## Import Connector

```groovy
dependencies {
    // ... other project dependencies

    implementation 'com.explorestack.hs.sdk.connector:appodeal:1.0.0'
}
```

## Register Connector

```java
public class MainActivity extends AppCompatActivity {
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create connector for Appodeal
        HSAppodealConnector appodealConnector = new HSAppodealConnector();
    
        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                //Include Appodeal connector to HSApp config
                .withConnectors(appodealConnector);
    }
    ...
}
```

[appodeal_sdk_initialization]: appodeal_sdk_initialization
## Appodeal SDK Initialization

First, make sure you've read how to configure and initialize [HSApp](../../README.md#initialize_sdk)

```java
public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = ExampleActivity.class.getSimpleName();

    private final HSAppInitializeListener hsAppInitializeListener = new HSAppInitializeListener() {
        @Override
        public void onAppInitialized() {
            // HSApp was successfully initialized and now you can initialize Appodeal SDK
            initializeAppodeal(ExampleActivity.this);
        }

        @Override
        public void onAppInitializationFailed(@NonNull HSError error) {
            // HSApp initialization failed, more info can be found in 'error' object
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check, if HSApp is initialized. If 'yes', then you can initialize appropriate SDK,
        // otherwise - subscribe to listen to the initialization state
        if (HSApp.isInitialized()) {
            // HSApp was successfully initialized and now you can initialize required SDK
            initializeAppodeal(this);
        } else {
            // Attach HSApp initialization listener
            HSApp.addInitializeListener(hsAppInitializeListener);
            // Start HSApp initialization if it's not started yet
            ExampleApplication.initializeHSApp(getApplicationContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove HSApp initialize listener, since HSApp store strong reference to provided listener
        HSApp.removeInitializeListener(hsAppInitializeListener);
    }

    /**
     * Storing and processing Appodeal SDK initialization
     */
    private static boolean isAppodealInitialized = false;

    private static void initializeAppodeal(@NonNull Activity activity) {
        if (!isAppodealInitialized) {
            Appodeal.initialize(activity, YOUR_APPODEAL_KEY, REQUIRED_ADS_TYPES);
            isAppodealInitialized = true;
        }
    }
}
```

[Code example](../../example/src/main/java/com/explorestack/hs/sdk/example/ExampleActivity.java#L19)
