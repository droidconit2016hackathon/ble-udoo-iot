package it.droidcon.hackathon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.udoo.udooblulib.manager.UdooBluManager;

import it.droidcon.hackathon.services.IblioService;
import it.droidcon.hackathon.services.NeoBLEmoduleService;
import java.util.Collection;



@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    @ViewById
    TextView connection_info_text;

    @ViewById
    Button light_on;

    @ViewById
    Button reset_button;

    @ViewById
    TextView iblioservice_info;

    @ViewById
    TextView neo_bluetooth_info;

    @ViewById
    TextView accelerometer_info;

    @ViewById
    TextView beacon_distance_info;

    @ViewById
    TextView fall_info;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;





    @Bean
    IblioService iblioService;

    @Bean
    NeoBLEmoduleService neoBLEmoduleService;

    private static final long SCAN_PERIOD = 25000;

    private BluetoothDevice iBlioBluetoothDevice;

    private BluetoothDevice udooBluetoothDevice;

    private UdooBluManager mUdooBluManager;

    private BeaconManager beaconManager;

    @AfterViews
    public void init() {
        mUdooBluManager = new UdooBluManager(this);
        initBluetooth();
        scanLeDevice();
    }


    @Click(R.id.light_on)
    void turnLightOn() {
        iblioService.lightOnLed(5);
    }

    @Click(R.id.reset_button)
    void resetFallenInfo() {
        setFallenInfo("Listening...");
    }


    private void initBluetooth() {
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
                } else if (udooBluetoothDevice == null && scanRecord != null && scanRecord.getDeviceName() != null && scanRecord.getDeviceName().contains("CC2650")) {
                    setTextOnNeoBleConnectionInfo("NEO BLE device found!");
                    udooBluetoothDevice = result.getDevice();
                    neoBLEmoduleService.connectNeoBleDevice(mUdooBluManager, udooBluetoothDevice.getAddress());
                }
            }
        };


        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();
        mHandler.postDelayed(() -> {
            bluetoothLeScanner.stopScan(callback);
        }, SCAN_PERIOD);

        bluetoothLeScanner.startScan(callback);

    }

    private void initBeacons() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // proprietary ibeacons - m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24
        // proprietary kontakt - m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25
        // proprietary blueup - m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @UiThread
    public void setTextIblioServiceInfo(String text) {
        iblioservice_info.setText(text);
    }


    @UiThread
    public void setTextOnConnectionInfo(String text) {
        connection_info_text.setText(text);
    }

    @UiThread
    public void setTextOnNeoBleConnectionInfo(String text) {
        neo_bluetooth_info.setText(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(getClass().getSimpleName(), "connect beacon");
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.i(getClass().getSimpleName(), "scan range");
                if (beacons.size() > 0) {
                    setTextOnBeaconDistance(String.valueOf(beacons.iterator().next().getDistance()));
                    Log.i(getClass().getSimpleName(), "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                } else {
                    //setTextOnBeaconDistance("none");
                }

            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(getClass().getSimpleName(), "onBeaconServiceConnect", e);
        }
    }

    @UiThread
    public void setTextOnBeaconDistance(String text) {
        beacon_distance_info.setText(text);
    }

    @UiThread
    public void setAccelerometerInfo(String text) {
          accelerometer_info.setText(text);
    }


    @UiThread
    public void setFallenInfo(String text) {
        fall_info.setText(text);
    }



}
