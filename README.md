# About
Stack Holistic Solution SDK for Android simplifies the collection and transfer of the necessary parameters from third-party services to the corresponding Stack SDKs to improve the performance of services such as Mediation and UA

## Integration Guide
- [Before integration started](#before-integration-started)
- [Import SDK](#import-sdk)
	- [Add the Appodeal maven repository](#1-add-the-appodeal-maven-repository)
	- [Add maven dependencies](#2-add-maven-dependencies)
	- [Add required connectors and services](#3-add-required-connectors-and-services)
        * [Connector integration](#31-connector-integration)
            * [Appodeal Connector](#appodeal-connector)
        * [Service integration](#32-service-integration)
            * [AppsFlyer Service](#appsflyer-service)
            * [Facebook Service](#facebook-service)
            * [Firebase Service](#firebase-service)
* [Initialize SDK](#initialize-sdk)
* [Features](#features)
  * [Enable debug logic](#enable-debug-logic)
  * [Enable logs](#enable-logs)
  * [Events](#events)
  * [In-App purchase validation](#in-app-purchase-validation)

## Before integration started

HS SDK using [AndroidX](https://developer.android.com/jetpack/androidx), so please make sure you have enabled [Jetifier](https://developer.android.com/jetpack/androidx#using_androidx_libraries_in_your_project)

## Import SDK

#### 1. Add the Appodeal maven repository

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

#### 2. Add maven dependencies

Next, open the app-level build.gradle file for your app, and look for the dependencies section:

Example app-level build.gradle (excerpt)

```groovy
dependencies {
    // ... other project dependencies
    implementation 'com.explorestack.hs:sdk:1.0.2'
}
```

#### 3. Add required connectors and services

In the HS SDK, we distinguish 2 main entities, these are HSService and HSConnector. HSService is a third-party SDK that does some work as a separate service. HSConnector is an entity that receives events from Services and passes them for processing(in the Appodeal SDK)

##### 3.1. Connector integration

- #### [Appodeal Connector](connectors/appodeal/README.md)

Follow the [link](connectors/appodeal/README.md)  and configure the Appodeal Connector, then continue the integration.

##### 3.2. Service integration

- #### [AppsFlyer Service](services/appsflyer/README.md)

Follow the [link](services/appsflyer/README.md) and configure the AppsFlyer Service, then continue the integration.

- #### [Facebook Service](services/facebook/README.md)

Follow the [link](services/facebook/README.md) and configure the Facebook Service, then continue the integration.

- #### [Firebase Service](services/firebase/README.md)

Follow the [link](services/firebase/README.md) and configure the Firebase Service, then continue the integration.

##  Initialize SDK

Holistic Solution SDK will automatically initialize all registered services (e.g - AppsFlyer, Firebase) and sync all required data to registered connectors (e.g - Appodeal).

To initialize SDK add the line below to onCreate method of your application or activity class.

Initialization example:

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

## Features

#### Enable debug logic
Enable HSApp, services and connectors debug logic if possible

```java
HSAppConfig appConfig = new HSAppConfig()
        //Enable debug logic
        .setDebugEnabled(true);
```

> Please note that not all services and connector have appropriate logic

#### Enable logs
Enable HSApp, services and connectors logging.

```java
HSLogger.setEnabled(true)
```
After enabling it, you can view logs by the `HSApp` tag

#### Events
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

##### Disabling Events for specific service or connector
If you want to disable Events for specific service or connector, you can call `setEventsEnabled` on appropriate component:
```java
HSFacebookService facebookService = new HSFacebookService();
//Disable service Events
facebookService.setEventsEnabled(false);
```
#### In-App purchase validation
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

| Parameter            | Description                                                                                                        |
|----------------------|--------------------------------------------------------------------------------------------------------------------|
| publicKey            | [Public key from Google Developer Console](https://support.google.com/googleplay/android-developer/answer/186113)  |
| signature            | Transaction signature (returned from Google API when the purchase is completed).                                   |
| purchaseData         | Product purchased in JSON format (returned from Google API when the purchase is completed).                        |
| price                | In-app event revenue.                                                                                              |
| currency             | In-app event currency.                                                                                             |
| additionalParameters | Additional parameters of the in-app event.                                                                         |

> In-App purchase validation runs by FIFO queue in a single thread

[Code example](example/src/main/java/com/explorestack/hs/sdk/example/ExampleActivity.java#L82)