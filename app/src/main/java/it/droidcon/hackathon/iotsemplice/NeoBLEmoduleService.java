package it.droidcon.hackathon.iotsemplice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import it.droidcon.hackathon.MainActivity;

/**
 * Created by fditrani on 09/04/16.
 */
@EBean
public class NeoBLEmoduleService {


    @RootContext
    Context context;

    BluetoothGatt gatt;
    BluetoothGattCharacteristic getAccDataCharacteristic;
    boolean connected;

//    public boolean lightOnLed(int seconds){
//        if (led1Characteristic != null && connected) {
//            led1Characteristic.setValue(seconds, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
//            gatt.writeCharacteristic(led1Characteristic);
//            return true;
//        } else {
//            return false;
//        }
//    }

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

            private void waitMilliseconds() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                if (status == BluetoothGatt.GATT_SUCCESS) {


                    BluetoothGattService neoBleService = gatt.getService(UUID.fromString("d7728ceb-79c6-452f-994c-9829da1a4229"));
                    getAccDataCharacteristic = neoBleService.getCharacteristic(UUID.fromString("d7729684-79c6-452f-994c-9829da1a4229"));
                    BluetoothGattCharacteristic accPeriodCharacteristic = neoBleService.getCharacteristic(UUID.fromString("d7729684-79c6-452f-994c-9829da1a4229"));
                    BluetoothGattCharacteristic enableCharacteristic = neoBleService.getCharacteristic(UUID.fromString("d7729684-79c6-452f-994c-9829da1a4229"));


                    //enable accelerometer/emitter
                    byte[] enabledValue = new byte[1];
                    enabledValue[0] = 1;
                    enableCharacteristic.setValue(enabledValue);
                    gatt.writeCharacteristic(enableCharacteristic);
                    waitMilliseconds();
                    //emit data every 100 milliseconds byte value = (byte) period;
                    byte[] periodValue = new byte[1];
                    periodValue[0] = 10;
                    accPeriodCharacteristic.setValue(periodValue);
                    gatt.writeCharacteristic(accPeriodCharacteristic);

                    waitMilliseconds();

                    gatt.setCharacteristicNotification(getAccDataCharacteristic, true);


                    BluetoothGattDescriptor descriptor = enableCharacteristic.getDescriptors().get(1);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    waitMilliseconds();

                    NeoBLEmoduleService.this.gatt = gatt;

                    if (getAccDataCharacteristic != null) {
                        ((MainActivity) context).setTextOnNeoBleConnectionInfo("Characteristic found on NEO BLE module!");
                        connected = true;
                    }
                }


            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic, int status) {

                byte[] measureValue = characteristic.getValue();

                int intValue = getValue(measureValue);

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {

                byte[] measureValue = characteristic.getValue();

                int intValue = getValue(measureValue);

            }


        });

    }



    private int getValue(byte[] measureValue) {
        return ByteBuffer.wrap(measureValue).order(ByteOrder.BIG_ENDIAN).getInt();
    }
}
