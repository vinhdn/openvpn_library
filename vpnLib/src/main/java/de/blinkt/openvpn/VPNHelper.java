package de.blinkt.openvpn;

import static de.blinkt.openvpn.core.OpenVPNService.humanReadableByteCount;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.OpenVPNManagement;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

public class VPNHelper extends Activity implements VpnStatus.StateListener, VpnStatus.ByteCountListener {
    public Activity activity;
    public LifecycleOwner lifecycleOwner;
    public static OnVPNStatusChangeListener listener;
    private static String config;
    private static boolean vpnStart;
    private static Intent profileIntent;
    private static String username;
    private static String password;
    private static String keyPassword;
    private static String name;
    private static List<String> bypassPackages;

    public JSONObject status = new JSONObject();

    private String byteIn, byteOut;
    private String duration;

    long c = Calendar.getInstance().getTimeInMillis();
    long time;
    int lastPacketReceive = 0;
    String seconds = "0", minutes, hours;

    public boolean isConnected(){
        return vpnStart;
    }

    public VPNHelper(Activity activity, LifecycleOwner lifecycleOwner) {
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
        VPNHelper.vpnStart = false;
        VpnStatus.initLogCache(activity.getCacheDir());
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
    }

    public void setOnVPNStatusChangeListener(OnVPNStatusChangeListener listener) {
        VPNHelper.listener = listener;
        NotificationManager.getNotificationLiveData().observe(lifecycleOwner, notificationObserver);
    }

    public void startVPN() {
        if (!vpnStart) connect();
    }


    public void startVPN(String config, String username, String password, String name, List<String> bypass) {
        startVPN(config, username, password, "", name, bypass);
    }

    public void startVPN(String config, String username, String password, String keyPassword, String name, List<String> bypass) {
        VPNHelper.config = config;
        VPNHelper.profileIntent = VpnService.prepare(activity);
        VPNHelper.username = username;
        VPNHelper.password = password;
        VPNHelper.keyPassword = keyPassword;
        VPNHelper.name = name;
        VPNHelper.bypassPackages = bypass;

        if (profileIntent != null) {
            activity.startActivityForResult(VPNHelper.profileIntent, 1);
        }else{
            startVPN();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launchvpn);
        NotificationManager.getNotificationLiveData().observe(lifecycleOwner, notificationObserver);
        startVPN();
    }

    public void stopVPN() {
//        OpenVPNThread.stop();
//        OpenVpn
    }

    private void connect() {
        try {
            OpenVpnApi.startVpn(activity, config,name, username, password, keyPassword, bypassPackages);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setStage(String stage) {
        String output = stage;
        switch (stage.toUpperCase()) {
            case "CONNECTED":
                output = "connected";
                vpnStart = true;
                break;
            case "DISCONNECTED":
                output = "disconnected";
                vpnStart = false;
//                OpenVPNService.setDefaultStatus();
                break;
            case "WAIT":
                output = "wait_connection";
                break;
            case "AUTH":
                output = "authenticating";
                break;
            case "RECONNECTING":
                output = "reconnect";
                break;
            case "NONETWORK":
                output = "no_connection";
                break;
            case "CONNECTING":
                output = "connecting";
                break;
            case "PREPARE":
                output = "prepare";
                break;
            case "DENIED":
                output = "denied";
                break;
            case "ERROR":
                output = "error";
                break;
        }
        if (listener != null) listener.onVPNStatusChanged(output);
    }


    private final Observer<JSONObject> notificationObserver = new Observer<JSONObject>() {
        @Override
        public void onChanged(JSONObject object) {
            try {
                if (object.has("state")) {
                    setStage(object.getString("state"));
                }
                String duration = object.has("duration") ? object.getString("duration") : null;
                String lastPacketReceive = object.has("lastPacketReceive") ? object.getString("lastPacketReceive") : null;
                String byteIn = object.has("byteIn") ? object.getString("byteIn") : null;
                String byteOut = object.has("byteOut") ? object.getString("byteOut") : null;

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("connected_on", duration);
                    jsonObject.put("last_packet_receive", lastPacketReceive);
                    jsonObject.put("byte_in", byteIn);
                    jsonObject.put("byte_out", byteOut);

                    status = jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                VPNHelper.listener.onConnectionStatusChanged(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (intent.getStringExtra("state") != null) {
                    setStage(intent.getStringExtra("state"));
                }
                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("connected_on", duration);
                    jsonObject.put("last_packet_receive", lastPacketReceive);
                    jsonObject.put("byte_in", byteIn);
                    jsonObject.put("byte_out", byteOut);

                    status = jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                VPNHelper.listener.onConnectionStatusChanged(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onDetachedFromWindow() {
        NotificationManager.getNotificationLiveData().removeObserver(notificationObserver);
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        NotificationManager.getNotificationLiveData().observe(lifecycleOwner, notificationObserver);
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
        String status = OpenVPNService.getStatus();
        if (status != null) setStage(status);
        super.onAttachedToWindow();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                startVPN();
            } else {
                VPNHelper.listener.onVPNStatusChanged("denied");
            }
        }
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent) {
        setStage(state);
    }

    @Override
    public void setConnectedVPN(String uuid) {

    }


    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        byteIn = String.format("↓%2$s", getString(R.string.statusline_bytecount),
                humanReadableByteCount(in,false, getResources())) + " - " + humanReadableByteCount(diffIn / OpenVPNManagement.mBytecountInterval, false, getResources()) + "/s";
        byteOut = String.format("↑%2$s", getString(R.string.statusline_bytecount),
                humanReadableByteCount(out, false,getResources())) + " - " + humanReadableByteCount(diffOut / OpenVPNManagement.mBytecountInterval, false, getResources()) + "/s";
        long time = Calendar.getInstance().getTimeInMillis() - c;
        lastPacketReceive = Integer.parseInt(convertTwoDigit((int) (time / 1000) % 60)) - Integer.parseInt(seconds);
        seconds = convertTwoDigit((int) (time / 1000) % 60);
        minutes = convertTwoDigit((int) ((time / (1000 * 60)) % 60));
        hours = convertTwoDigit((int) ((time / (1000 * 60 * 60)) % 24));
        duration = hours + ":" + minutes + ":" + seconds;
        lastPacketReceive = checkPacketReceive(lastPacketReceive);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("connected_on", duration);
            jsonObject.put("last_packet_receive", lastPacketReceive);
            jsonObject.put("byte_in", byteIn);
            jsonObject.put("byte_out", byteOut);

            status = jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VPNHelper.listener.onConnectionStatusChanged(duration, String.valueOf(lastPacketReceive), byteIn, byteOut);
    }

    public int checkPacketReceive(int value) {
        value -= 2;
        return Math.max(value, 0);
    }
    public String convertTwoDigit(int value) {
        if (value < 10) return "0" + value;
        else return value + "";
    }
}