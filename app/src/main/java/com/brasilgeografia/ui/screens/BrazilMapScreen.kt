package com.brasilgeografia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brasilgeografia.viewmodel.BrazilMapViewModel

@Composable
fun BrazilMapScreen(
    modifier: Modifier = Modifier,
    viewModel: BrazilMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        when (val currentState = uiState) {
            is MapUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            is MapUiState.Error -> {
                Text(
                    text = "Erro: ${currentState.message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is MapUiState.Success -> {
                DrawBrazilMap(currentState.states)
            }
        }
    }
}