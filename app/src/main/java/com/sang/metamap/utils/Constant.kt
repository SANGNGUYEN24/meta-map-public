package com.sang.metamap.utils

import com.mapbox.mapboxsdk.geometry.LatLng

object Constant {
    // Firebase
    const val CAMPUS = "campus"
    const val REVIEW = "review"
    const val BUILDING_INFO = "buildingInfo"
    const val ENTERTAINMENT_VENUE = "entertainmentVenue"
    const val SERVICE = "service"
    const val USER_REVIEW = "userReview"
    const val COLLECTION_HCMUT_MAP_USER = "hcmutMapUser"
    const val COLLECTION_HCMUT_INDOOR_PATH= "indoorPath"
    const val COLLECTION_BUILDING_INDOOR_PATH= "path"
    const val HCMUT = "hcmut"
    const val INDOOR_ROUTE_MAIN_GATE_ID = "gate1"
    const val QR_CODE_KEY = "qr_code_key"
    const val SRC_TO_GET_PLACE_DETAIL = "src_to_get_place_detail"

    const val DEFAULT_LATITUDE = 10.773385891429179
    const val DEFAULT_LONGITUDE = 106.66009900282725

    // Map
    val HCMUT_MAP_SOUTH_WEST_POINT = LatLng(
        10.776672598672207,
        106.6569465702433
    )

    val HCMUT_MAP_NORTH_EAST_POINT = LatLng(
        10.770258017897598,
        106.66270005616461
    )

    // Animation
    const val SHORT_ANIM_TIME = 100L

    // UI
    const val BOTTOM_SHEET_PEEK_HEIGHT = 400

    // String format
    const val STR_FORMAT_ROOM = "Room %s"

    // Field
    const val FIELD_MAX_INDOOR_PATH_CAN_BE_CREATED = "maxIndoorPathCanBeCreated"
    const val FIELD_NUM_OF_INDOOR_PATH_CREATED = "numOfIndoorPathCreated"


    const val MAIN_CONTENT_JUST_TURN_LEFT = "You've turned left"
    const val MAIN_CONTENT_JUST_TURN_RIGHT = "You've turned right"

}