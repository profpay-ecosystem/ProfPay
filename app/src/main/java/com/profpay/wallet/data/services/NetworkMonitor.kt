package com.profpay.wallet.data.services

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class NetworkMonitor(
    context: Context,
    sharedPref: SharedPreferences,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkAvailable = MutableStateFlow(true)
    val networkAvailable: StateFlow<Boolean> = _networkAvailable.asStateFlow()

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//            CoroutineScope(Dispatchers.IO).launch {
//                val isInternetAvailable = isInternetAvailable()
//
//                _networkAvailable.value = isInternetAvailable
//                sharedPref.edit(commit = true) {
//                    putBoolean("is_blocked_app", isInternetAvailable)
//                }
//            }
            }

            override fun onLost(network: Network) {
//            sharedPref.edit(commit = true) {
//                putBoolean("is_blocked_app", false)
//            }
//            _networkAvailable.value = false
            }

            override fun onUnavailable() {
//            sharedPref.edit(commit = true) {
//                putBoolean("is_blocked_app", false)
//            }
//            _networkAvailable.value = false
            }
        }

    fun register() {
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    suspend fun isInternetAvailable(): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Socket().use {
                    it.connect(InetSocketAddress("1.1.1.1", 443), 1500)
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
}
