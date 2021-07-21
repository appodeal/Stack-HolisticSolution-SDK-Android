# About
Stack Holistic Solution SDK for Android simplifies the collection and transfer of the necessary parameters from third-party services to the corresponding Stack SDKs to improve the performance of services such as Mediation and UA

## Integration Guide
- [Before integration started](#before-integration-started)
- [Import SDK](#import-sdk)
	- [Add the Appodeal maven repository](#1-add-the-appodeal-maven-repository)
	- [Add maven dependencies](#2-add-maven-dependencies)
	- [Setup required services](#3-setup-required-services)
		- [Facebook Service](#31-facebook-service)
		- [Firebase Service](#32-firebase-service)
* [Initialize SDK](#initialize-sdk)
* [Features](#features)
  * [Enable debug logic](#enable-debug-logic)
  * [Enable logs](#enable-logs)
  * [Events](#events)
  * [In-App purchase validation](#purchase-validation)

## Before integration started

HS SDK using [AndroidX](https://developer.android.com/jetpack/androidx), so please make sure you have enabled [Jetifier](https://developer.android.com/jetpack/androidx#using_androidx_libraries_in_your_project)

## Import SDK

### 1. Add the Appodeal maven repository

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

### 2. Add maven dependencies

Next, open the app-level build.gradle file for your app, and look for the dependencies section:

Example app-level build.gradle (excerpt)

```groovy
dependencies {
    // ... other project dependencies
    implementation 'com.explorestack.hs:sdk:2.0.0.+'
}
```

### 3. Setup required services
#### 3.1. Facebook Service
> Note that HS Facebook Service will include only 'facebook-core' dependency independently

###### 1. Configure Your Facebook App

Please follow this [guide](https://developers.facebook.com/docs/app-events/getting-started-app-events-android) to configure you Facebook app

###### 2. Add Your Facebook App ID

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
#### 3.2. Firebase Service
>Note that HS Firebase Service will include 'firebase-analytics' and 'firebase-config' dependencies independently

###### 1. Configure Your Firebase App

Please, follow this [guide](https://firebase.google.com/docs/android/setup#console) to configure you Firebase app

##  Initialize SDK

Holistic Solution SDK will automatically initialize all components and sync all required data to connectors (e.g - Appodeal).

To initialize SDK add the line below to onCreate method of your application or activity class.

Initialization example:

```java
public class YourApplication extends Application {
    ...
    @Override
    public void onCreate() {
        super.onCreate();

	//Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                .setAppKey(YOUR_APPODEAL_KEY)
                .setAdType(REQUIRED_ADS_TYPES)
                .setDebugEnabled(...)
                .setComponentInitializeTimeout(...);

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
| Parameter            | Description                                                                                                        		               |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| appKey               | [Appodeal application key](https://app.appodeal.com/apps).							                                                       |
| adType               | Appodeal ad types (e.g - `Appodeal.INTERSTITIAL`).                                   	           	                        			   |
| debug                | Enable sdk, services and connectors debug logic if possible.                        				                             		   |
| timeout              | In this case is timeout for **one** operation: starting attribution service or fetching remote config. By default the value is **30 sec**.|

[Code example](example/src/main/java/com/explorestack/hs/sdk/example/ExampleApplication.java#L28)


## Features

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


#### Purchase validation
Holistic Solution SDK allows you to unify purchase validation using a single method:
```java
// Purchase object is returned by Google API in onPurchasesUpdated() callback
public void validatePurchase(Purchase purchase) {
    
    // Create new HSInAppPurchase
    HSInAppPurchase hsPurchase = HSInAppPurchase.newBuilder("PURCHASE_TYPE")
        .withPublicKey("YOUR_PUBLIC_KEY")
        .withSignature(purchase.getSignature())
        .withPurchaseData(purchase.getOriginalJson())
        .withPurchaseToken(purchase.getPurchaseToken())
        .withPurchaseTimestamp(purchase.getPurchaseTime())
        .withOrderId(purchase.getOrderId())
        .withSku(...)
        .withPrice(...)
        .withCurrency(...)
        .withAdditionalParams(...)
        .build();	    

    // Validate InApp purchase
    HSApp.validateInAppPurchase(hsPurchase, new HSInAppPurchaseValidateListener() {
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
| purchaseType         | Purchase type. Must be one of [PurchaseType](sdk/src/main/java/com/explorestack/hs/sdk/HSInAppPurchase.java#L7).   |
| publicKey            | [Public key from Google Developer Console](https://support.google.com/googleplay/android-developer/answer/186113). |
| signature            | Transaction signature (returned from Google API when the purchase is completed).                                   |
| purchaseData         | Product purchased in JSON format (returned from Google API when the purchase is completed).                        |
| purchaseToken        | Product purchased token (returned from Google API when the purchase is completed).                        	        |
| purchaseTimestamp    | Product purchased timestamp (returned from Google API when the purchase is completed).                        	    |
| orderId              | Product purchased unique order id for the transaction (returned from Google API when the purchase is completed).   |
| sku                  | Stock keeping unit id.											                                                    |
| price                | Purchase revenue.                                                                                                  |
| currency             | Purchase currency.                                                                                                 |
| additionalParameters | Additional parameters of the purchase event.                                                                       |

> In-App purchase validation runs by FIFO queue in a single thread

[Code example](example/src/main/java/com/explorestack/hs/sdk/example/ExampleActivity.java#L122)
