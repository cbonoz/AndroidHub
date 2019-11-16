package com.example.androidhub.ui.home

import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.androidhub.R
import org.w3c.dom.Text

class DeviceFragment : Fragment() {

    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var nfcManager: NfcManager
    private lateinit var locationManager: LocationManager

    private var lastItem: MenuItem? = null

    private fun setupSystemManagers(activity: FragmentActivity) {
//        if (activity == null) {
//            Toast.makeText(activity, "no context found", Toast.LENGTH_SHORT).show()
//            return
//        }

        val app = activity.applicationContext
        wifiManager = app.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        nfcManager = app.getSystemService(Context.NFC_SERVICE) as NfcManager
        devicePolicyManager =
            app.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        locationManager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    lateinit var infoText: TextView
    lateinit var titleText: TextView
    lateinit var versionText: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        setupSystemManagers(activity!!)

        titleText = root.findViewById(R.id.text_title)
        infoText = root.findViewById(R.id.text_info)
        versionText = root.findViewById(R.id.version_text)
        val refreshButton: Button = root.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            refreshData()
        }
        return root
    }

    fun refreshData() {
        if (lastItem != null) {
            refreshData(lastItem!!)
        }
    }

    public fun refreshData(item: MenuItem) {
        lastItem = item
        val selectedItem = item.toString()
        titleText.text = selectedItem
        // Cellular Connection


        when (selectedItem) {
            "LTE" -> getLTE()
            "Bluetooth" -> getBLE()
            "NFC" -> getNFC()
            "VPN" -> getVPN()
            "GPS" -> getGPS()
            "Encryption" -> getEncryption()
            "AntiVirus" -> getAntiVirus()
            "MDM Status" -> getMdmStatus()
        }

        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        versionText.text = "Android SDK: $sdkVersion ($release)"
    }

    fun isManaged(): Boolean {
manag
        val admins = devicePolicyManager.getActiveAdmins();
        if (admins == null) return false;
        for (admin in admins) {
            val adminPackageName = admin.getPackageName();
            if (devicePolicyManager.isDeviceOwnerApp(adminPackageName)
                || devicePolicyManager.isProfileOwnerApp(adminPackageName)
            ) {
                return true;
            }
        }

        return false;
    }

    private fun getMdmStatus() {
        if (isManaged()) {
            infoText.text = "Device or current profile is currently managed"
        } else {
            infoText.text = "MDM currently disabled"
        }

    }

    private fun getNFC() {
        val nfcAdapter = nfcManager.defaultAdapter
        if (nfcAdapter != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && nfcAdapter.isSecureNfcEnabled) {
            infoText.setText("Secure NFC on")
        } else if (nfcAdapter != null && nfcAdapter.isEnabled) {
            infoText.setText("NFC on")
        } else {
            infoText.setText("NFC off")
        }
    }

    private fun getBLE() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter.isEnabled) {
            infoText.text = "Bluetooth on"
        } else {
            infoText.text = "Bluetooth off"
        }
        val pairedDevices = bluetoothAdapter.bondedDevices.map { x -> x.name }
        infoText.text =
            "${infoText.text} with ${pairedDevices.size} Paired Devices\n${pairedDevices.joinToString { "," }}"
    }

    private fun getAntiVirus() {
        val antivirusAppList = ArrayList<String>()
        infoText.setText("No antivirus software detected")
        // TODO: Get an approved anti virus list and verify if present.
        for (app in antivirusAppList) {
            //            try {
            //                packageManager.getPackageInfo(app, PackageManager.GET_ACTIVITIES)
            //                infoText.setText(app)
            //            } catch (e: PackageManager.NameNotFoundException) {
            //            }

        }
    }

    private fun getGPS() {
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsEnabled) {
            infoText.setText("GPS Enabled")
        } else {
            infoText.setText("GPS Not Enabled")
        }
    }

    private fun getEncryption() {
        val storageEncryptionStatus = devicePolicyManager.storageEncryptionStatus
        when (storageEncryptionStatus) {
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> {
                infoText.setText("Unsupported")
            }
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> {
                infoText.setText("Inactive")
            }
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING -> {
                infoText.setText("Activating")
            }
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> {
                infoText.setText("Active")
            }
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY -> {
                infoText.setText("Active Default Key")
            }
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER -> {
                infoText.setText("Active Per User")
            }
        }
    }

    private fun getVPN() {
        // VPN
        val networks: Array<Network> = connectivityManager.allNetworks
        infoText.setText("None")
        for (i in networks.indices) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(networks[i])
            if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                infoText.text = "${infoText.text}, ${networks[i]}"
            }
        }
    }

    private fun getWIFI(): Boolean {
        val networkList = wifiManager.scanResults
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null) {
            val ssid = wifiInfo.ssid
            if (networkList != null) {
                for (network in networkList) {
                    if (ssid == network.SSID) {
                        val capabilities = network.capabilities
                        infoText.setText(capabilities)
                        return true
                    }
                }
            }
        } else {
            infoText.setText("Not Connected")
        }
        return false
    }

    private fun getLTE() {
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null) {
            infoText.text = networkInfo.typeName + "  " + networkInfo.subtypeName
        } else {
            infoText.text = "Disconnected"
        }
    }
}