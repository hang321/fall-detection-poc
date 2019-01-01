package net.hang321.sample.app.falldetectionpoc;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.CompoundButton.OnCheckedChangeListener;
import static net.hang321.sample.app.falldetectionpoc.detectors.MovementDetector.MovementListener;
import static net.hang321.sample.app.falldetectionpoc.detectors.VerticalAccelerationDetector.VerticalAccelerationListener;

public class MainActivity extends AppCompatActivity
    implements OnCheckedChangeListener,  MovementListener, VerticalAccelerationListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final boolean DEBUG = true;

  private TextView textViewResult;
  private TextView textViewAccelerometerValueX, textViewAccelerometerValueY, textViewAccelerometerValueZ;
  private TextView textViewGeomagneticValueX, textViewGeomagneticValueY, textViewGeomagneticValueZ;
  private TextView textViewRotationValueX, textViewRotationValueY, textViewRotationValueZ;
  private SwitchCompat switchMovement, switchVerticalAcceleration;
  private SeekBar seekBarMovementThreshold, seekBarSecondsToStationary, seekBarTotalAccelerometerThreshold,
  //private SeekBar  seekBarSecondsToStationary, seekBarTotalAccelerometerThreshold,
  seekBarVerticalAccelerometerThreshold, seekBarTotalToVerticalRatio;
  private TextView textViewMovementThresholdValue, textViewSecondsToStationaryValue,
      textViewTotalAccelerometerThresholdValue, textViewVerticalAccelerometerThresholdValue,
      textViewTotalToVerticalRatioValue;

  // private RangeSeekBar seekBarMovementThreshold;

  private Handler handler;

  /// holding selector values, initialze as default value. // TODO store as preference and retrieve it later
  private float movementThreshold = 0.3f, totalAccelerometerThreshold = 17,
      verticalAccelerometerThreshold = 14, totalToVerticalRatio = 0.7f;
  private long secondsToStationary = 5000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

