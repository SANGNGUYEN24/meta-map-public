package com.sang.metamap.domain.model

import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.sang.metamap.utils.Constant


data class Building(
    val imageLink: String? = null,
    val buildingName: String? = null,
    val buildingId: String? = null,
    val campusName: String? = null,
    val campusId: String? = null,
    val desc: String? = null,
    val starNum: Double? = null,
    val reviewNum: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rooms: List<String>? = null,
    var enableIndoorFeature: Boolean = false
)

fun Building.getCoordinate(): Point {
    return  Point.fromLngLat(
        this.longitude ?: Constant.DEFAULT_LATITUDE,
        this.latitude ?: Constant.DEFAULT_LONGITUDE
    )
}
