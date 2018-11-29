package com.wit.jasonfagerberg.nightsout.addDrink.scanBarcode

import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import android.graphics.Bitmap
import android.os.Handler
import android.graphics.BitmapFactory
import android.widget.Toast
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream


class ScanBarcodeActivity : AppCompatActivity() {
    private var mCamera: Camera? = null
    private lateinit var camView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_scan_barcode)

        val x = findViewById<ImageView>(R.id.scan_barcode_close)
        x.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FRAGMENT_ID", 4)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        mCamera = getCameraInstance()
        camView = CameraView(this, mCamera!!)
        val fl = findViewById<FrameLayout>(R.id.layout_scan_barcode)
        fl.addView(camView)
        super.onResume()

//        val handler = Handler()
//        val delay = 1000 //milliseconds
//
//        handler.postDelayed(object : Runnable {
//            override fun run() {
//                //do something
//                //takePicture()
//                //takePicture()
//                handler.postDelayed(this, delay.toLong())
//            }
//        }, delay.toLong())
        findViewById<FloatingActionButton>(R.id.fab_take_photo).setOnClickListener{
            takePicture()
        }


    }



    private fun takePicture() { //you can call this every 5 seconds using a timer or whenever you want
        mCamera!!.takePicture(null, null,
                Camera.PictureCallback { data, camera ->
                    val uriTarget = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())

                    val imageFileOS: OutputStream?
                    try {
                        imageFileOS = contentResolver.openOutputStream(uriTarget!!)
                        imageFileOS!!.write(data)
                        imageFileOS.flush()
                        imageFileOS.close()

                        Toast.makeText(this, "Image saved: " + uriTarget.toString(), Toast.LENGTH_LONG).show()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val pic = BitmapFactory.decodeByteArray(data, 0, data.size)
                    FirebaseApp.initializeApp(this)
                    val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                            .setBarcodeFormats(
                                    FirebaseVisionBarcode.FORMAT_UPC_A,
                                    FirebaseVisionBarcode.FORMAT_UPC_E)
                            .build()
                    val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
                    val image: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(pic)
                    detector.detectInImage(image)
                            .addOnSuccessListener {
                                // Task succeeded!
                                for (barcode in it) {
                                    Log.v("ScanBarcodeActivity", "scan successful")
                                    // Do something with barcode
                                }
                            }
                            .addOnFailureListener {
                                Log.v("ScanBarcodeActivity", "scan failed")
                                // Task failed with an exception
                            }
                })
    }

    override fun onPause() {
        mCamera?.release()
        mCamera = null
        super.onPause()
    }

    private fun getCameraInstance(): Camera? {
        try {
            return Camera.open()
        } catch (e: Exception) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FRAGMENT_ID", 4)
            intent.putExtra("ERROR_MESSAGE", "failed to open camera")
            startActivity(intent)
            e.printStackTrace()
        }
        return null
    }
}
