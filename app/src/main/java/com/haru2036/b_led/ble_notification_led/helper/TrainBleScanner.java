package com.haru2036.b_led.ble_notification_led.helper;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class TrainBleScanner implements Observable.OnSubscribe<ScanResult>{
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBLEScanner;
    List<ScanFilter> scanFilters;
    ScanCallback mScanCallback;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TrainBleScanner(Context context){
        BluetoothManager bluetoothManager = (BluetoothManager) context.
                getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public void setFilters(List<ScanFilter> filters){
        scanFilters = filters;
    }

    public void scan(final Subscriber<? super ScanResult> subscriber){
        final Subscription btSubscription = Observable.create(this).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                mBLEScanner.stopScan(mScanCallback);
                Log.d("scan", "stop");
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        Observable.timer(10, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                subscriber.onCompleted();
                btSubscription.unsubscribe();
            }
        });
    }

    @Override
    public void call(final Subscriber<? super ScanResult> subscriber) {
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                subscriber.onError(new Exception(Integer.toString(errorCode)));
                Log.d("scan", "failed");
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d("scanned", "onScanResult");
                if(result != null && result.getDevice() != null) {
                    Log.d("scanned", result.getDevice().getAddress());
                    subscriber.onNext(result);
                }
            }
        };
        mBLEScanner.startScan(scanFilters, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build(), mScanCallback);
        Log.d("scan","started");
    }
}
