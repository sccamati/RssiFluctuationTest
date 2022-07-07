package com.example.rssifluctuationtest

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.beaconapp.models.BeaconModel
import com.example.beaconapp.models.OfflineRp
import com.example.beaconapp.models.OnlinePoint
import com.example.fingerprintapp.models.TestPoint
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.altbeacon.beacon.*
import org.altbeacon.beacon.service.ArmaRssiFilter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*


class FingerPrintOnline : AppCompatActivity() {

    lateinit var database: DocumentReference
    private var currentBeacons: MutableList<Beacon> = ArrayList()
    private var reachedBeacons: MutableList<BeaconModel> = ArrayList()
    lateinit var beaconManager: BeaconManager
    lateinit var region: Region
    private var listOfRP: MutableList<OfflineRp> = ArrayList()
    lateinit var dbName: String
    lateinit var collectionName: EditText
    var isSaveTestPoints: Boolean = false
    var saveTestPoint: Int = 0

    lateinit var onlineX: EditText
    lateinit var onlineY: EditText

    lateinit var beaconName: TextView

    lateinit var beaconRSSI: TextView

    lateinit var v: Vibrator

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finger_print_online)
        v =
            getSystemService(androidx.appcompat.app.AppCompatActivity.VIBRATOR_SERVICE) as android.os.Vibrator
        dbName = intent.extras?.getString("dbName").toString()

        collectionName = findViewById(R.id.collectionName)
        onlineX = findViewById(R.id.onlineX)
        onlineY = findViewById(R.id.onlineY)

        beaconName = findViewById(R.id.beaconNameOnline)

        beaconRSSI = findViewById(R.id.beaconRSSIOnline)

        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter::class.java)

        database = FirebaseFirestore.getInstance().document("OfflinePhase/${dbName}")
        val myBeaconNamespaceId = Identifier.parse("0x626C7565636861726D31")

        database.collection("ReferencePoints").get().addOnSuccessListener { documents ->

            for (document in documents) {
                listOfRP.add(document.toObject(OfflineRp::class.java))
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this)

        region = Region("MyBeacons", myBeaconNamespaceId, null, null)

        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this, rangingObserver)
        beaconManager.foregroundScanPeriod = 750
        beaconManager.foregroundBetweenScanPeriod = 0
        beaconManager.startRangingBeacons(region)

        loadCollectionName()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->

        GetCurrentBeacons(beacons)

        if(beacons.size > 0){
            beaconName.text = reachedBeacons[0].Name

            beaconRSSI.text = reachedBeacons[0].RSSI.toString()
        }

        try {
            if (isSaveTestPoints) {
                SaveTestPoint()
                saveTestPoint--
                Log.d("SIEMA", saveTestPoint.toString())
                if (saveTestPoint == 0) {
                    isSaveTestPoints = false
                    Toast.makeText(this, "added test points", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(
                            VibrationEffect.createOneShot(
                                1000,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        );
                    } else {
                        //deprecated in API 26
                        v.vibrate(1000);
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error during adding TP", Toast.LENGTH_SHORT).show()
        }

    }

    fun SaveTestPoint(view: View) {
        saveTestPoint = 750
        isSaveTestPoints = true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun SaveTestPoint() {
        try {
            val current = GetCurrentDate()
            val tp = TestPoint(
                reachedBeacons[0],
                onlineX.text.toString().toDouble(),
                onlineY.text.toString().toDouble(),
                current
            )

            database.collection(collectionName.text.toString()).add(tp).addOnSuccessListener {
            }.addOnFailureListener {
            }
        } catch (e: Exception) {

            val toast = Toast.makeText(applicationContext, "error adding TP", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun GetCurrentBeacons(beacons: Collection<Beacon>) {
        if (!beacons.isEmpty()) {
            val sorted = beacons.sortedByDescending { it.runningAverageRssi }.toMutableList()

            currentBeacons = sorted
            currentBeacons = if (sorted.size < 4) {
                sorted
            } else if (sorted.size == 4) {
                sorted
            } else {
                sorted.slice(0..3).toMutableList()
            }

            if (CheckFoundBeacons(currentBeacons)) {
                for (beacon: Beacon in currentBeacons) {
                    reachedBeacons.find { it.Name == beacon.bluetoothName }!!.RSSI =
                        beacon.runningAverageRssi
                }
            } else {
                reachedBeacons.clear()
                for (beacon: Beacon in currentBeacons) {
                    reachedBeacons.add(BeaconModel(beacon.bluetoothName, beacon.runningAverageRssi))
                }
            }

            reachedBeacons = reachedBeacons.sortedByDescending { it.RSSI }.toMutableList()

        }
    }

    fun CheckFoundBeacons(currentBeacons: MutableList<Beacon>): Boolean {
        for (beacon: Beacon in currentBeacons) {
            if (!reachedBeacons.any { it.Name == beacon.bluetoothName }) {
                return false
            }
        }
        return true
    }


    fun saveCollectionName(view: View) {
        val fos = openFileOutput("CollefctionName.txt", Context.MODE_PRIVATE)
        fos.write(collectionName.text.toString().toByteArray())
    }

    fun loadCollectionName() {
        val fis = openFileInput("CollefctionName.txt")
        var inputStreamReader = InputStreamReader(fis)
        val bufferedReader = BufferedReader(inputStreamReader)

        val stringBuilder = StringBuilder()
        var text: String? = null

        while ({ text = bufferedReader.readLine(); text }() != null) {
            stringBuilder.append(text)
        }

        collectionName.setText(stringBuilder.toString())

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun GetCurrentDate(): String? {
        val current = Calendar.getInstance().time
        val format1 = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS")
        val formatted = format1.format(current);
        return formatted
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.stopRangingBeacons(region)
    }
}