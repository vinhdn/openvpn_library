package de.blinkt.openvpn;

import static android.app.Activity.RESULT_OK;
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

public class VPNHelper implements VpnStatus.StateListener, VpnStatus.ByteCountListener {
    public Activity activity;
    public LifecycleOwner lifecycleOwner;
    public static OnVPNStatusChangeListener listener;
    private static String config;
    private static boolean vpnStart;
    private static Intent profileIntent;
    @Nullable private static String username;
    @Nullable private static String password;
    @Nullable private static String keyPassword;
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
    }

    public void startVPN() {
        connect();
    }

    public void startVPN(String config, String username, String password, String name, List<String> bypass) {
        startVPN(config, username, password, "", name, bypass);
    }

    public void startVPN(String config, String username, String password, String keyPassword, String name, List<String> bypass) {
        VPNHelper.config = config;
        VPNHelper.profileIntent = VpnService.prepare(activity);
        VPNHelper.username = (username == null || username.isEmpty()) ? null : username;
        VPNHelper.password = (password == null || password.isEmpty()) ? null : password;
        VPNHelper.keyPassword = (keyPassword == null || keyPassword.isEmpty()) ? null : keyPassword;
        VPNHelper.name = name;
        VPNHelper.bypassPackages = bypass;

        if (profileIntent != null) {
            activity.startActivityForResult(VPNHelper.profileIntent, 24);
        }else{
            startVPN();
        }
    }

    public void stopVPN() {
    }

    private void connect() {
        try {
            OpenVpnApi.startVpn(activity, config,name, username, password, keyPassword, bypassPackages);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setStage(String stage) {
        final String output;
        switch (stage.toUpperCase()) {
            case "CONNECTED":
                output = "connected";
                vpnStart = true;
                break;
            case "NOPROCESS":
            case "DISCONNECTED":
                output = "disconnected";
                vpnStart = false;
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
                vpnStart = false;
                break;
            case "CONNECTING":
                output = "connecting";
                break;
            case "PREPARE":
                output = "prepare";
                break;
            case "DENIED":
                output = "denied";
                vpnStart = false;
                break;
            case "ERROR":
                output = "error";
                vpnStart = false;
                break;
            default:
                output = stage.toLowerCase();
                vpnStart = false;
        }
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (VPNHelper.listener != null) VPNHelper.listener.onVPNStatusChanged(output);
            });
        }
    }

    public void onDetachedFromWindow() {
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
    }

    public void onAttachedToWindow() {
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                startVPN();
            } else {
                VPNHelper.listener.onVPNStatusChanged("denied");
            }
        }
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent, long lastConnectedTime) {
        if (activity == null) return;
        if (lifecycleOwner == null) return;
        setStage(state);
    }

    @Override
    public void setConnectedVPN(String uuid) {

    }


    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut, long lastConnectedTime) {
        if (activity == null) return;
        if (lifecycleOwner == null) return;
        byteIn = String.format("↓%2$s", activity.getString(R.string.statusline_bytecount),
                humanReadableByteCount(in,false, activity.getResources())) + " - " + humanReadableByteCount(diffIn / OpenVPNManagement.mBytecountInterval, false, activity.getResources()) + "/s";
        byteOut = String.format("↑%2$s", activity.getString(R.string.statusline_bytecount),
                humanReadableByteCount(out, false,activity.getResources())) + " - " + humanReadableByteCount(diffOut / OpenVPNManagement.mBytecountInterval, false, activity.getResources()) + "/s";
        long time = Calendar.getInstance().getTimeInMillis() - lastConnectedTime;
        if(time < 0) time = 0;
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

        if(activity != null) {
            activity.runOnUiThread(() -> {
                if (VPNHelper.listener != null)
                    VPNHelper.listener.onConnectionStatusChanged(duration, String.valueOf(lastPacketReceive), byteIn, byteOut);
            });
        }
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