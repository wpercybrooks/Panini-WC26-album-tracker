package com.panini.wc26.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.panini.wc26.R
import com.panini.wc26.data.AppDatabase
import com.panini.wc26.data.Sticker
import com.panini.wc26.databinding.ActivityScannerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

data class MatchedId(
    val id: String,
    val boundingBox: Rect
)

object StickerIdParser {
    fun extractStickerId(text: String): String? {
        // Regex for IDs like COL 1, FWC 19, CC 14
        // Handles optional space and common OCR error O instead of 0
        val regex = Regex("([A-Z]{2,3})\\s*([0-9O]{1,2})", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        
        return match?.let {
            val code = it.groupValues[1].uppercase()
            var number = it.groupValues[2].uppercase()
            
            // Normalize O to 0 in numbers
            number = number.replace('O', '0')
            
            // Format consistently with space
            "$code $number"
        }
    }
}

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var validPrefixes: Set<String> = emptySet()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(application)
            val ids = withContext(Dispatchers.IO) {
                db.stickerDao().getAllStickerIds()
            }
            validPrefixes = ids.mapNotNull { it.split(" ").firstOrNull() }.toSet()
            Log.d(TAG, "Loaded ${validPrefixes.size} valid prefixes: $validPrefixes")
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding.captureButton.setOnClickListener { takePhoto() }
        binding.closeButton.setOnClickListener { finish() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageForOverlay(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageForOverlay(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        // Use rotation to determine if we need to swap width/height for scaling
        val isRotated = imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270
        val width = if (isRotated) imageProxy.height else imageProxy.width
        val height = if (isRotated) imageProxy.width else imageProxy.height

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val matches = mutableListOf<MatchedId>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val detectedId = StickerIdParser.extractStickerId(line.text)
                        if (detectedId != null) {
                            val prefix = detectedId.split(" ").first()
                            if (validPrefixes.contains(prefix)) {
                                val box = line.boundingBox
                                if (box != null) {
                                    matches.add(MatchedId(detectedId, box))
                                }
                            }
                        }
                    }
                }
                binding.scannerOverlay.updateData(matches, width, height)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        processImage(inputImage, imageProxy)
                    } else {
                        imageProxy.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun processImage(image: InputImage, imageProxy: ImageProxy) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d(TAG, "Detected text: ${visionText.text}")
                val matches = mutableListOf<MatchedId>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val detectedId = StickerIdParser.extractStickerId(line.text)
                        if (detectedId != null) {
                            val prefix = detectedId.split(" ").first()
                            Log.d(TAG, "Candidate ID: $detectedId (Prefix: $prefix)")
                            if (validPrefixes.contains(prefix)) {
                                val box = line.boundingBox
                                if (box != null) {
                                    matches.add(MatchedId(detectedId, box))
                                    Log.d(TAG, "Valid Match: $detectedId at $box")
                                }
                            }
                        }
                    }
                }

                if (matches.isNotEmpty()) {
                    // Pick the best match:
                    // 1. Prioritize right-most
                    // 2. Then top-most
                    val bestMatch = matches.sortedWith(
                        compareByDescending<MatchedId> { it.boundingBox.right }
                            .thenBy { it.boundingBox.top }
                    ).first()
                    
                    Log.d(TAG, "Best match selected: ${bestMatch.id}")
                    showConfirmationDialog(bestMatch.id)
                } else {
                    Toast.makeText(this, "No valid sticker ID detected. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Text recognition failed", e)
                Toast.makeText(this, "Recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun showConfirmationDialog(id: String) {
        val editText = android.widget.EditText(this).apply {
            setText(id)
            setSelection(id.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Sticker ID")
            .setView(editText)
            .setPositiveButton("Next") { _, _ ->
                val finalId = editText.text.toString().trim().uppercase()
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(application)
                    val sticker = withContext(Dispatchers.IO) {
                        db.stickerDao().getStickerById(finalId)
                    }
                    if (sticker != null) {
                        showAddDecisionDialog(sticker)
                    } else {
                        Toast.makeText(this@ScannerActivity, "Sticker $finalId not found in catalog", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddDecisionDialog(sticker: Sticker) {
        AlertDialog.Builder(this)
            .setTitle("Sticker Found")
            .setMessage("You currently have ${sticker.ncopies} copies of ${sticker.id}.")
            .setPositiveButton("Add to collection") { _, _ ->
                incrementStickerCount(sticker)
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    private fun incrementStickerCount(sticker: Sticker) {
        lifecycleScope.launch {
            Log.d(TAG, "Incrementing sticker count: ${sticker.id}")
            val db = AppDatabase.getDatabase(application)
            val stickerDao = db.stickerDao()
            
            val updated = sticker.copy(ncopies = sticker.ncopies + 1)
            withContext(Dispatchers.IO) {
                stickerDao.insertStickers(listOf(updated))
                // Verification read
                val verified = stickerDao.getStickerById(sticker.id)
                Log.d(TAG, "Verification - ID: ${verified?.id}, new ncopies: ${verified?.ncopies}")
            }
            Toast.makeText(this@ScannerActivity, "Added ${sticker.id}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "ScannerActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}