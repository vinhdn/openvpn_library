package com.vinhdn.vpnlib.example

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinhdn.vpnlib.example.ui.theme.Openvpn_libraryTheme
import de.blinkt.openvpn.OnVPNStatusChangeListener
import de.blinkt.openvpn.VPNHelper
import de.blinkt.openvpn.activities.DisconnectVPN
import de.blinkt.openvpn.api.RemoteAction
import de.blinkt.openvpn.core.OpenVPNService

class MainActivity : ComponentActivity() {

    private lateinit var vpnHelper: VPNHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val multipleState = mutableStateOf("init")
        val multipleStatus = mutableStateOf("--")
        setContent {
            Openvpn_libraryTheme {
                val state by remember {
                    multipleState
                }
                val status by remember {
                    multipleStatus
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Column {
                        val modifier = Modifier.padding(innerPadding)

                        Greeting(
                            name = state,
                            modifier = Modifier.padding(innerPadding)
                        )

                        Text(
                            text = status,
                            modifier = modifier
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                                vpnHelper.startVPN(defaultConfig, "vpnbook", "zm396a4", "", "Korea", listOf())
                        }) {
                            Text(text = "Connect")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            val intentDisconnect = Intent(this@MainActivity, RemoteAction::class.java)
                            intentDisconnect.setComponent(
                                ComponentName.createRelative(
                                    this@MainActivity,
                                    ".api.DisconnectVPN"
                                )
                            )
                            intentDisconnect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intentDisconnect)
                        }) {
                            Text(text = "Disconnect")
                        }
                    }
                }
            }
        }
        VPNHelper.listener = object : OnVPNStatusChangeListener {

            override fun onVPNStatusChanged(status: String?) {
                multipleState.value = status ?: "Android"
            }

            override fun onConnectionStatusChanged(
                duration: String?,
                lastPacketReceive: String?,
                byteIn: String?,
                byteOut: String?
            ) {
                multipleStatus.value = "Duration: $duration\nLast packet receive: $lastPacketReceive\nByte in: $byteIn\nByte out: $byteOut"
            }
        }
        vpnHelper = VPNHelper(this, this)
        vpnHelper.onAttachedToWindow()
        if(intent.action == OpenVPNService.DISCONNECT_VPN) {
            startActivity(Intent(this, DisconnectVPN::class.java))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(null)
        if(intent.action == OpenVPNService.DISCONNECT_VPN) {
            startActivity(Intent(this, DisconnectVPN::class.java))
        }
        println("onNewIntent")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish();
    }

    override fun onDestroy() {
        vpnHelper.onDetachedFromWindow()
        super.onDestroy()
    }

    val defaultConfig = """client
dev tun1
proto tcp
remote 144.217.253.149 443
resolv-retry infinite
nobind
persist-key
persist-tun
auth-user-pass
comp-lzo
verb 3
cipher AES-256-CBC
fast-io
pull
route-delay 2
redirect-gateway
<ca>
-----BEGIN CERTIFICATE-----
MIIDSzCCAjOgAwIBAgIUJdJ6+6lTiYZBvpl2P40Lgx3BeHowDQYJKoZIhvcNAQEL
BQAwFjEUMBIGA1UEAwwLdnBuYm9vay5jb20wHhcNMjMwMjIwMTk0NTM1WhcNMzMw
MjE3MTk0NTM1WjAWMRQwEgYDVQQDDAt2cG5ib29rLmNvbTCCASIwDQYJKoZIhvcN
AQEBBQADggEPADCCAQoCggEBAMcVK+hYl6Wl57YxXIVy7Jlgglj42LaC2sUWK3ls
aRcKQfs/ridG6+9dSP1ziCrZ1f5pOLz34gMYXChhUOc/x9rSIRGHao4gHeXmEoGs
twjxA+kRBSv5xqeUgaTKAhdwiV5SvBE8EViWe3rlHLoUbWBQ7Kky/L4cg7u+ma1V
31PgOPhWY3RqZJLBMu3PHCctaaHQyoPLDNDyCz7Zb2Wos+tjIb3YP5GTfkZlnJsN
va0HdSGEyerTQL5fqW2V6IZ4t2Np2kVnJcfEWgJF0Kw1nqoPfKjxM44bR+K1EGGW
ir1rs/RFPg8yFVxd4ZHpqoCo2lXZjc6oP1cwtIswIHb6EbsCAwEAAaOBkDCBjTAd
BgNVHQ4EFgQULgM8Z91cLOSHl6EDF8jalx3piqQwUQYDVR0jBEowSIAULgM8Z91c
LOSHl6EDF8jalx3piqShGqQYMBYxFDASBgNVBAMMC3ZwbmJvb2suY29tghQl0nr7
qVOJhkG+mXY/jQuDHcF4ejAMBgNVHRMEBTADAQH/MAsGA1UdDwQEAwIBBjANBgkq
hkiG9w0BAQsFAAOCAQEAT5hsP+dz11oREADNMlTEehXWfoI0aBws5c8noDHoVgnc
BXuI4BREP3k6OsOXedHrAPA4dJXG2e5h33Ljqr5jYbm7TjUVf1yT/r3TDKIJMeJ4
+KFs7tmXy0ejLFORbk8v0wAYMQWM9ealEGePQVjOhJJysEhJfA4u5zdGmJDYkCr+
3cTiig/a53JqpwjjYFVHYPSJkC/nTz6tQOw9crDlZ3j+LLWln0Cy/bdj9oqurnrc
xUtl3+PWM9D1HoBpdGduvQJ4HXfss6OrajukKfDsbDS4njD933vzRd4E36GjOI8Q
1VKIe7kamttHV5HCsoeSYLjdxbXBAY2E0ZhQzpZB7g==
-----END CERTIFICATE-----
</ca>
<cert>
-----BEGIN CERTIFICATE-----
MIIDYDCCAkigAwIBAgIQP/z/mAlVNddzohzjQghcqzANBgkqhkiG9w0BAQsFADAW
MRQwEgYDVQQDDAt2cG5ib29rLmNvbTAeFw0yMzAyMjAyMzMwNDlaFw0zMzAyMTcy
MzMwNDlaMB0xGzAZBgNVBAMMEmNsaWVudC52cG5ib29rLmNvbTCCASIwDQYJKoZI
hvcNAQEBBQADggEPADCCAQoCggEBANPiNyyYH6yLXss6AeHLzJ6/9JfUzVAs7ttq
8OWJRkBjKuEPW3MUVjpMgptm6+zJohM4IdSo/ES6H81sLK4AWiUUOzeOt8xAzgib
NrLss5px0D0Pm+uXH8hGOle386JH5oyOQ6ub2O3ro0TeTF4rg43TF1oOz2AVS/gc
sB3d6AG73otZ4C6/wabiGz4rFO8xl4S4PBKX73Eb7cdSoACc8AIrqcR+PEDHOZYt
1qp4lM87+5ADEXelpe9vLTaoXonIuZElqA9rwFi/KQmPCHsl7eEnmSo1iOg0y3iP
0CRHzv8FkvhhpB9Z3i3TUxq8XvnLtEQ38eD5Dw20WMYPmPShtXMCAwEAAaOBojCB
nzAJBgNVHRMEAjAAMB0GA1UdDgQWBBQKO5Ub8pRCA8iTdRIxUIeMpNX2vzBRBgNV
HSMESjBIgBQuAzxn3Vws5IeXoQMXyNqXHemKpKEapBgwFjEUMBIGA1UEAwwLdnBu
Ym9vay5jb22CFCXSevupU4mGQb6Zdj+NC4MdwXh6MBMGA1UdJQQMMAoGCCsGAQUF
BwMCMAsGA1UdDwQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAel1YOAWHWFLH3N41
SCIdQNbkQ18UFBbtZz4bzV6VeCWHNzPtWQ6UIeADpcBp0mngS09qJCQ8PwOBMvFw
MizhDz2Ipz817XtLJuEMQ3Io51LRxPP394mlw46e8KFOh06QI8jnC/QlBH19PI+M
OeQ3Gx6uYK41HHmyu/Z7dUE4c4s2iiHA7UgD98dkrU0rGAv1R/d2xRXqEm4PrwDj
MlC1TY8LrIJd6Ipt00uUfHVAzhX3NKR528azYH3bud5NV+KEiQZSyirUyoMbMQeO
UXh+GEDX5GBPElzQmPOsLete/PMH9Ayg6Gh/sccqwgH7BxjqcVLKXg2S4jL5BUPd
kI3/sg==
-----END CERTIFICATE-----
</cert>
<key>
-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDT4jcsmB+si17L
OgHhy8yev/SX1M1QLO7bavDliUZAYyrhD1tzFFY6TIKbZuvsyaITOCHUqPxEuh/N
bCyuAFolFDs3jrfMQM4Imzay7LOacdA9D5vrlx/IRjpXt/OiR+aMjkOrm9jt66NE
3kxeK4ON0xdaDs9gFUv4HLAd3egBu96LWeAuv8Gm4hs+KxTvMZeEuDwSl+9xG+3H
UqAAnPACK6nEfjxAxzmWLdaqeJTPO/uQAxF3paXvby02qF6JyLmRJagPa8BYvykJ
jwh7Je3hJ5kqNYjoNMt4j9AkR87/BZL4YaQfWd4t01MavF75y7REN/Hg+Q8NtFjG
D5j0obVzAgMBAAECggEAAV/BLdfatLq+paC9rGIu9ISYKHfn0PJJpkCeSU7HltlN
yOHZnPhvyrb+TdWwB/wSwf8mMQPbhvKSDDn8XDCCZSUpcSXKyVdOPr4K78QbMhA0
4oB8aV20hg72h+UYfl/q/dRaWf2LvZc+ms66Pg4YL05EI4BfFedtc7Fz7u2meIRl
Wm0b7/QQ10wrR1I7PonZzgnU9diB1cKxptJ06AfJmCGobymjq/A1JsAr/NFnJlmu
yq3n5tcRpfc8K+XsfnpwDQJo3kKwLGIoBmUkGEcHgQhVwOL5+P+3pTYr1bt4cAUp
FxbExqcxW0es//g3x2Z80icUpa4/OvSTAa0XF3J4UQKBgQDv4E/3/r5lULYIksNC
zO1yRp7awv41ZAUpDSglNtWsehnhhUsiVE/Ezmyz4E0qjsW2+EUxUZ990T4ZVK4b
9cEhB/TDBc6PBPd498aIGiiqznWXMdsU2o6xrvkQeWdmXoVjvWTcRWlfAQ+PQBOJ
tJ3wR7ZoHgu0P/yzIzn0eQ+BiQKBgQDiIDgRtlQBw8tZc66OzxWOuJh6M5xUF5zY
S0SLXFWlKVkfGACaerHUlFwZKID39BBifgXO1VOQ6AzalDd2vaIU9CHo2bFpTY7S
EkkcIt9Gpl5o1sjEyJChXBIz+s48XBMXlqFN7AdhX/H6R43g8eS/YlzqSBxkUcAa
V3tt8n+sGwKBgD+aSXnnKNKyWOHjEDUJIzh2sy4sH71GXPvqiid756II6g3bCvX6
RwBW/4meQrezDYebQrV2AAUbUwziYBv3yJKainKfeop/daK0iAaUcQ4BGjrRtFZO
MSG51D5jAmCpVVMB59lj6jGPlXGVOtj7dBk+2oW22cGcacOR5o8E/nCJAoGBALVP
KCXrj8gqea4rt1cCbEKXeIrjPwGePUCgeUFUs8dONAteb31ty5CrtHznoSEvLMQM
UBPbsLmLlmLcXOx0eLVcWqQdiMbqTQ3bY4uP2n8HfsOJFEnUl0MKU/4hp6N2IEjV
mlikW/aTu632Gai3y7Y45E9lqn41nlaAtpMd0YjpAoGBAL8VimbhI7FK7X1vaxXy
tnqLuYddL+hsxXXfcIVNjLVat3L2WN0YKZtbzWD8TW8hbbtnuS8F8REg7YvYjkZJ
t8VO6ZmI7I++borJBNmbWS4gEk85DYnaLI9iw4oF2+Dr0LKKAaUL+Pq67wmvufOn
hTobb/WAAcA75GKmU4jn5Ln2
-----END PRIVATE KEY-----
</key>
""";

    val testConfig = """client
proto udp
explicit-exit-notify
remote 103.82.21.184 1194
dev tun
resolv-retry infinite
nobind
persist-key
persist-tun
remote-cert-tls server
verify-x509-name server_LQxCWL0DiT3UFCc3 name
auth SHA256
auth-nocache
cipher AES-128-GCM
tls-client
tls-version-min 1.2
tls-cipher TLS-ECDHE-ECDSA-WITH-AES-128-GCM-SHA256
ignore-unknown-option block-outside-dns
setenv opt block-outside-dns # Prevent Windows 10 DNS leak
verb 3
<ca>
-----BEGIN CERTIFICATE-----
MIIB1TCCAXugAwIBAgITN7ZcwSTvocY+VF1QQ2iGuj7KrjAKBggqhkjOPQQDAjAe
MRwwGgYDVQQDDBNjbl8zc0xFYzB3V1poaU5ackRYMB4XDTIzMTAyNDE0MTMxOFoX
DTMzMTAyMTE0MTMxOFowHjEcMBoGA1UEAwwTY25fM3NMRWMwd1daaGlOWnJEWDBZ
MBMGByqGSM49AgEGCCqGSM49AwEHA0IABKGCrX4b5xjZAckQnJC1UsGIC87nzoJa
mAHGrtXMCJg7GW/uKn/Nd0h0zE7E9aOx3HsdklEz1bzIFZlenzS6xjCjgZcwgZQw
DAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUvIiAa6IzOCYCH/ZN91FSVyNr1LUwWAYD
VR0jBFEwT4AUvIiAa6IzOCYCH/ZN91FSVyNr1LWhIqQgMB4xHDAaBgNVBAMME2Nu
XzNzTEVjMHdXWmhpTlpyRFiCEze2XMEk76HGPlRdUENohro+yq4wCwYDVR0PBAQD
AgEGMAoGCCqGSM49BAMCA0gAMEUCIBMl22Oz2SXI6WbQHHIEjfB1YAOvQqxFbQZ5
+ZOdzvqMAiEA586MLlR0+/cAVS9u/9D56ZIP18ljfETXfAC7zAHxk7Y=
-----END CERTIFICATE-----
</ca>
<cert>
-----BEGIN CERTIFICATE-----
MIIB2TCCAYCgAwIBAgIRAN+7EmVNwOS1IdFEkBA++tYwCgYIKoZIzj0EAwIwHjEc
MBoGA1UEAwwTY25fM3NMRWMwd1daaGlOWnJEWDAeFw0yMzEwMjUwMjMwMjFaFw0y
NjAxMjcwMjMwMjFaMBMxETAPBgNVBAMMCHdpbmh1bWF4MFkwEwYHKoZIzj0CAQYI
KoZIzj0DAQcDQgAEW2/GiIN9ItBnNAI8QBtNwNaIErpJRMNCcspYvEqAu0L0DRhX
gNgwEuDiWl4gHp4+ub5KquK5oHXwLOdA1OCZYqOBqTCBpjAJBgNVHRMEAjAAMB0G
A1UdDgQWBBQeBH9PViDM2DZ4jdRCSMlTo8Q+iTBYBgNVHSMEUTBPgBS8iIBrojM4
JgIf9k33UVJXI2vUtaEipCAwHjEcMBoGA1UEAwwTY25fM3NMRWMwd1daaGlOWnJE
WIITN7ZcwSTvocY+VF1QQ2iGuj7KrjATBgNVHSUEDDAKBggrBgEFBQcDAjALBgNV
HQ8EBAMCB4AwCgYIKoZIzj0EAwIDRwAwRAIgFtlg/CvLa0EUSJhCH3Gemo/FeOQA
cObfGiNCCKwVWMYCIGN/X6Klsyr9ZI7WaFsPGQnu8tSMeo55wkodUBl0U2cn
-----END CERTIFICATE-----
</cert>
<key>
-----BEGIN ENCRYPTED PRIVATE KEY-----
MIHjME4GCSqGSIb3DQEFDTBBMCkGCSqGSIb3DQEFDDAcBAiQstu5718srgICCAAw
DAYIKoZIhvcNAgkFADAUBggqhkiG9w0DBwQIEiD0HUYiYAoEgZCqx4oUIrMkM77o
kkSnsPhkkp/WCdQoyrp6Pau1Mb/BoH4JcdTXvsH4FM5eRzjmEYLfO24D5bslbLq3
KsDcNrZ7ahQgV4eYcWQB4WXEirVWmo+c0beCJOmXq5ji5YaQ4zt0AcuDbbwO7Q+f
GYYlfFSe2jqSbZO652mL9tqMYifaeS0XzzwH3Jcja9X1+fvDdZY=
-----END ENCRYPTED PRIVATE KEY-----
</key>
<tls-crypt>
#
# 2048 bit OpenVPN static key
#
-----BEGIN OpenVPN Static key V1-----
8afbe1b632cd2b748e08d17b54fa0b8c
aa470e9ca327a7e52e552b0fd15eb4a8
53c7acbf946404cb75880a25c68f5406
0a6eb8d1cd9c5328e945d30797cbd26b
fe992f087f95b718202ea5d56511d889
438b59d7ca16b3d6a7fe68b85885c764
3d7726f8d817bfc8997509b2a73ab01b
f6ba49726e1a34f87d2805a8a0f696ce
5a21b86952108fb367440d6a674e9940
e23b2f866045ba89b3fc0d8564b24300
f26b787a25243bde2fe49b46dcfe7ffa
17c11129d36e34e3aa91530b900cbf3e
d9577493e91cb2bfe65a2f37dc3562a1
e262baa4536127b668a211462bf640cf
34b793146e52e4a43afe17bb676500df
b8b5275e00ba82737e38ae9e2b438efb
-----END OpenVPN Static key V1-----
</tls-crypt>
""";
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Openvpn_libraryTheme {
        Greeting("Android")
    }
}