package com.fh.android.cameraapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.view.CameraView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.CAMERA,
                              Manifest.permission.WRITE_EXTERNAL_STORAGE,
                              Manifest.permission.READ_EXTERNAL_STORAGE)
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    private val filenameTemplate = "img_*.png"
    private val root = Environment.getExternalStorageDirectory()

    private var fotoapparat: Fotoapparat? = null
    private var vibe : Vibrator? = null
    private var effectSuccess : VibrationEffect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeVibrator()
        addButtonListeners()
        createFotoapparat()
    }

    private fun initializeVibrator(){
        vibe = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        effectSuccess = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    private fun addButtonListeners(){
        fab_camera.setOnClickListener {
            takePhoto()
        }
    }

    private fun createFotoapparat(){
        val cameraView = findViewById<CameraView>(R.id.camera_view)

        fotoapparat = Fotoapparat(
            context = this,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back()
        )
    }


    private fun takePhoto(){
        if (hasNoPermissions()) {
            requestPermissions()
        } else {
           var filename = filenameTemplate.replace("*", LocalDateTime.now().format(formatter))
           val file = File(root.absolutePath + "/DCIM/Camera/" + filename)

           val photoResult = fotoapparat?.takePicture()

           photoResult?.saveToFile(file)?.whenAvailable { result ->
               vibe?.vibrate(effectSuccess)
               toast("Image saved successfully.")
           }
        }
    }

    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(){
        requestPermissions(this, permissions,0)
    }

    private fun toast(message : String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
    }

    override fun onStart() {
        super.onStart()
        if (hasNoPermissions()) {
            requestPermissions()
        } else {
            fotoapparat?.start()
        }
    }
}
