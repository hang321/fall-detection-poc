package net.hang321.sample.app.falldetectionpoc.detectors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by steve on 6/06/17.
 */

public abstract class SensorDetector implements SensorEventListener {

  private final int[] sensorTypes;

  public SensorDetector(int... sensorTypes) {
    this.sensorTypes = sensorTypes;
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    // only call when event belongs to registered types
    for (int sensorType : sensorTypes) {
      if (sensorEvent.sensor.getType() == sensorType) {
        onSensorEvent(sensorEvent);
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  void onSensorEvent(SensorEvent sensorEvent) { }

  public int[] getSensorTypes() {
    return sensorTypes;
  }


}
