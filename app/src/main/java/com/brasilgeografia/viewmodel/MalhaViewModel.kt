package com.brasilgeografia.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brasilgeografia.data.model.MalhaResponse
import com.brasilgeografia.data.repository.MalhaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class MalhaViewModel : ViewModel() {
    val malhaState = mutableStateOf<MalhaResponse?>(null)
    val errorState = mutableStateOf("")

    private val repository = MalhaRepository()

    fun fetchMalhaPais() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.fetchMalhaPais()
                }
                malhaState.value = result
            }catch (e: CancellationException) {
                Log.d("Cancel", "Operação cancelada")
            }catch (e: Exception) {
                errorState.value = e.message ?: "Erro desconhecido"
            }
        }
    }
}
