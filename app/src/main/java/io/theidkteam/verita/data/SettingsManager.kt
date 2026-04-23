package io.theidkteam.verita.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("verita_settings", Context.MODE_PRIVATE)

    var proxyHost by mutableStateOf(prefs.getString("proxy_host", "") ?: "")
    var proxyPort by mutableStateOf(prefs.getInt("proxy_port", 0))
    var useProxy by mutableStateOf(prefs.getBoolean("use_proxy", false))

    var primaryR by mutableStateOf(prefs.getInt("primary_r", 98))
    var primaryG by mutableStateOf(prefs.getInt("primary_g", 0))
    var primaryB by mutableStateOf(prefs.getInt("primary_b", 238))

    var fontFamily by mutableStateOf(prefs.getString("font_family", "Default") ?: "Default")
    var language by mutableStateOf(prefs.getString("language", "en") ?: "en")

    fun saveLanguage(langCode: String) {
        language = langCode
        prefs.edit().putString("language", langCode).apply()
    }

    fun saveProxy(host: String, port: Int, enabled: Boolean) {
        proxyHost = host
        proxyPort = port
        useProxy = enabled
        prefs.edit()
            .putString("proxy_host", host)
            .putInt("proxy_port", port)
            .putBoolean("use_proxy", enabled)
            .apply()
    }

    fun saveColors(r: Int, g: Int, b: Int) {
        primaryR = r
        primaryG = g
        primaryB = b
        prefs.edit()
            .putInt("primary_r", r)
            .putInt("primary_g", g)
            .putInt("primary_b", b)
            .apply()
    }

    fun getProxy(): Proxy? {
        if (!useProxy || proxyHost.isBlank() || proxyPort <= 0) return null
        return Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort))
    }
}
