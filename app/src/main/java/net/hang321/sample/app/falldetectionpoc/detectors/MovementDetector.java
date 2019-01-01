package net.hang321.sample.app.falldetectionpoc.detectors;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

/**
 * Simple detector for movement
 *
 * Created by steve on 6/06/17.
 */

public class MovementDetector extends SensorDetector {

  private static final String TAG = MovementDetector.class.getSimpleName();

  private final MovementListener movementListener;
  private final float threshold;
  private final long durationBeforeStationaryDeclaration;

  private float currentAcceleration = SensorManager.GRAVITY_EARTH;
  private long lastMovementDetectedTime = System.currentTimeMillis();
  private boolean isMoving = false;

  /**
   * Instantiates a default Movement detector.
   * @param movementListener
   */
  public MovementDetector(MovementListener movementListener) {
    this(0.3f, 5000, movementListener);
  }

  /**
   *
   * @param threshold
   * @param durationBeforeStationaryDeclaration time before declaring stationary
   * @param movementListener
   */
  public MovementDetector(float threshold, long durationBeforeStationaryDeclaration, MovementListener movementListener) {
    super(TYPE_ACCELEROMETER);
    this.movementListener = movementListener;
    this.threshold = threshold;
    this.durationBeforeStationaryDeclaration = durationBeforeStationaryDeclaration;
    Log.i(TAG, String.format("starting %s with threshold %.2f, duration: %d", TAG,
        threshold, durationBeforeStationaryDeclaration));
  }

  // logic to detect onMovement or onStationary
  @Override
  protected void onSensorEvent(SensorEvent sensorEvent) {
    // https://developer.android.com/reference/android/hardware/SensorEvent.html#values
    float x = sensorEvent.values[0];
    float y = sensorEvent.values[1];
    float z = sensorEvent.values[2];

    /* Logic:
      If there is no external force on the device, vector sum of accelerometer sensor values
      will be only gravity. If there is a change in vector sum of gravity, then there is a force.
      If this force is significant, you can assume device is moving.
      If vector sum is equal to gravity with +/- threshold its stable lying on table.
     */
    float lastAcceleration = currentAcceleration;
    currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
    float delta = Math.abs(currentAcceleration - lastAcceleration);

    if (delta > threshold) {
      lastMovementDetectedTime = System.currentTimeMillis();
      isMoving = true;
      movementListener.onMovement();
    } else {
      long timeDelta = (System.currentTimeMillis() - lastMovementDetectedTime);
      if (isMoving && timeDelta > durationBeforeStationaryDeclaration) {
        isMoving = false;
        movementListener.onStationary();
      }
    }

    movementListener.onUpdateAcceleration(sensorEvent.values, delta);
  }

  public interface MovementListener {
    void onMovement();
    void onStationary();

    // common interface ??
    void onUpdateAcceleration(float[] gravity, float delta);
  }

}
