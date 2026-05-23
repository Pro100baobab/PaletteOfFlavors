package com.paletteofflavors.data.remote.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.paletteofflavors.domain.utils.InternetChecker

class AndroidInternetChecker(private val context: Context) : InternetChecker {
    override fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
