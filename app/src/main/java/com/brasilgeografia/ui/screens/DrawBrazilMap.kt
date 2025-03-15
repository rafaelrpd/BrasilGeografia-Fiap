package com.brasilgeografia.ui.screens

import android.graphics.Region
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.brasilgeografia.data.model.BrazilState

@Composable
fun DrawBrazilMap(statesList: List<BrazilState>) {
    var highlightedState by remember { mutableStateOf<BrazilState?>(null) }

    val pathCache = remember(statesList) {
        statesList.associate { state ->
            state.pathData to try {
                Path().apply {
                    PathParser().parsePathString(state.pathData).toPath(this)
                }
            } catch (e: Exception) {
                Path()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp)
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        highlightedState = statesList.firstOrNull { state ->
                            pathCache[state.pathData]?.let { path ->
                                isPointInBrazilStatePath(path, offset)
                            } ?: false
                        }
                    }
                }
        ) {
            val combinedBounds = android.graphics.RectF()
            pathCache.forEach { (_, path) ->
                val rect = android.graphics.RectF()
                path.asAndroidPath().computeBounds(rect, true)
                combinedBounds.union(rect)
            }

            val svgWidth = combinedBounds.width()
            val svgHeight = combinedBounds.height()

            val scaleX = size.width / svgWidth
            val scaleY = size.height / svgHeight
            val scale = minOf(scaleX, scaleY)

            val offsetX = (size.width - svgWidth * scale) / 2f
            val offsetY = (size.height - svgHeight * scale) / 2f

            pathCache.forEach { (_, path) ->
                withTransform({
                    translate(left = offsetX, top = offsetY)
                    scale(scale)
                    translate(-combinedBounds.left, -combinedBounds.top)
                }) {
                    drawPath(
                        path = path,
                        color = Color.LightGray,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        highlightedState?.let { st ->
            Text(
                text = "Estado selecionado: ${st.name}",
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


private fun isPointInBrazilStatePath(path: Path, offset: Offset): Boolean {
    val androidPath = path.asAndroidPath()
    val bounds = android.graphics.RectF()
    androidPath.computeBounds(bounds, true)

    return try {
        val region = Region().apply {
            setPath(
                androidPath,
                Region(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt()
                )
            )
        }
        region.contains(offset.x.toInt(), offset.y.toInt())
    } catch (e: Exception) {
        false
    }
}