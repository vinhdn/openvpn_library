package de.blinkt.openvpn;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

public class NotificationLiveData extends MutableLiveData<JSONObject> {

    public void setNotification(JSONObject message){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setValue(message);
            }
        });
    }
}