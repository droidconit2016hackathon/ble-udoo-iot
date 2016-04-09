package it.droidcon.hackathon.iotsemplice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.nio.ByteOrder;
import java.util.UUID;

import it.droidcon.hackathon.MainActivity;

import static java.nio.ByteBuffer.wrap;

/**
 * Created by fditrani on 09/04/16.
 */
@EBean
public class IblioService {

    @RootContext
    Context context;

    BluetoothGatt gatt;
    BluetoothGattCharacteristic led1Characteristic;
    boolean connected;

    public boolean lightOnLed(int seconds){
        if (led1Characteristic != null && connected) {
            led1Characteristic.setValue(seconds, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            gatt.writeCharacteristic(led1Characteristic);
            return true;
        } else {
            return false;
        }
    }

    public void connectGatt(BluetoothDevice mBluetoothDevice) {

        mBluetoothDevice.connectGatt(context, true, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    ((MainActivity) context).setTextOnConnectionInfo("Connected with device: " + gatt.getDevice().getName());
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    ((MainActivity) context).setTextOnConnectionInfo("not connected");
                    Log.i("INFO", "Disconnected from GATT server.");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                if (status == BluetoothGatt.GATT_SUCCESS) {


                    BluetoothGattService iBlioService = gatt.getService(UUID.fromString("ee0c1000-8786-40ba-ab96-99b91ac981d8"));
                    led1Characteristic = iBlioService.getCharacteristic(UUID.fromString("ee0c1012-8786-40ba-ab96-99b91ac981d8"));


                    IblioService.this.gatt = gatt;

                    if (led1Characteristic != null) {
                        ((MainActivity) context).setTextIblioServiceInfo("Characteristic found on iBlio!");
                        connected = true;
                    }
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

//    private int getValue(byte[] measureValue) {
//        return wrap(measureValue).order(ByteOrder.BIG_ENDIAN).getInt();
//    }

}
