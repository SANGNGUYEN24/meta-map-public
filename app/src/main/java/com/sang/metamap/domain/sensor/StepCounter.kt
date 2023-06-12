package com.sang.metamap.domain.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

const val TAG = "StepCounter"

class StepCounter(
    private val sensorManager: SensorManager, private val stepCounterListener: Listener
) : SensorEventListener, ISensor {
    private var stepSensor: Sensor? = null
    private var stepWhenStart = 0

    /**
     * The tracking step
     */
    var stepNow = 0
    var shouldResetStepWhenStart = true

    override fun start() {
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            // Step counter sensor not available on this device
            stepCounterListener.onStepSensorNotAvailable()
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun showMess(message: String) {
        stepCounterListener.onStepCounterWannaSay(message)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (shouldResetStepWhenStart) {
                stepWhenStart = event.values[0].toInt()
            }
            shouldResetStepWhenStart = false
            stepNow = event.values[0].toInt() - stepWhenStart
            stepCounterListener.onStepChange(stepNow)
        }
    }

    interface Listener {
        fun onStepChange(stepCounted: Int)
        fun onStepSensorNotAvailable()

        /**
         * Show a toast with the message
         */
        fun onStepCounterWannaSay(message: String)
    }
}
