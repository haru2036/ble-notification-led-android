package com.haru2036.b_led.ble_notification_led.service

import android.bluetooth.*
import android.content.Context
import android.graphics.Color
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.haru2036.b_led.ble_notification_led.helper.ColorHelper
import java.util.*

/**
 * Created by haru2036 on 16/08/05.
 */
class BLENotificationSerice : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sendNotificationAsColor()

    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sendNotificationAsColor()

    }

    fun sendNotificationAsColor(){
        val notifications = activeNotifications.filter { x -> x.packageName != "android" && x.packageName != "com.lastpass.lpandroid"}

        if(notifications.size == 0){
            Log.d("received notification", "0")
            sendColor("#000000")
        }else {
            val notification = notifications[0]
            Log.d("received notification", notification.packageName)
            val color = String.format("#%06X", (0xffffff.and(notification.notification.color)))
            sendColor(color)
        }

    }


    fun sendColor(color: String){

        val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt?.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                val service = gatt?.getService(UUID.fromString("F1D8C0D1-4A4F-4B43-9A5B-7C59BD85EE57"))
                val rgbCharacteristic = service?.getCharacteristic(UUID.fromString("F1D8C0D1-4A4F-4B43-9A5B-7C59BD85EE59"))

                val colorArr = ColorHelper.hexStringToByteArray(color.substring(1, 7))
                Log.d("color", color)
                Log.d("writevalue r", colorArr[0].toString())
                Log.d("writevalue g", colorArr[1].toString())
                Log.d("writevalue b", colorArr[2].toString())

                rgbCharacteristic?.value = colorArr
                gatt?.writeCharacteristic(rgbCharacteristic)

            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                gatt?.disconnect()
            }
        }

        bluetoothManager.adapter.getRemoteDevice("00:1B:DC:05:60:37").connectGatt(applicationContext, true, gattCallback)

    }
}
