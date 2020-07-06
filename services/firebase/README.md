# Firebase Service for Holistic Solution SDK

> Note that HS Firebase Service will include 'firebase-analytics' and 'firebase-config' dependencies independently

## Integrate Firebase

Please, follow one of these [steps](https://firebase.google.com/docs/android/setup#console) to connect the Android application to Firebase

## Integrate Firebase Remote Config SDK

Please, follow this [guide](https://firebase.google.com/docs/remote-config/use-config-android) to add Firebase Remote Config SDK to you project

## Import Service

```groovy
dependencies {
    // ... other project dependencies

    implementation 'com.explorestack.hs.sdk.service:firebase:1.0.0'
}
```

## Register Service 

```java
public class MainActivity extends AppCompatActivity {
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create service for Firebase
        HSFirebaseService firebaseService = new HSFirebaseService();

        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                //Include Firebase service to HSApp config
                .withServices(firebaseService, ...);      
    }
    ...
}
```

#### Service parameters

| Parameter                     | Required | Description                                                                                                                                                                                                                                                                                                                 |
|-------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| targetValuesKeys              | No       | Values keys, which should be collected from Firebase Remote Config SDK response. If it's not provided or it's empty, all values will be collected                                                                                                                                                                           |
| minimumFetchIntervalInSeconds | No       | The default minimum fetch interval which means that configs won't be fetched from the backend more than once in provided interval window, regardless of how many fetch calls are actually made. You can read more about this parameter [here](https://firebase.google.com/docs/remote-config/use-config-android#throttling) |

#### Service methods

| Method               | Description                                                                     |
|----------------------|---------------------------------------------------------------------------------|
| setFirebaseAnalytics | Sets custom `FirebaseAnalytics` instance which will be used for dispatch events |