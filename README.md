# About

Stack Holistic Solution SDK for Android simplifies the collection and transfer of the necessary parameters from third-party services to the corresponding Stack SDKs to improve the performance of services such as Mediation and UA

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
    //Firebase service
    implementation 'com.explorestack.hs.sdk.service:firebase:1.0.0'
}
```

[initialize_sdk]: initialize_sdk
##  Initialize SDK

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

        //Create service for Firebase
        HSFirebaseService firebaseService = new HSFirebaseService();

        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                .withConnectors(appodealConnector)
                .withServices(appsflyerService, firebaseService);
        
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

## AndroidX

HS SDK using [AndroidX](https://developer.android.com/jetpack/androidx), so please make sure you have enabled [Jetifier](https://developer.android.com/jetpack/androidx#using_androidx_libraries_in_your_project)
