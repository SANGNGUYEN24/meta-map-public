package com.sang.metamap.domain.model

import android.graphics.Bitmap

data class RecordIndoorItem(
    var mainContent: String = "",
    var detailContent: String = "",
    var imageBitmap: Bitmap? = null,
    var turnDirection: TurnDirection = TurnDirection.LEFT,
    var type: RecordIndoorItemType = RecordIndoorItemType.IN_RECORD,
)

enum class TurnDirection {
    LEFT, RIGHT, NA
}

enum class RecordIndoorItemType {
    START, IN_RECORD, END
}


