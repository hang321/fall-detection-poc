package net.hang321.sample.app.falldetectionpoc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import net.hang321.sample.app.falldetectionpoc.detectors.MovementDetector;
import net.hang321.sample.app.falldetectionpoc.detectors.SensorDetector;
import net.hang321.sample.app.falldetectionpoc.detectors.VerticalAccelerationDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import static net.hang321.sample.app.falldetectionpoc.detectors.MovementDetector.MovementListener;
import static net.hang321.sample.app.falldetectionpoc.detectors.VerticalAccelerationDetector.VerticalAccelerationListener;

/**
 * Created by steve on 7/06/17.
 */

public class FallDetection {

  private static final String TAG = FallDetection.class.getSimpleName();

  private int samplingPeriod = SensorManager.SENSOR_DELAY_NORMAL;

  /**
   * Map from any of default listeners to SensorDetectors created by those listeners.
   *
   * This map is needed to hold reference to all started detections <strong>NOT</strong>
   * through {@link FallDetection#startSensorDetection(SensorDetector, Object)}, because the last one
   * passes task to hold reference of {@link SensorDetector sensorDetector} to the client
   */
  private final Map<Object, SensorDetector> registeredDefaultSensorsMap = new HashMap<>();

  private SensorManager sensorManager;


  // private static FallDetection instance;

  public FallDetection() {
    // instance = this;
  }

  public static FallDetection getInstance() {
    // return instance;
    return LazyHolder.INSTANCE;
  }

  /**
   * init library
   * @param context
   */
  public void init(Context context) {
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
  }

  public void init(Context context, int samplingPeriod) {
    init(context);
    this.samplingPeriod = samplingPeriod;
  }

  public void stop() {
    this.sensorManager = null;
  }

  // start all default detector, now split to individual
//  public void startSensorDetection(Object clientListener) {
//    // startMovementDetection((MovementListener) clientListener);
//    startFallDetection((FallListener) clientListener);
//  }


  private void startSensorDetection(SensorDetector detector, Object clientListener) {
    if (!registeredDefaultSensorsMap.containsKey(clientListener)) {
      registeredDefaultSensorsMap.put(clientListener, detector);

      final Collection<Sensor> sensors = convertTypesToSensors(detector.getSensorTypes());
      if (!sensors.isEmpty()) {
        for (Sensor sensor : sensors) {
          sensorManager.registerListener(detector, sensor, samplingPeriod);
        }
      }
    }
  }
  /** find out Android sensor by types. return empty collection if one of sensor type does not have available sensor */
  private Collection<Sensor> convertTypesToSensors(int... sensorTypes) {
    Collection<Sensor> sensors = new ArrayList<>();
    if (sensorManager != null) {
      for (int sensorType : sensorTypes) {
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if (sensor == null) {
          return Collections.emptySet();
        }
        sensors.add(sensor);
      }
    }
    return sensors;
  }



  public void stopAllSensorDetection() {
    Set<Object> listeners = new HashSet<>(registeredDefaultSensorsMap.keySet());
    for (Object listener : listeners) {
      stopSensorDetection(listener);
    }
  }

  private void stopSensorDetection(Object clientListener) {
    SensorDetector detector = registeredDefaultSensorsMap.remove(clientListener);
    if (detector != null && sensorManager != null) {
      sensorManager.unregisterListener(detector);
      Log.d(TAG, "unregistered sensor detector " + detector.getClass().getSimpleName());
    }
  }




  // individual sensor detector
  public void startMovementDetection(float threshold, long durationBeforeStationaryDeclaration, MovementListener movementListener) {
    startSensorDetection(new MovementDetector(threshold, durationBeforeStationaryDeclaration,movementListener), movementListener);
  }

  public void stopMovementDetection(MovementListener movementListener) {
    stopSensorDetection(movementListener);
  }


  public void startFallDetection(float totalAccelerometerThreshold, float verticalAccelerometerThreshold,
                                 float accelerometerComparisonThreshold, VerticalAccelerationListener verticalAccelerationListener) {
    startSensorDetection(
        new VerticalAccelerationDetector(totalAccelerometerThreshold, verticalAccelerometerThreshold,
            accelerometerComparisonThreshold, verticalAccelerationListener),
        verticalAccelerationListener);
  }

  public void stopFallDetection(VerticalAccelerationListener verticalAccelerationListener) {
    stopSensorDetection(verticalAccelerationListener);
  }


  private static class LazyHolder {
    private static final FallDetection INSTANCE = new FallDetection();
  }

}
