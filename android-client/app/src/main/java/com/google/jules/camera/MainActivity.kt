package com.google.jules.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.jules.camera.core.CameraManager
import com.google.jules.camera.network.CloudProcessor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraExecutor: ExecutorService
    private val cloudProcessor = CloudProcessor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        val cameraCaptureButton = findViewById<Button>(R.id.image_capture_button)
        cameraCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Create time-stamped output file to hold the image
        val filename = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(
            getExternalFilesDir(null), // Use app-specific storage for simplicity
            "Jules_${filename}.jpg"
        )

        Toast.makeText(baseContext, "Capturing...", Toast.LENGTH_SHORT).show()

        cameraManager.takePhoto(
            photoFile,
            onImageSaved = { file ->
                val msg = "Photo saved locally. Uploading to AI Engine..."
                runOnUiThread { Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show() }
                Log.d(TAG, msg)

                // Trigger AI Processing
                uploadImageForProcessing(file)
            },
            onError = { exc ->
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                runOnUiThread { Toast.makeText(baseContext, "Capture Failed", Toast.LENGTH_SHORT).show() }
            }
        )
    }

    private fun uploadImageForProcessing(file: File) {
        // Construct options JSON
        val options = JSONObject().apply {
            put("modules", JSONArray().apply {
                put("auto_enhance")
                put("super_res")
            })
            put("parameters", JSONObject().apply {
                put("output_format", "JPEG")
                put("quality", 95)
            })
        }

        cloudProcessor.uploadAndProcess(file, options) { jobId, error ->
            runOnUiThread {
                if (error != null) {
                    Log.e(TAG, "Upload failed: ${error.message}")
                    Toast.makeText(baseContext, "Upload Failed: ${error.message}", Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "Job started: $jobId")
                    Toast.makeText(baseContext, "AI Processing Started (Job: $jobId)", Toast.LENGTH_LONG).show()
                    // In a real app, we would now poll checkStatus(jobId)
                }
            }
        }
    }

    private fun startCamera() {
        val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
        cameraManager = CameraManager(this, this, viewFinder)
        cameraManager.startCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraManager.isInitialized) {
            cameraManager.shutdown()
        }
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "JulesCamera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
            }.toTypedArray()
    }
}
