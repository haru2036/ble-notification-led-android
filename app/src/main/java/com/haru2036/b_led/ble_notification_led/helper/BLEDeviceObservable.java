package com.haru2036.b_led.ble_notification_led.helper;

import android.bluetooth.BluetoothDevice;
import android.databinding.BaseObservable;
import android.databinding.Bindable;


/**
 * Created by haru2036 on 16/04/30.
 */
public class BLEDeviceObservable extends BaseObservable{
    private BluetoothDevice device;

    public BLEDeviceObservable(BluetoothDevice device){
        this.device = device;
    }

    @Bindable
    public String getName(){
        return device.getName();
    }

    @Bindable
    public String getAddress(){
        return device.getAddress();
    }

}
