package com.mateusz.frontend

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import okhttp3.internal.and
import java.util.UUID

class HeartRateManager(private val context: Context) {
    private val HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb"
    private val HEART_RATE_MEASUREMENT_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private val handler = Handler(Looper.getMainLooper())

    private var onHeartRateUpdate: ((Int) -> Unit)? = null

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun startHeartRateMonitoring(onHeartRate: (Int) -> Unit) {
        onHeartRateUpdate = onHeartRate
        startScan()
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (isScanning) return

        if (!hasRequiredPermissions()) {
            return
        }

        isScanning = true
        try {
            bluetoothAdapter?.bluetoothLeScanner?.let { scanner ->
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                val filter = ScanFilter.Builder()
                    .setDeviceName("InfiniTime")
                    .build()

                handler.postDelayed({ stopScan() }, 10000) // Stop scanning after 10 seconds

                scanner.startScan(listOf(filter), settings, scanCallback)
            }
        } catch (e: SecurityException) {
            isScanning = false
            // Handle security exception
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (!isScanning) return

        if (!hasRequiredPermissions()) {
            return
        }

        isScanning = false
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Handle security exception
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            stopScan()
            connectToDevice(result.device)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (!hasRequiredPermissions()) {
            return
        }

        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: SecurityException) {
            // Handle security exception
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (!hasRequiredPermissions()) {
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                try {
                    gatt.discoverServices()
                } catch (e: SecurityException) {
                    // Handle security exception
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (!hasRequiredPermissions()) {
                return
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    val heartRateService = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID))
                    val heartRateChar = heartRateService?.getCharacteristic(
                        UUID.fromString(HEART_RATE_MEASUREMENT_CHAR_UUID)
                    )

                    if (heartRateChar != null) {
                        gatt.setCharacteristicNotification(heartRateChar, true)
                    }
                } catch (e: SecurityException) {
                    // Handle security exception
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == UUID.fromString(HEART_RATE_MEASUREMENT_CHAR_UUID)) {
                val flag = value[0]
                val format = if (flag and 0x01 != 0) BluetoothGattCharacteristic.FORMAT_UINT16
                else BluetoothGattCharacteristic.FORMAT_UINT8
                val heartRate = when (format) {
                    BluetoothGattCharacteristic.FORMAT_UINT8 -> value[1].toInt()
                    BluetoothGattCharacteristic.FORMAT_UINT16 -> (value[2].toInt() shl 8) + value[1]
                    else -> 0
                }

                handler.post {
                    onHeartRateUpdate?.invoke(heartRate)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopHeartRateMonitoring() {
        if (!hasRequiredPermissions()) {
            return
        }

        try {
            bluetoothGatt?.close()
            bluetoothGatt = null
            onHeartRateUpdate = null
        } catch (e: SecurityException) {
            // Handle security exception
        }
    }
}