package com.brasilgeografia.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun saveBytes(context: Context, fileName: String, bytes: ByteArray) {
        val file = File(context.filesDir, fileName)
        try {
            FileOutputStream(file).use { it.write(bytes) }
        } catch (e: Exception) {
            Log.e("FileUtils", "Erro ao salvar arquivo", e)
        }
    }

    fun loadBytes(context: Context, fileName: String): ByteArray? {
        return try {
            File(context.filesDir, fileName).takeIf { it.exists() }?.readBytes()
        } catch (e: Exception) {
            Log.e("FileUtils", "Erro ao ler arquivo", e)
            null
        }
    }
}