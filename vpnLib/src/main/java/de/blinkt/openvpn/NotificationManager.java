package de.blinkt.openvpn;

import org.json.JSONObject;

public class NotificationManager {

    private static final NotificationLiveData notificationLiveData = new NotificationLiveData();

    public static void updateNotificationMessage(JSONObject message){
        notificationLiveData.setNotification(message);
    }

    public static NotificationLiveData getNotificationLiveData() {
        return notificationLiveData;
    }
}