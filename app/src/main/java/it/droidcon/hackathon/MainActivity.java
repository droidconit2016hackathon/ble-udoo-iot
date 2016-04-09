package it.droidcon.hackathon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.ByteBuffer.wrap;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById
    TextView connection_info_text;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    BluetoothGatt gatt;
    private BluetoothDevice mBluetoothDevice;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 15000;

    @AfterViews
    public void init() {
        initBluetooth();
        scanLeDevice();
    }


    private void initBluetooth() {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    private void scanLeDevice() {

        final ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                ScanRecord scanRecord = result.getScanRecord();
                if (mBluetoothDevice == null && scanRecord != null && scanRecord.getDeviceName() != null && scanRecord.getDeviceName().contains("A2FEB1")) {
                    mBluetoothDevice = result.getDevice();
                    connectGatt(mBluetoothDevice);
                }
            }
        };


        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Stops scanning after a pre-defined scan period.
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                bluetoothLeScanner.stopScan(callback);
            }
        }, SCAN_PERIOD);

        mScanning = true;

        bluetoothLeScanner.startScan(callback);

    }





    private void connectGatt(BluetoothDevice mBluetoothDevice) {

        mBluetoothDevice.connectGatt(MainActivity.this, true, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    setTextOnConnectionInfo("Connected with device: " + gatt.getDevice().getName());
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    setTextOnConnectionInfo("not connected");
                    Log.i("INFO", "Disconnected from GATT server.");
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                if (status == BluetoothGatt.GATT_SUCCESS) {


                    MainActivity.this.gatt = gatt;

                    BluetoothGattService iBlioService = gatt.getService(UUID.fromString("ee0c1000-8786-40ba-ab96-99b91ac981d8"));
                    BluetoothGattCharacteristic led1Characteristic = iBlioService.getCharacteristic(UUID.fromString("ee0c1012-8786-40ba-ab96-99b91ac981d8"));

                    led1Characteristic.setValue(10, BluetoothGattCharacteristic.FORMAT_UINT16, 0);


                    gatt.writeCharacteristic(led1Characteristic);


                    Log.i("INFO", "onServicesDiscovered received: " + status);
                }

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {

//                byte[] measureValue = characteristic.getValue();
//
//                int intValue = getValue(measureValue);
//                Log.i("VAL", String.valueOf(intValue));
//
//                //TODO do something
            }


        });

    }

    private int getValue(byte[] measureValue) {
        return wrap(measureValue).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    @UiThread
    protected void setTextOnConnectionInfo(String text) {
        connection_info_text.setText(text);


    }
}
