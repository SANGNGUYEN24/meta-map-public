package com.sang.metamap.utils

import org.json.JSONObject

object JsonUtil {
    infix fun <V> String.by(value: V): Pair<String, V> = Pair(this, value)
    fun <V> jsonOf(vararg pairs: Pair<String, V>): JSONObject {
        val json = JSONObject()
        pairs.forEach {
            json.put(it.first, it.second)
        }
        return json
    }
}

