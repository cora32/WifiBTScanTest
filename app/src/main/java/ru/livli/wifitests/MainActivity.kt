package ru.livli.wifitests

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.jetbrains.anko.wifiManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        tryToScan()
//        btScan()
        btLeScan()
    }

    private fun btLeScan() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val callback = object : ScanCallback() {
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)

                    var res = ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        res += "advertisingSid: ${result?.advertisingSid}\n"
                        res += "dataStatus: ${result?.dataStatus}\n"
                        res += "txPower: ${result?.txPower}\n"
                        res += "primaryPhy: ${result?.primaryPhy}\n"
                        res += "secondaryPhy: ${result?.secondaryPhy}\n"
                        res += "periodicAdvertisingInterval: ${result?.periodicAdvertisingInterval}\n"
                        res += "isLegacy: ${result?.isLegacy}\n"
                        res += "isConnectable: ${result?.isConnectable}\n"
                    }
                    res += "device: ${result?.device}\n"
                    res += "rssi: ${result?.rssi}\n"
                    res += "advertiseFlags: ${result?.scanRecord?.advertiseFlags}\n"
                    res += "bytes: ${result?.scanRecord?.bytes}\n"
                    res += "deviceName: ${result?.scanRecord?.deviceName}\n"
                    res += "manufacturerSpecificData: ${result?.scanRecord?.manufacturerSpecificData}\n"
                    res += "serviceData: ${result?.scanRecord?.serviceData}\n"
                    res += "serviceUuids: ${result?.scanRecord?.serviceUuids}\n"
                    res += "txPowerLevel: ${result?.scanRecord?.txPowerLevel}\n"
                    res += "timestampNanos: ${result?.timestampNanos}\n"
//                    result?.scanRecord?.manufacturerSpecificData?.forEach { key, value ->
//                        res += "\t\t$key: $value\n"
//                    }
                    Log.e("---", res)
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                }
            }
            mBluetoothAdapter.bluetoothLeScanner.startScan(callback)
        }
    }

    private fun btScan() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                //Finding devices
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // Get the BluetoothDevice object from the Intent
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Add the name and address to an array adapter to show in a ListView
                    Log.e("---", "name: ${device.name} \naddress: ${device.address} \ntype: ${device.type} \ndeviceClass: ${device.bluetoothClass.deviceClass}  \nmajorDeviceClass: ${device.bluetoothClass.majorDeviceClass} \nbondState: ${device.bondState} \nuuids: ${device.uuids}")
//                    mArrayAdapter.add(device.name + "\n" + device.address)
                    val result = device.fetchUuidsWithSdp()
                    if (result) {

                    }
                } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                    val uuid = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    Log.e("\n\n---", " $uuid")
                    uuid?.forEach {
                        Log.e("---", " $it")
                    }
                    Handler().postDelayed({ mBluetoothAdapter.startDiscovery() }, 3000L)
                }
            }
        }
        registerReceiver(mReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND).apply { addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) })
        if (!mBluetoothAdapter.isEnabled)
            mBluetoothAdapter.enable()
        Handler().postDelayed({ mBluetoothAdapter.startDiscovery() }, 3000L)

    }

    private fun tryToScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                scan()
            }
        } else {
            scan()
        }
    }

    private fun scan() {
//        val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//        startActivity(myIntent)
        if (wifiManager.isWifiEnabled) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        p1?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
                    } else {
                        true
                    }
                    if (success) {
//                        unregisterReceiver(this)
                        val result = wifiManager.scanResults
                        result?.forEach {
                            Log.e("---", " res $it")
                        }
                    }
                }

            }
            registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            wifiManager.startScan()
        } else {
//            wifiManager.isWifiEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED)
                        return
                }
                scan()
            }
        }
    }
}
