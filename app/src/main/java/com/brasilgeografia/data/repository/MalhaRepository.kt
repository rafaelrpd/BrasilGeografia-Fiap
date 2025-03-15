package com.brasilgeografia.data.repository

import android.content.Context
import android.util.Log
import com.brasilgeografia.data.model.MalhaMetadata
import com.brasilgeografia.data.model.MalhaResponse
import com.brasilgeografia.data.network.RetrofitInstance
import com.brasilgeografia.util.FileUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class MalhaRepository {

    private suspend fun loadFromCache(context: Context): MalhaResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = FileUtils.loadBytes(context, "mapa_brasil.svg")
                if (bytes != null && bytes.isNotEmpty()) {
                    Log.d("CacheDebug", "Cache válido: ${bytes.size} bytes")
                    MalhaResponse(isImage = true, imageBytes = bytes)
                } else {
                    Log.d("CacheDebug", "Cache vazio ou inexistente")
                    null
                }
            } catch (e: Exception) {
                Log.e("CacheDebug", "Erro ao ler cache", e)
                null
            }
        }
    }

    private suspend fun saveToCache(context: Context, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("CacheSave", "Iniciando salvamento...")

                val file = File(context.filesDir, "mapa_brasil.svg").apply {
                    if (!exists()) createNewFile()
                }

                file.writeBytes(bytes)
                Log.d("CacheSave", "Cache salvo com sucesso. Novo tamanho: ${file.length()} bytes")

            } catch (e: Exception) {
                Log.e("CacheSaveError", "Falha ao salvar", e)
                throw e
            }
        }
    }

    suspend fun fetchMalhaPais(): MalhaResponse {
        return try {
            Log.d("Network", "Iniciando requisição da malha...")

            val response = withTimeout(15000) { // Timeout de 15 segundos
                RetrofitInstance.api.getMalhaPais()
            }

            Log.d("Network", "Resposta recebida. Código: ${response.code()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Sem corpo de erro"
                Log.e("Network", "Erro HTTP ${response.code()} - $errorBody")
                throw HttpException(response)
            }

            val contentType = response.headers()["Content-Type"]?.lowercase()
            val responseBody = response.body()?.bytes()

            when {
                contentType?.contains("image/svg+xml") == true && responseBody != null -> {
                    Log.d("Network", "SVG recebido (${responseBody.size} bytes)")
                    MalhaResponse(
                        isImage = true,
                        imageBytes = responseBody
                    )
                }
                contentType?.contains("application/json") == true && responseBody != null -> {
                    Log.d("Network", "JSON recebido (${responseBody.size} bytes)")
                    try {
                        val jsonString = responseBody.decodeToString()
                        val metadata = Gson().fromJson(jsonString, MalhaMetadata::class.java)
                        MalhaResponse(
                            isImage = false,
                            metadata = metadata
                        )
                    } catch (e: Exception) {
                        Log.e("Network", "Erro ao parsear JSON", e)
                        MalhaResponse(
                            isImage = false,
                            errorMessage = "Formato de resposta inválido"
                        )
                    }
                }
                else -> {
                    Log.w("Network", "Tipo de conteúdo não suportado: $contentType")
                    MalhaResponse(
                        isImage = false,
                        errorMessage = "Tipo de conteúdo não suportado: $contentType"
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("Network", "Erro de rede", e)
            MalhaResponse(
                isImage = false,
                errorMessage = "Falha na conexão: ${e.localizedMessage}"
            )
        } catch (e: HttpException) {
            Log.e("Network", "Erro HTTP", e)
            MalhaResponse(
                isImage = false,
                errorMessage = "Erro do servidor: ${e.code()}"
            )
        } catch (e: TimeoutCancellationException) {
            Log.e("Network", "Timeout na requisição")
            MalhaResponse(
                isImage = false,
                errorMessage = "Tempo excedido na requisição"
            )
        } catch (e: Exception) {
            Log.e("Network", "Erro inesperado", e)
            MalhaResponse(
                isImage = false,
                errorMessage = "Erro desconhecido: ${e.javaClass.simpleName}"
            )
        }
    }

    suspend fun getMalhaPaisComCache(context: Context): MalhaResponse {
        return try {
            // Passo 1: Tentar carregar do cache
            val cached = loadFromCache(context)
            if (cached != null) {
                Log.d("CacheFlow", "Retornando cache existente")
                return cached
            }

            // Passo 2: Buscar da rede se cache não existir
            Log.d("CacheFlow", "Buscando da rede...")
            val networkResponse = fetchMalhaPais()

            // Passo 3: Salvar apenas se for imagem válida
            if (networkResponse.isImage && networkResponse.imageBytes != null) {
                Log.d("CacheFlow", "Salvando novo cache")
                FileUtils.saveBytes(context, "mapa_brasil.svg", networkResponse.imageBytes)
            }

            // Passo 4: Retornar resposta da rede
            networkResponse

        } catch (e: Exception) {
            Log.e("CacheFlow", "Falha geral: ${e.message}")
            MalhaResponse(isImage = false, metadata = null)
        }
    }

    private suspend fun fetchAndSaveNetworkResponse(context: Context): MalhaResponse {
        return fetchMalhaPais().also { response ->
            if (response.isImage && response.imageBytes != null) {
                withContext(Dispatchers.IO) {
                    FileUtils.saveBytes(context, "mapa_brasil.svg", response.imageBytes)
                }
            }
        }
    }
}