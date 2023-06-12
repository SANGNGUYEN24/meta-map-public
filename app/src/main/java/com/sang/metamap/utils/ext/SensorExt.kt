package com.sang.metamap.utils.ext

import com.sang.metamap.domain.sensor.ISensor

fun MutableList<ISensor>.start() {
    forEach { it.start() }
}

fun MutableList<ISensor>.stop() {
    forEach { it.stop() }
}