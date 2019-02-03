package com.nimtego.tesseract_sample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : FragmentActivity() {

    private val TAG = MainActivity::class.java.simpleName
    val PHOTO_REQUEST_CODE = 1
    private var tessBaseApi: TessBaseAPI? = null
    var textView: TextView? = null
    var outputFileUri: Uri? = null
    private val lang = "eng"
    var result = "empty"
    private var requestTool: RequestPermissionsTool? = null

    private val DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/"
    private val TESSDATA = "tessdata"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (action_button != null) {
            action_button.setOnClickListener { startCameraActivity() }
        }
    }

    private fun startCameraActivity() {
        try {
            val IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs"
            prepareDirectory(IMGS_PATH)

            val img_path = "$IMGS_PATH/ocr.jpg"

            outputFileUri = Uri.fromFile(File(img_path))

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE)
            }
        } catch (e: Exception) {

        }

    }

    private fun prepareDirectory(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(
                    TAG,

                    "ERROR"
                )
            }
        } else {
            Log.i(TAG, "Created directory")
        }
    }

    public override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        //making photo
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            doOCR()
        } else {
            Toast.makeText(this, "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doOCR() {
        prepareTesseract()
        outputFileUri?.let { startOCR(it) }
    }

    private fun prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        copyTessDataFiles(TESSDATA)
    }

    private fun copyTessDataFiles(path: String) {
        try {
            val fileList = assets.list(path)

            for (fileName in fileList!!) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                val pathToDataFile = "$DATA_PATH$path/$fileName"
                if (!File(pathToDataFile).exists()) {

                    val into = assets.open("$path/$fileName")

                    val out = FileOutputStream(pathToDataFile)

                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int

                    while (into.read(buf) > 0) {
                        len = into.read(buf)
                        out.write(buf, 0, len)
                    }
                    into.close()
                    out.close()

                    Log.d(TAG, "Copied " + fileName + "to tessdata")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString())
        }

    }

    private fun startOCR(imgUri: Uri) {
        try {
            val options = BitmapFactory.Options()
            options.inSampleSize =
                    4 // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            val bitmap = BitmapFactory.decodeFile(imgUri.path, options)

            result = extractText(bitmap)

            textView?.text = result

        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }

    }

    private fun extractText(bitmap: Bitmap): String {
        try {
            tessBaseApi = TessBaseAPI()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.")
            }
        }

        tessBaseApi?.init(DATA_PATH, lang)

        //       //EXTRA SETTINGS
        //        //For example if we only want to detect numbers
        //        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
        //
        //        //blackList Example
        //        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
        //                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d(TAG, "Training file loaded")
        tessBaseApi?.setImage(bitmap)
        var extractedText = "empty result"
        try {
            extractedText = tessBaseApi?.utF8Text ?: ":("
        } catch (e: Exception) {
            Log.e(TAG, "Error in recognizing text.")
        }

        tessBaseApi?.end()
        return extractedText
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestTool = RequestPermissionsToolImpl()
        (requestTool as RequestPermissionsToolImpl).requestPermissions(this, permissions)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        var grantedAllPermissions = true
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                grantedAllPermissions = false
            }
        }

        if (grantResults.size != permissions.size || !grantedAllPermissions) {

            requestTool?.onPermissionDenied()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

}
