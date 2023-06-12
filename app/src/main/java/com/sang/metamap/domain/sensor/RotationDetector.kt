package com.sang.metamap.domain.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sang.metamap.domain.model.TurnDirection

const val SCALE = 10

class RotationDetector(private val sensorManager: SensorManager, private val listener: Listener) :
    SensorEventListener, ISensor {

    private lateinit var rotationSensor: Sensor
    private var leftAngle = 0
    private var rightAngle = 0
    private var turnAnglesUpdated = false

    override fun start() {
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun showMess(message: String) {
        listener.onRotationDetectorWannaSay(message)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                // Get the rotation matrix form rotation vector sensor
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                // Calculate Euler angles based on the rotation matrix
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // Get the direction of the device relative to the magnetic North (in degree)
                val direction = Math.toDegrees(orientation[0].toDouble()).toInt()
                if (!turnAnglesUpdated) {
                    // When user turn left or right,
                    // we calculate again the left direction and right direction
                    updateTurnAngle(direction)
                }

                when (direction) {
                    // If the direction in range of leftAngle +- SCALE
                    // It means the user turned left
                    in (leftAngle - SCALE)..(leftAngle + SCALE) -> {
                        listener.onDirectionChanged(TurnDirection.LEFT)
                        turnAnglesUpdated = false
                        updateTurnAngle(direction)
                    }
                    // Similarly, the user turned right
                    in (rightAngle - SCALE)..(rightAngle + SCALE) -> {
                        listener.onDirectionChanged(TurnDirection.RIGHT)
                        turnAnglesUpdated = false
                        updateTurnAngle(direction)
                    }
                }
            }
        }
    }

    private fun updateTurnAngle(direction: Int) {
        when (direction) {
            // The direction is in the first and second quadrant
            in 0..90, in -90..0 -> {
                leftAngle = direction - 90
                rightAngle = direction + 90
            }

            // Third quadrant
            in -180..-90 -> {
                leftAngle = direction + 270
                rightAngle = direction + 90
            }

            // Forth quadrant
            in 90..180 -> {
                leftAngle = direction - 90
                rightAngle = direction - 270
            }
        }
        turnAnglesUpdated = true
    }

    interface Listener {
        /**
         * Determine the user is turned right or left
         */
        fun onDirectionChanged(turnDirection: TurnDirection)

        /**
         * Show a toast with the message
         */
        fun onRotationDetectorWannaSay(message: String)
    }
}
