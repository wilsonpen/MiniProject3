package com.ziven0069.miniproject3.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziven0069.miniproject3.model.Tanaman
import com.ziven0069.miniproject3.network.ApiStatus
import com.ziven0069.miniproject3.network.TanamanApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel: ViewModel() {
    var data = mutableStateOf(emptyList<Tanaman>())
        private set
    var status = MutableStateFlow(ApiStatus.LOADING)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set
    var deleteStatus = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = TanamanApi.service.getTanaman(userId)
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(userId: String, nama_tanaman: String, namaLatin: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TanamanApi.service.postTanaman(
                    userId,
                    nama_tanaman.toRequestBody("text/plain".toMediaTypeOrNull()),
                    namaLatin.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )

                if (result.status == "success")
                    retrieveData(userId)
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }


        fun updateData(userId: String, nama_tanaman: String, namaLatin: String, bitmap: Bitmap, id : String) {
            Log.d("MainViewModel", "UserId : $userId , $nama_tanaman $namaLatin $id");
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = TanamanApi.service.updateTanaman(
                        userId,
                        nama_tanaman.toRequestBody("text/plain".toMediaTypeOrNull()),
                        namaLatin.toRequestBody("text/plain".toMediaTypeOrNull()),
                        bitmap.toMultipartBody(),
                        id
                    )

                    if (result.status == "success")
                        retrieveData(userId)
                    else
                        throw Exception(result.message)
                } catch (e: Exception) {
                    Log.d("MainViewModel", "Failure: ${e.message}")
                    errorMessage.value = "Error: ${e.message}"
                }
            }
        }

    fun deleteData(userId: String, tanamanId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TanamanApi.service.deleteTanaman(userId, tanamanId)
                if (result.status == "success") {
                    retrieveData(userId)
                } else {
                    deleteStatus.value = result.message ?: "Gagal menghapus data"
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Delete failure: ${e.message}")
                deleteStatus.value = "Terjadi kesalahan: ${e.message}"
            }
        }
    }

    fun clearDeleteStatus() {
        deleteStatus.value = null
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            "gambar", "image.jpg", requestBody)
    }

    fun clearMessage() {
        errorMessage.value = null
    }
}