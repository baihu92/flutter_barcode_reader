package com.apptreesoftware.barcodescan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Window
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class BarcodeScannerActivity : Activity(), ZXingScannerView.ResultHandler {

    lateinit var scannerView: ZXingScannerView
    lateinit var flashOnTitle: String
    lateinit var flashOffTitle: String

    companion object {
        const val REQUEST_TAKE_PHOTO_CAMERA_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val intent = this.intent
        flashOnTitle = intent.getStringExtra("flashOnTitle")
        flashOffTitle = intent.getStringExtra("flashOffTitle")
        val fontName = intent.getStringExtra("fontName")

        title = ""
        scannerView = ZXingScannerView(this)
        scannerView.setAutoFocus(true)
        // this paramter will make your HUAWEI phone works great!
        scannerView.setAspectTolerance(0.5f)

        val flashButton = Button(this)
        flashButton.setBackgroundColor(Color.TRANSPARENT)
        flashButton.setTextColor(Color.WHITE)
        flashButton.setOnClickListener {
            scannerView.flash = !scannerView.flash
            flashButton.text = if (scannerView.flash) flashOffTitle else flashOnTitle
        }

        if (fontName != null && fontName.isNotEmpty())
            flashButton.typeface = Typeface.createFromAsset(assets, "fonts/$fontName")

        val px = DisplayUtil.convertDpToPixel(14f, this)
        flashButton.setPadding(px, px, px, px)
        flashButton.text = if (scannerView.flash) flashOffTitle else flashOnTitle
        val textViewLayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        textViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        val layout = RelativeLayout(this)
        layout.addView(scannerView)
        layout.addView(flashButton, textViewLayoutParams)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        // start camera immediately if permission is already given
        if (!requestCameraAccessIfNecessary()) {
            scannerView.startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", result.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun finishWithError(errorCode: String) {
        val intent = Intent()
        intent.putExtra("ERROR_CODE", errorCode)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun requestCameraAccessIfNecessary(): Boolean {
        val array = arrayOf(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, array,
                    REQUEST_TAKE_PHOTO_CAMERA_PERMISSION)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO_CAMERA_PERMISSION -> {
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    scannerView.startCamera()
                } else {
                    finishWithError("PERMISSION_NOT_GRANTED")
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}

object PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].

     * @see Activity.onRequestPermissionsResult
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}

object DisplayUtil {
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Int {
        return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
