package com.brasilgeografia.util

import android.util.Log
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import com.brasilgeografia.data.model.BrazilState
import com.brasilgeografia.data.model.ibgeStateNames
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

fun parseBrazilSVGFromBytes(svgBytes: ByteArray): List<BrazilState> {
    val inputStream = ByteArrayInputStream(svgBytes)
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)

    document.documentElement.normalize()

    val nodeList = document.getElementsByTagName("path")
    val result = mutableListOf<BrazilState>()

    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        if (node is Element) {
            val idAttr = node.getAttribute("id")
            val dAttr = node.getAttribute("d")

            val ibgeCode = idAttr.toIntOrNull()

            if (ibgeCode != null && dAttr.isNotEmpty()) {
                val stateName = ibgeStateNames[ibgeCode] ?: "Desconhecido ($ibgeCode)"
                result.add(
                    BrazilState(
                        ibgeCode = ibgeCode,
                        name = stateName,
                        pathData = dAttr
                    )
                )
            } else {
                Log.w("parseBrazilSVG", "Ignorando path com id=$idAttr, d=$dAttr")
            }
        }
    }
    return result
}

fun BrazilState.toPath(): Path? {
    return try {
        val path = Path()
        PathParser().parsePathString(this.pathData).toPath(path)
        path
    } catch (e: Exception) {
        Log.e("toPath", "Erro ao parsear pathData: ${this.pathData}", e)
        null
    }
}