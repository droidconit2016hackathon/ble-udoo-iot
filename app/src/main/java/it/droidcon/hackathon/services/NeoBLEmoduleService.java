package it.droidcon.hackathon.services;

import android.content.Context;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.udoo.udooblulib.interfaces.IBleDeviceListener;
import org.udoo.udooblulib.interfaces.OnCharacteristicsListener;
import org.udoo.udooblulib.manager.UdooBluManager;
import org.udoo.udooblulib.sensor.UDOOBLESensor;
import org.udoo.udooblulib.utils.Point3D;

import it.droidcon.hackathon.MainActivity;

/**
 * Created by fditrani on 09/04/16.
 */
@EBean
public class NeoBLEmoduleService {


    @RootContext
    Context context;

    public void connectNeoBleDevice(UdooBluManager mUdooBluManager, String address) {

        mUdooBluManager.connect(address, new IBleDeviceListener() {
            @Override
            public void onDeviceConnected() {
                mUdooBluManager.discoveryServices(address);
            }

            @Override
            public void onServicesDiscoveryCompleted() {
                mUdooBluManager.enableSensor(address, UDOOBLESensor.ACCELEROMETER, true);
                mUdooBluManager.setNotificationPeriod(address, UDOOBLESensor.ACCELEROMETER);

                mUdooBluManager.enableNotification(address, true, UDOOBLESensor.ACCELEROMETER, new OnCharacteristicsListener() {


                    @Override
                    public void onCharacteristicsRead(String uuidStr, byte[] value, int status) {

                    }

                    @Override
                    public void onCharacteristicChanged(String uuidStr, byte[] rawValue) {

                        Point3D point3D = UDOOBLESensor.ACCELEROMETER.convert(rawValue);
                        if (point3D != null) {
                            float[] accelerometer_values = point3D.toFloatArray();
                            String xValue = String.valueOf(accelerometer_values[0]);
                            String yValue = String.valueOf(accelerometer_values[1]);
                            String zValue = String.valueOf(accelerometer_values[2]);
                            float[] gravity = new float[3];

                            //Kalman filter
                            float kFilteringFactor=0.6f;

                            gravity[0] = (accelerometer_values[0] * kFilteringFactor) + (gravity[0] * (1.0f - kFilteringFactor));
                            gravity[1] = (accelerometer_values[1] * kFilteringFactor) + (gravity[1] * (1.0f - kFilteringFactor));
                            gravity[2] = (accelerometer_values[2] * kFilteringFactor) + (gravity[2] * (1.0f - kFilteringFactor));
                            float[] linear_acceleration = new float[3];
                            linear_acceleration[0] = (accelerometer_values[0] - gravity[0]);
                            linear_acceleration[1] = (accelerometer_values[1] - gravity[1]);
                            linear_acceleration[2] = (accelerometer_values[2] - gravity[2]);

                            float magnitude;
                            magnitude = (float)Math.sqrt(linear_acceleration[0]*linear_acceleration[0]+linear_acceleration[1]*linear_acceleration[1]+linear_acceleration[2]*linear_acceleration[2]);
                            magnitude = Math.abs(magnitude);
                            if (fallenMagnitude(magnitude)){
                                ((MainActivity)context).setFallenInfo("Caduta rilevata!!");
                            }
                            ((MainActivity)context).setAccelerometerInfo(
                                    "x: " + xValue + "\ny: " + yValue + "\nz: " + zValue + "\nmagnitude: " + magnitude);

                        }


                    }
                });
            }

            @Override
            public void onDeviceDisconnect() {

            }

        });
    }

    private boolean fallenMagnitude(float magnitude) {
        return (int)magnitude == 0;
    }
}
