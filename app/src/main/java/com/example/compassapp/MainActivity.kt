package com.example.compassapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.InternalCoroutinesApi
import kotlin.math.*

class MainActivity : AppCompatActivity(), SensorEventListener {


    // Declare UI elements
    lateinit var needle: ImageView
    lateinit var deg: TextView
    lateinit var dir: TextView

    // Declare SensorManager and sensor readings
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    // Declare rotation matrix and orientation angles
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get SensorManager instance
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get UI elements by id
        needle = findViewById(R.id.needle)
        deg = findViewById(R.id.degree_textView)
        dir = findViewById(R.id.dir_textView)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onResume() {
        super.onResume()

        // Register sensor listeners when app is resumed
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
    }

    fun getDirection(degrees: Float): Pair<Float, String> {
        val normalizedDegrees = (degrees + 360) % 360
        val directionName = when (normalizedDegrees) {
            in 0f..22.5f, in 337.5f..360f -> "N"
            in 22.5f..67.5f -> "NE"
            in 67.5f..112.5f -> "E"
            in 112.5f..157.5f -> "SE"
            in 157.5f..202.5f -> "S"
            in 202.5f..247.5f -> "SW"
            in 247.5f..292.5f -> "W"
            in 292.5f..337.5f -> "NW"
            else -> "N"
        }
        return Pair(normalizedDegrees, directionName)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        updateOrientationAngles()
    }

    var currentDeg: Float = 0F;

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    @OptIn(InternalCoroutinesApi::class)

    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val az = (-(orientationAngles[0].toDouble() * 180 / PI).toFloat())


        var rotateAnimation: Animation = RotateAnimation(
            currentDeg, az,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5F
        )
        currentDeg = az

        rotateAnimation.duration = 100
        rotateAnimation.repeatCount = 0
        needle.startAnimation(rotateAnimation)

        val normDeg = getDirection(currentDeg)
        deg.text = normDeg.first.toString() + " °"
        dir.text = normDeg.second

    }


}
