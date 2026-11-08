package com.musica.paralosoidos.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isCurrentlyOnline(): Boolean
}

class ConnectivityNetworkMonitor(
    private val context: Context
) : NetworkMonitor {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override fun isCurrentlyOnline(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override val isOnline: Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networksWithInternet = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                val capabilities = cm.getNetworkCapabilities(network)
                val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                if (hasInternet) {
                    networksWithInternet.add(network)
                }
                channel.trySend(networksWithInternet.isNotEmpty())
            }

            override fun onLost(network: Network) {
                networksWithInternet.remove(network)
                channel.trySend(networksWithInternet.isNotEmpty())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasInternet) {
                    networksWithInternet.add(network)
                } else {
                    networksWithInternet.remove(network)
                }
                channel.trySend(networksWithInternet.isNotEmpty())
            }
        }

        channel.trySend(isCurrentlyOnline())

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching {
            cm.registerNetworkCallback(request, callback)
        }

        awaitClose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }.conflate()
}
