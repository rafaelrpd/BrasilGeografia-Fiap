package com.brasilgeografia.data.model

data class MalhaResponse(
    val isImage: Boolean,
    val imageBytes: ByteArray? = null,
    val metadata: MalhaMetadata? = null,
    val errorMessage: String? = null
)

data class MalhaMetadata(
    val nome: String,
    val dimensoes: String,
    val tipoMIME: String
)
