package net.hang321.sample.app.falldetectionpoc.detectors;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Random;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_ROTATION_VECTOR;

/**
 * Created by steve on 6/06/17.
 */

public class VerticalAccelerationDetector extends SensorDetector {

  private static final String TAG = VerticalAccelerationDetector.class.getSimpleName();

  private final VerticalAccelerationListener verticalAccelerationListener;

  private final float totalAccelerometerThreshold; // 11, 12, 13, 14
  private final float verticalAccelerometerThreshold;
  private final float accelerometerComparisonThreshold;

  private float currentAcceleration = SensorManager.GRAVITY_EARTH;

  private float[] gravity;
  private float[] geomagnetic;
  private float[] rotation;

  /**
   * default threshold
   * @param verticalAccelerationListener
   */
  public VerticalAccelerationDetector(VerticalAccelerationListener verticalAccelerationListener) {
    // this(11, 9, 0.5f, fallListener);
    this(17, 14, 0.7f, verticalAccelerationListener);
  }

  /**
   *
   * @param totalAccelerometerThreshold
   * @param verticalAccelerometerThreshold
   * @param accelerometerComparisonThreshold
   * @param verticalAccelerationListener
   */
  public VerticalAccelerationDetector(float totalAccelerometerThreshold, float verticalAccelerometerThreshold,
                      float accelerometerComparisonThreshold, VerticalAccelerationListener verticalAccelerationListener) {
    super(TYPE_ACCELEROMETER, TYPE_MAGNETIC_FIELD, TYPE_ROTATION_VECTOR);
    this.verticalAccelerationListener = verticalAccelerationListener;
    this.totalAccelerometerThreshold = totalAccelerometerThreshold;
    this.verticalAccelerometerThreshold = verticalAccelerometerThreshold;
    this.accelerometerComparisonThreshold = accelerometerComparisonThreshold;
    Log.i(TAG, String.format("starting %s with total-acc %.2f, vertical-acc: %.2f, ratio: %.2f", TAG,
        totalAccelerometerThreshold, verticalAccelerometerThreshold, accelerometerComparisonThreshold));
  }

  @Override
  void onSensorEvent(SensorEvent sensorEvent) {
    if (sensorEvent.sensor.getType() == TYPE_ACCELEROMETER) {
      gravity = sensorEvent.values;
    }
    if (sensorEvent.sensor.getType() == TYPE_MAGNETIC_FIELD) {
      geomagnetic = sensorEvent.values;
    }
    if (sensorEvent.sensor.getType() == TYPE_ROTATION_VECTOR) {
      rotation = sensorEvent.values;
    }
    if (gravity == null || geomagnetic == null || rotation == null) {
      // Log.v(TAG, "skip, gravity is " + gravity==null?"null":"having value" + ", geomaenetic is : " + geomagnetic == null?"null":"having value");
      return; // nothing to do
    }

    float[] R = new float[9];
    float[] I = new float[9];

    // boolean success =
    // if (success) {
    SensorManager.getRotationMatrix(R, I, rotation, geomagnetic);
    float[] orientationData = new float[3];
    SensorManager.getOrientation(R, orientationData);
    float thetaY = orientationData[2];
    float thetaZ = orientationData[0];

    float x = gravity[0];
    float y = gravity[1];
    float z = gravity[2];

    boolean isFall = calculateThreshold(x, y, z, thetaY, thetaZ);
    if (isFall) {
      verticalAccelerationListener.onFallDetected();
    }
//    }

    // display info to UI
    if (gravity != null) {
      float lastAcceleration = currentAcceleration;
      currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
      float delta = Math.abs(currentAcceleration - lastAcceleration);
      verticalAccelerationListener.onUpdateAcceleration(gravity, delta);
    }
    if (rotation != null) {
      verticalAccelerationListener.onUpdateRotation(rotation);
    }
    if (geomagnetic != null) {
      verticalAccelerationListener.onUpdateGeomagnetic(geomagnetic);
    }

  }

  private boolean calculateThreshold(float x, float y, float z, float thetaY, float thetaZ) {
    double totalAcceleration = Math.abs(Math.sqrt(x * x + y * y + z * z));
    double verticalAcceleration = Math.abs(x * Math.sin(thetaZ) +
        y * Math.sin(thetaY) -
        z * Math.cos(thetaY) * Math.cos(thetaZ));

    // TODO comment it
    // too verbose to log ~120 lines every seconds, only 10% chance it wil be logged
    Random random = new Random();
    int randomInt = random.nextInt(10);
    if (randomInt > 7) {
      Log.d(TAG, String.format("totalAcc: %.4f, vertAcc: %.4f, ratio: %.2f || x: %.4f, y: %.4f, z: %.4f, thetaY: %.4f, thetaZ: %.4f",
          totalAcceleration, verticalAcceleration, (verticalAcceleration/totalAcceleration), x, y, z, thetaY, thetaZ));
    }

    if (totalAcceleration >= totalAccelerometerThreshold &&
        verticalAcceleration >= verticalAccelerometerThreshold &&
        (verticalAcceleration / totalAcceleration)  >= accelerometerComparisonThreshold) {

      Log.w(TAG, String.format("totalAcc: %.4f, vertAcc: %.4f, ratio: %.2f || x: %.4f, y: %.4f, z: %.4f, thetaY: %.4f, thetaZ: %.4f",
          totalAcceleration, verticalAcceleration, (verticalAcceleration/totalAcceleration), x, y, z, thetaY, thetaZ));

      return true;
    }
    return false;
  }


  public interface VerticalAccelerationListener {

    void onFallDetected();

    //void onTimerExpired();

    //void onMovement();

    void onUpdateAcceleration(float[] gravity, float delta);

    void onUpdateRotation(float[] rotation);

    void onUpdateGeomagnetic(float[] geomagnetic);

  }
}