//    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//    setSupportActionBar(toolbar);


    // init
    FallDetection.getInstance().init(this);

    // UI
    handler = new Handler();
    textViewResult = (TextView) findViewById(R.id.textViewResult);

    textViewAccelerometerValueX = (TextView) findViewById(R.id.textViewAccelerometerValueX);
    textViewAccelerometerValueY = (TextView) findViewById(R.id.textViewAccelerometerValueY);
    textViewAccelerometerValueZ = (TextView) findViewById(R.id.textViewAccelerometerValueZ);

    textViewGeomagneticValueX = (TextView) findViewById(R.id.textViewGeomagneticValueX);
    textViewGeomagneticValueY = (TextView) findViewById(R.id.textViewGeomagneticValueY);
    textViewGeomagneticValueZ = (TextView) findViewById(R.id.textViewGeomagneticValueZ);

    textViewRotationValueX = (TextView) findViewById(R.id.textViewRotationValueX);
    textViewRotationValueY = (TextView) findViewById(R.id.textViewRotationValueY);
    textViewRotationValueZ = (TextView) findViewById(R.id.textViewRotationValueZ);

    switchMovement = (SwitchCompat) findViewById(R.id.switchMovement);
    switchMovement.setOnCheckedChangeListener(this);
    switchMovement.setChecked(false);

    switchVerticalAcceleration = (SwitchCompat) findViewById(R.id.switchVerticalAcceleration);
    switchVerticalAcceleration.setOnCheckedChangeListener(this);
    switchVerticalAcceleration.setChecked(false);

    textViewMovementThresholdValue = (TextView) findViewById(R.id.textViewMovementThresholdValue);
    textViewSecondsToStationaryValue = (TextView) findViewById(R.id.textViewSecondsToStationaryValue);
    textViewTotalAccelerometerThresholdValue = (TextView) findViewById(R.id.textViewTotalAccelerometerThresholdValue);
    textViewVerticalAccelerometerThresholdValue = (TextView) findViewById(R.id.textViewVerticalAccelerometerThresholdValue);
    textViewTotalToVerticalRatioValue = (TextView) findViewById(R.id.textViewTotalToVerticalRatioValue);

    // seekBarMovementThreshold = (RangeSeekBar) findViewById(R.id.seekBarMovementThreshold);
    seekBarMovementThreshold = (SeekBar) findViewById(R.id.seekBarMovementThreshold);
    seekBarMovementThreshold.setMax((int) ((1 - 0.1) / 0.1));  // (max - min) / step
    seekBarMovementThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        movementThreshold = (float) (0.1 + (progress * 0.1));   // min + (progress * step)
        textViewMovementThresholdValue.setText(String.format("%.1f", movementThreshold));
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    seekBarSecondsToStationary = (SeekBar) findViewById(R.id.seekBarSecondsToStationary);
    seekBarSecondsToStationary.setMax((60 - 1) / 1);
    seekBarSecondsToStationary.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int value = 1 + (progress * 1);
        textViewSecondsToStationaryValue.setText(String.valueOf(value));
        secondsToStationary = value * 1000; // ms to s
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    seekBarTotalAccelerometerThreshold  = (SeekBar) findViewById(R.id.seekBarTotalAccelerometerThreshold);
    seekBarTotalAccelerometerThreshold.setMax( (int) ((30 - 4) / 0.1));
    seekBarTotalAccelerometerThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        totalAccelerometerThreshold = (float) (4 + (progress * 0.1));
        textViewTotalAccelerometerThresholdValue.setText(String.format("%.1f", totalAccelerometerThreshold));
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    seekBarVerticalAccelerometerThreshold = (SeekBar) findViewById(R.id.seekBarVerticalAccelerometerThreshold);
    seekBarVerticalAccelerometerThreshold.setMax( (int) ((29 - 3) / 0.1));
    seekBarVerticalAccelerometerThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        verticalAccelerometerThreshold = (float) (3 + (progress * 0.1));
        textViewVerticalAccelerometerThresholdValue.setText(String.format("%.1f", verticalAccelerometerThreshold));
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    seekBarTotalToVerticalRatio = (SeekBar) findViewById(R.id.seekBarTotalToVerticalRatio);
    seekBarTotalToVerticalRatio.setMax( (int) ((2 - 0.1 )/ 0.1));
    seekBarTotalToVerticalRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        totalToVerticalRatio = (float) (0.1 + (progress * 0.1));
        textViewTotalToVerticalRatioValue.setText(String.format("%.1f", totalToVerticalRatio));
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // switched to individual detector. below will enable them when app launch
    // FallDetection fallDetection = FallDetection.getInstance();
    // fallDetection.startSensorDetection(this);



    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "start detection again if stopped by screen lock", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();

        FallDetection fallDetection = FallDetection.getInstance();
        // fallDetection.startSensorDetection(MainActivity.this); TODO (check changed listener?)
        switchMovement.setChecked(true);
        switchVerticalAcceleration.setChecked(true);
        Toast.makeText(MainActivity.this, "starting all detectors", Toast.LENGTH_SHORT).show();
      }
    });


  }

  @Override
  protected void onPause() {
    super.onPause();

    FallDetection.getInstance().stopAllSensorDetection();
    switchMovement.setChecked(false);
    switchVerticalAcceleration.setChecked(false);

    resetResultInView(textViewResult);
    Toast.makeText(this, "Stopping all detectors!", Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // release context !
    FallDetection.getInstance().stop();
  }

//  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//    // Inflate the menu; this adds items to the action bar if it is present.
//    getMenuInflater().inflate(R.menu.menu_main, menu);
//    return true;
//  }
//
//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//    // Handle action bar item clicks here. The action bar will
//    // automatically handle clicks on the Home/Up button, so long
//    // as you specify a parent activity in AndroidManifest.xml.
//    int id = item.getItemId();
//
//    //noinspection SimplifiableIfStatement
//    if (id == R.id.action_settings) {
//      return true;
//    }
//
//    return super.onOptionsItemSelected(item);
//  }

  @Override
  public void onCheckedChanged(CompoundButton switchbtn, boolean isChecked) {
    switch (switchbtn.getId()) {
      case R.id.switchMovement:
        if (isChecked) {
          // TODO get threshold from seekbar here !!!
          // float threshold = 0.3f;
          // long duration = 5000;
          // float threshold = (float) seekBarMovementThreshold.getSelectedMinValue();
          FallDetection.getInstance().startMovementDetection(movementThreshold, secondsToStationary, this);
        } else {
          FallDetection.getInstance().stopMovementDetection(this);
        }
        break;
      case R.id.switchVerticalAcceleration:
        if (isChecked) {
          FallDetection.getInstance().startFallDetection(totalAccelerometerThreshold,
              verticalAccelerometerThreshold, totalToVerticalRatio, this);
        } else {
          FallDetection.getInstance().stopFallDetection(this);
        }
        break;
      default:
        break;
    }
  }



  private void setResultTextView(final String text, final boolean realtime) {
    if (textViewResult != null) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          textViewResult.setText(text);
          textViewResult.setBackgroundColor(Color.YELLOW);
          if (!realtime) {
            resetResultInView(textViewResult);
          }
        }
      });

      if (DEBUG) {
        Log.i(TAG, text);
      }
    }
  }

  private void resetResultInView(final TextView txt) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        txt.setText("Result show here");
        txt.setBackgroundColor(Color.TRANSPARENT);
      }
    }, 3000);
  }


  // ====== individual methods for listeners ======


  @Override
  public void onMovement() {
    setResultTextView("Movement Detected!", false);
  }

  @Override
  public void onStationary() {
    setResultTextView("Device Stationary!", false);
  }

  @Override
  public void onUpdateAcceleration(float[] gravity, float delta) {
    textViewAccelerometerValueX.setText(String.valueOf(gravity[0]));
    textViewAccelerometerValueY.setText(String.valueOf(gravity[1]));
    textViewAccelerometerValueZ.setText(String.valueOf(gravity[2]));
  }

  @Override
  public void onUpdateRotation(float[] rotation) {
    textViewRotationValueX.setText(String.valueOf(rotation[0]));
    textViewRotationValueY.setText(String.valueOf(rotation[1]));
    textViewRotationValueZ.setText(String.valueOf(rotation[2]));
  }

  @Override
  public void onUpdateGeomagnetic(float[] geomagnetic) {
    textViewGeomagneticValueX.setText(String.valueOf(geomagnetic[0]));
    textViewGeomagneticValueY.setText(String.valueOf(geomagnetic[1]));
    textViewGeomagneticValueZ.setText(String.valueOf(geomagnetic[2]));
  }

  @Override
  public void onFallDetected() {
    setResultTextView("Vertical acceleration exceed threshold detected!", false);
  }
}
