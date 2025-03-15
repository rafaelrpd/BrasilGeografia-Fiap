package com.brasilgeografia.ui.screens

import com.brasilgeografia.data.model.BrazilState

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(val states: List<BrazilState>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}