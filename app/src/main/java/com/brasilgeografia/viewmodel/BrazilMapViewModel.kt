package com.brasilgeografia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brasilgeografia.data.repository.MalhaRepository
import com.brasilgeografia.ui.screens.MapUiState
import com.brasilgeografia.util.parseBrazilSVGFromBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrazilMapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MalhaRepository()
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMap()
    }

    private fun loadMap() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getMalhaPaisComCache(context = getApplication()).also {
                        Log.d("Loading", "Bytes recebidos: ${it.imageBytes?.size ?: 0}")
                    }
                }

                if (response.isImage && response.imageBytes != null) {
                    val states = withContext(Dispatchers.Default) {
                        parseBrazilSVGFromBytes(response.imageBytes).also {
                            Log.d("Parsing", "${it.size} estados carregados")
                        }
                    }
                    _uiState.value = MapUiState.Success(states)
                }
            } catch (e: Exception) {
                Log.e("Error", "Erro no carregamento", e)
            }
        }
    }
}