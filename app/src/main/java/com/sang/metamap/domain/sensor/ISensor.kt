package com.sang.metamap.domain.sensor

interface ISensor {
    /**
     * Activate this sensor
     */
    fun start()

    /**
     * Deactivate this sensor
     */
    fun stop()

    fun showMess(message: String)
}