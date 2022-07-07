package com.example.beaconapp.models

import com.google.firebase.firestore.IgnoreExtraProperties
import org.altbeacon.beacon.Beacon

@IgnoreExtraProperties
class OfflineRp(
    var Beacons: MutableList<BeaconModel> = arrayListOf(),
    var X: Double = 0.0,
    var Y: Double = 0.0
) {
}