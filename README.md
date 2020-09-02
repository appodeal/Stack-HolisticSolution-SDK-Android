# About

Stack Holistic Solution SDK for Android simplifies the collection and transfer of the necessary parameters from third-party services to the corresponding Stack SDKs to improve the performance of services such as Mediation and UA

## Table of contents

* [Import SDK](#import-sdk)
* [Initialize SDK](#initialize-sdk)
* [Events](#events)
* [In-App purchase validation](#in-app-purchase-validation)
* [Available Services](services/README.md)
* [Available Connectors](connectors/README.md)
  
## Import SDK

#### Add the Appodeal maven repository

Apps can import the HS SDK with a Gradle dependency that points to the Appodeal's Maven repository. In order to use that repository, you need to reference it in the app's project-level build.gradle file. Open yours and look for an allprojects section:

Example project-level build.gradle (excerpt)

```groovy
allprojects {
    repositories {
        // ... other project repositories
        maven {
            url "https://artifactory.appodeal.com/appodeal"
        }
    }
}
```

#### Add maven dependencies

Next, open the app-level build.gradle file for your app, and look for the dependencies section:

Example app-level build.gradle (excerpt)

```groovy
dependencies {
    // ... other project dependencies

    implementation 'com.explorestack.hs:sdk:1.0.0'
}
```

#### Add required connectors and services

Example app-level build.gradle (excerpt)

```groovy
dependencies {
    // ... other project dependencies

    //Appodeal SDK connector
    implementation 'com.explorestack.hs.sdk.connector:appodeal:1.0.0'
    //AppsFlyer service
    implementation 'com.explorestack.hs.sdk.service:appsflyer:1.0.0'
    //Facebook service
    implementation 'com.explorestack.hs.sdk.service:facebook:1.0.1'
    //Firebase service
    implementation 'com.explorestack.hs.sdk.service:firebase:1.0.0'
}
```

##  Initialize SDK

Holistic Solution SDK will automatically initialize all registered services (e.g - AppsFlyer, Firebase) and sync all required data to registered connectors (e.g - Appodeal).

To initialize SDK add the line below to onCreate method of your application or activity class:

```java
public class YourApplication extends Application {
    ...
    @Override
    public void onCreate() {
        super.onCreate();

        //Create connector for Appodeal
        HSAppodealConnector appodealConnector = new HSAppodealConnector();

        //Create service for AppsFlyer
        HSAppsflyerService appsflyerService = new HSAppsflyerService(YOUR_APPSFLYER_DEV_KEY);

        //Create Facebook service
        HSFacebookService facebookService = new HSFacebookService();

        //Create service for Firebase
        HSFirebaseService firebaseService = new HSFirebaseService();

        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                .withConnectors(appodealConnector)
                .withServices(appsflyerService, facebookService, firebaseService);
        
        //Initialize HSApp
        HSApp.initialize(activity, appConfig, new HSAppInitializeListener() {
            @Override
            public void onAppInitialized(@Nullable List<HSError> errors) {
                //HSApp initialization finished, now you can initialize required SDK
            }
        });
    }
    ...
}
```

> Please note that you should call `HSAppConfig.withConnectors` and `HSAppConfig.withServices` only once, since they will override previous appropriate values

[Code example](example/src/main/java/com/explorestack/hs/sdk/example/ExampleApplication.java#L28)

[Appodeal initialization example](connectors/appodeal/README.md#appodeal_sdk_initialization)

#### Enable debug logic

Enable HSApp, services and connectors debug logic if possible

> Please note that not all services and connector have appropriate logic

```java
HSAppConfig appConfig = new HSAppConfig()
        //Enable debug logic
        .setDebugEnabled(true);
```

#### Enable logs

Enable HSApp, services and connectors logging

```java
HSLogger.setEnabled(true)
```

### AndroidX

HS SDK using [AndroidX](https://developer.android.com/jetpack/androidx), so please make sure you have enabled [Jetifier](https://developer.android.com/jetpack/androidx#using_androidx_libraries_in_your_project)

## Events

Holistic Solution SDK allows you to send events to analytic services such as Firebase, AppsFlyer and Facebook using a single method:

```java
// Create map of event parameters if required
Map<String, Object> params = new HashMap<>();
params.put("example_param_1", "Param1 value");
params.put("example_param_2", 123);

// Send event to all connected analytics services
HSApp.logEvent("hs_sdk_example_test_event", params);
```

> Event parameters can only be strings and numbers

[Code example](example/src/main/java/com/explorestack/hs/sdk/example/ExampleActivity.java#L67)

#### Disabling Events for specific service or connector

If you want to disable Events for specific service or connector, you can call `setEventsEnabled` on appropriate component:

```java
HSFacebookService facebookService = new HSFacebookService();
//Disable service Events 
facebookService.setEventsEnabled(false);
```

## In-App purchase validation

Holistic Solution SDK allows you to unify In-App purchase validation using a single method:

```java
// Purchase object is returned by Google API in onPurchasesUpdated() callback
public void validatePurchase(Purchase purchase) {
    // Create new HSInAppPurchase
    HSInAppPurchase purchase = HSInAppPurchase.newBuilder()
            .withPublicKey("YOUR_PUBLIC_KEY")
            .withSignature(purchase.getSignature())
            .withPurchaseData(purchase.getOriginalJson())
            .withPrice(...)
            .withCurrency(...)
            .withAdditionalParams(...)
            .build();

    // Validate InApp purchase
    HSApp.validateInAppPurchase(purchase, new HSInAppPurchaseValidateListener() {
        @Override
        public void onInAppPurchaseValidateSuccess(@NonNull HSInAppPurchase purchase,
                                                   @Nullable List<HSError> errors) {
            // In-App purchase validation was validated successfully by at least one
            // connected service
        }

        @Override
        public void onInAppPurchaseValidateFail(@NonNull List<HSError> errors) {
            // In-App purchase validation was failed by all connected service
        }
    });
}
```

> In-App purchase validation runs by FIFO queue in a single thread

[Code example](example/src/main/java/com/explorestack/hs/sdk/example/ExampleActivity.java#L82)