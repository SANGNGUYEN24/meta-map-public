package com.sang.metamap

import com.sang.metamap.utils.BuildingRoom
import com.sang.metamap.utils.CloudDataUtil
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FireStoreTest {
    var leftAngle = 0
    var rightAngle = 0
    var turnAnglesUpdated = false

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

    @Test
    fun `test updateTurnAngle for positive direction in first and second quadrant`() {
        val direction = 45
        updateTurnAngle(direction)

        assertEquals(-45, leftAngle)
        assertEquals(135, rightAngle)
        assertTrue(turnAnglesUpdated)
    }

    @Test
    fun `test updateTurnAngle for negative direction in first and second quadrant`() {
        val direction = -60
        updateTurnAngle(direction)

        assertEquals(-150, leftAngle)
        assertEquals(30, rightAngle)
        assertTrue(turnAnglesUpdated)
    }

    @Test
    fun `test updateTurnAngle for direction in third quadrant`() {
        val direction = -120
        updateTurnAngle(direction)

        assertEquals(150, leftAngle)
        assertEquals(-30, rightAngle)
        assertTrue(turnAnglesUpdated)
    }

    @Test
    fun `test updateTurnAngle for direction in fourth quadrant`() {
        val direction = 160
        updateTurnAngle(direction)

        assertEquals(70, leftAngle)
        assertEquals(-110, rightAngle)
        assertTrue(turnAnglesUpdated)
    }
}

