package com.sang.metamap.domain.repository

import com.sang.metamap.domain.model.Building

interface SharedPrefRepository {
    fun getHCMUTBuildings(): List<Building>
    fun saveHCMUTBuildings(buildings: List<Building>)
}