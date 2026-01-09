package com.google.jules.camera.network

import java.io.File
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Interface for the Backend Service.
 * Uploads images and retrieves processed results.
 */
class CloudProcessor {
    private val client = OkHttpClient()
    private val baseUrl = "https://api.jules-camera.google.com/v1" // Mock URL

    fun uploadAndProcess(
        imageFile: File,
        options: JSONObject,
        callback: (String?, Exception?) -> Unit
    ) {
        val mediaType = "image/*".toMediaTypeOrNull()
        val fileBody = imageFile.asRequestBody(mediaType)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name, fileBody)
            .addFormDataPart("options", options.toString())
            .build()

        val request = Request.Builder()
            .url("$baseUrl/process/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        callback(null, IOException("Unexpected code $it"))
                        return
                    }

                    val responseBody = it.body?.string()
                    val json = JSONObject(responseBody ?: "{}")
                    val jobId = json.optString("job_id")
                    callback(jobId, null)
                }
            }
        })
    }

    fun checkStatus(jobId: String, callback: (JSONObject?, Exception?) -> Unit) {
         val request = Request.Builder()
            .url("$baseUrl/process/status/$jobId")
            .build()

         client.newCall(request).enqueue(object : Callback {
             override fun onFailure(call: Call, e: IOException) {
                 callback(null, e)
             }

             override fun onResponse(call: Call, response: Response) {
                 response.use {
                     if (!it.isSuccessful) {
                         callback(null, IOException("Unexpected code $it"))
                         return
                     }
                     callback(JSONObject(it.body?.string() ?: "{}"), null)
                 }
             }
         })
    }
}
