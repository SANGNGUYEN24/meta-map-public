package com.sang.metamap.data.impl

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sang.metamap.domain.model.Building
import com.sang.metamap.domain.repository.SharedPrefRepository
import com.sang.metamap.utils.Constant.HCMUT

class SharedPrefImpl(
    private val sharedPref: SharedPreferences
) : SharedPrefRepository {
    private val defaultHCMUTBuildingsValue = "{}"
    override fun getHCMUTBuildings(): List<Building> {
        return sharedPref.getString(HCMUT, defaultHCMUTBuildingsValue)?.toBuildingList() ?: emptyList()
    }

    override fun saveHCMUTBuildings(buildings: List<Building>) {
        with(sharedPref.edit()) {
            putString(HCMUT, buildings.toString())
            apply()
        }
    }

    fun String.toBuildingList(): List<Building> {
        if (this == defaultHCMUTBuildingsValue) return emptyList()
        val gson = Gson()
        val buildingListType = object : TypeToken<List<Building>>() {}.type
        return gson.fromJson(this, buildingListType)
    }
}