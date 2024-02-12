package com.websitebeaver.documentscanner.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.websitebeaver.documentscanner.extensions.saveToFile
import java.io.File
import java.io.IOException
import java.io.InputStream


/**
 * This class contains a helper function for opening the gallery.
 *
 * @param activity current activity
 * @param onGallerySuccess gets called with photo file path when photo is ready
 * @param onCancelGallery gets called when user cancels out of gallery
 * @constructor creates gallery util
 */
class GalleryUtil(
  private val activity: ComponentActivity,
  private val onGallerySuccess: (photoFilePath: String) -> Unit,
  private val onCancelGallery: () -> Unit
) {
  /** @property photoFilePath the photo file path */
  private lateinit var photoFilePath: String

  /** @property photoFile the photo file */
  private lateinit var photoFile: File

  /** @property startForResult used to launch gallery */
  private val startForResult =
    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
      when (result.resultCode) {
        Activity.RESULT_OK -> {
          val data = result.data
          val uri = data?.data!!


          //modifyOrientation(  photoFilePath)

          //Log.d("TAG", calculateBitmapRotateDegrees(uri).toString())
          //modifyOrientation(ImageUtil().readBitmapFromFileUriString(uri.toString(), activity.contentResolver),photoFilePath ).saveToFile(photoFile, 100)
          // create bitmap from image selection and save it to file
          rotateImage(ImageUtil().readBitmapFromFileUriString(
            uri.toString(),
            activity.contentResolver
          ), calculateBitmapRotateDegrees(uri)).saveToFile(photoFile, 100)

          // send back photo file path on success
          onGallerySuccess(photoFilePath)
        }
        Activity.RESULT_CANCELED -> {
          // delete the photo since the user didn't finish choosing the photo
          File(photoFilePath).delete()
          onCancelGallery()
        }
      }
    }

  /**
   * open the gallery by launching an open document intent
   *
   * @param pageNumber the current document page number
   */
  @Throws(IOException::class)
  fun openGallery(pageNumber: Int) {
    // create intent to open gallery
    val openGalleryIntent = getGalleryIntent()

    // create new file for photo
    photoFile = FileUtil().createImageFile(activity, pageNumber)

    // store the photo file path, and send it back once the photo is saved
    photoFilePath = photoFile.absolutePath

    // open gallery
    startForResult.launch(openGalleryIntent)
  }

  private fun getGalleryIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    type = "image/*"
    addCategory(Intent.CATEGORY_OPENABLE)
    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
  }



  private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }

  private fun flipImage(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
    val matrix = Matrix()
    matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }



  private fun calculateBitmapRotateDegrees(uri: Uri): Float {
    var exif: ExifInterface? = null
    try {
      val inputStream: InputStream? = activity.contentResolver.openInputStream(uri)
      inputStream?.run {
        exif = ExifInterface(this)
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }

    exif?.run {
      val orientation = getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
      )

      return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90F
        ExifInterface.ORIENTATION_ROTATE_180 -> 180F
        ExifInterface.ORIENTATION_ROTATE_270 -> 270F
        else -> 0F
      }
    }
    return 0F
  }


}