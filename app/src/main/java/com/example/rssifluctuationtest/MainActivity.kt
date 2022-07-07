package com.example.rssifluctuationtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    val mBtAdapter = BluetoothAdapter.getDefaultAdapter()
    lateinit var databaseName : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        databaseName = findViewById(R.id.dbName)
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        mBtAdapter.enable()

        loadDbName()
    }

    fun GoToOnlinePhase(view: View){

        val intent = Intent(this, FingerPrintOnline::class.java).apply{}
        val dbName = databaseName.text.toString()
        intent.putExtra("dbName", dbName)
        startActivity(intent)
    }

    fun saveDbName(view : View){
        val fos = openFileOutput("DbName.txt", Context.MODE_PRIVATE)
        fos.write(databaseName.text.toString().toByteArray())
    }

    fun loadDbName(){
        val fis = openFileInput("DbName.txt")
        var inputStreamReader = InputStreamReader(fis)
        val bufferedReader = BufferedReader(inputStreamReader)

        val stringBuilder = StringBuilder()
        var text : String? = null

        while ({ text = bufferedReader.readLine(); text} () !=  null){
            stringBuilder.append(text)
        }

        databaseName.setText(stringBuilder.toString())

    }

    override fun onDestroy() {
        super.onDestroy()
    }
    }
