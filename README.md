# android_notification_listener

Flutter plugin to listen notifications on android

Add this to your android manifest
```
<service android:name="com.pkmnapps.android_notification_listener.NotificationListener"
            android:label="@string/service_name"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
</service>
```
