package com.example.beaconapp.models

import java.time.LocalDateTime
import java.util.*

class OnlinePoint(
    var Beacons: MutableList<BeaconModel> = arrayListOf(),
    val X: Double = 0.0,
    val Y: Double = 0.0,
    val RealX: Double = 0.0,
    val RealY: Double = 0.0,
    val Date: String? = null
) {
}

