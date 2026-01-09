package com.google.jules.camera.editor

import android.graphics.RectF

/**
 * Module 8: Crop Tool
 * Interface stub for interactive cropping.
 */
class CropTool {

    // The current crop rectangle (normalized 0..1 or pixel coordinates)
    var cropRect: RectF = RectF(0f, 0f, 1f, 1f)

    // Aspect ratio lock (e.g., 16/9f, or -1f for free)
    var aspectRatio: Float = -1f

    fun setAspectRatioLock(ratio: Float) {
        aspectRatio = ratio
        // Logic to adjust cropRect to match ratio would go here
    }

    fun updateCrop(newRect: RectF) {
        // Logic to validate bounds (stay within image)
        cropRect.set(newRect)
    }

    fun getCropCoordinates(imageWidth: Int, imageHeight: Int): android.graphics.Rect {
        return android.graphics.Rect(
            (cropRect.left * imageWidth).toInt(),
            (cropRect.top * imageHeight).toInt(),
            (cropRect.right * imageWidth).toInt(),
            (cropRect.bottom * imageHeight).toInt()
        )
    }
}
