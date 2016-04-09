package it.droidcon.hackathon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import it.droidcon.hackathon.iotsemplice.IblioService;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById
    TextView connection_info_text;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    BluetoothGatt gatt;

    @ViewById
    Button light_on;

    @Bean
    IblioService iblioService;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 15000;

    private BluetoothDevice iBlioBluetoothDevice;

    private BluetoothDevice udooBluetoothDevice;

    @AfterViews
    public void init() {
        initBluetooth();
        scanLeDevice();
    }


    @Click(R.id.light_on)
    void turnLightOn(){
        iblioService.lightOnLed(5);
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
                if (iBlioBluetoothDevice == null && scanRecord != null && scanRecord.getDeviceName() != null && scanRecord.getDeviceName().contains("A2FEB1")) {
                    iBlioBluetoothDevice = result.getDevice();
                    iblioService.connectGatt(iBlioBluetoothDevice);
                } else if (udooBluetoothDevice == null && scanRecord != null && scanRecord.getDeviceName() != null && scanRecord.getDeviceName().contains("CC2650")){
                    udooBluetoothDevice = result.getDevice();
                }
            }
        };


        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Stops scanning after a pre-defined scan period.
        mHandler = new Handler();
        mHandler.postDelayed(() -> {
            mScanning = false;
            bluetoothLeScanner.stopScan(callback);
        }, SCAN_PERIOD);

        mScanning = true;

        bluetoothLeScanner.startScan(callback);

    }






    @UiThread
    public void setTextOnConnectionInfo(String text) {
        connection_info_text.setText(text);


    }
}
