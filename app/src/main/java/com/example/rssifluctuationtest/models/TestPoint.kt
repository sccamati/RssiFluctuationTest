package com.example.fingerprintapp.models

import com.example.beaconapp.models.BeaconModel

class TestPoint(
    var Beacon: BeaconModel? = null,
    val RealX: Double = 0.0,
    val RealY: Double = 0.0,
    val Date: String? = null
) {
}