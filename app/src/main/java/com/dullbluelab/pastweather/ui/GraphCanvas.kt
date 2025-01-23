package com.dullbluelab.pastweather.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer

data class GraphTempItem(
    val year: Int = 0,
    val years: String = "",
    var high: Double = 0.0,
    var low: Double = 0.0
)

@Composable
fun GraphCanvas(
    tempList: List<GraphTempItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = MaterialTheme.colorScheme.secondary)

    Canvas(
        modifier = modifier
    ) {
        val scale = GraphScale(
            paddingX = 20f,
            paddingY = 20f,
            range = 100f,
            maxTemp = 60f,
            itemCount = tempList.size,
            width = size.width,
            height = size.height
        )

        // draw frame
        var pointY = scale.pointY(40f)
        val startX = scale.pointX(0)
        val endX = scale.pointX(tempList.size)
        drawLine(
            color = Color.Yellow,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 1f
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "40℃",
            topLeft = Offset(0f, pointY),
            style = textStyle,
        )
        pointY = scale.pointY(0f)
        drawLine(
            color = Color.Gray,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 2f
        )
        drawText(
            textMeasurer, "0℃",
            style = textStyle,
            topLeft = Offset(0f, pointY)
        )
        pointY = scale.pointY(20f)
        drawLine(
            color = Color.Green,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 2f
        )
        drawText(
            textMeasurer, "20℃",
            style = textStyle,
            topLeft = Offset(0f, pointY)
        )
        pointY = scale.pointY(-20f)
        drawLine(
            color = Color.Cyan,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 2f
        )
        drawText(
            textMeasurer, "-20℃",
            style = textStyle,
            topLeft = Offset(0f, pointY)
        )

        var prev: GraphItem? = null
        var pointX = scale.paddingX

        for (item in tempList) {
            val point = scale.graphItem(item, pointX)
            prev?.let {
                drawLine(
                    color = Color.Red,
                    start = Offset(it.highX, it.highY),
                    end = Offset(point.highX, point.highY),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.Blue,
                    start = Offset(it.lowX, it.lowY),
                    end = Offset(point.lowX, point.lowY),
                    strokeWidth = 3f
                )
            }
            prev = point
            pointX += scale.pitchX
        }
    }
}

private data class GraphItem(
    val highX: Float,
    val highY: Float,
    val lowX: Float,
    val lowY: Float
)

private class GraphScale(
    val paddingX: Float = 20f,
    val paddingY: Float = 20f,
    val range: Float = 100f,
    val maxTemp: Float = 60f,
    val itemCount: Int,
    val width: Float,
    val height: Float
) {
    val pitchX = (width - paddingX * 2f) / (itemCount - 1).toFloat()
    val pitchY = (height - paddingY * 2f) / range

    fun graphItem(item: GraphTempItem, pointX: Float): GraphItem
            = GraphItem(
        highX = pointX,
        highY = pointY(item.high.toFloat()),
        lowX = pointX,
        lowY = pointY(item.low.toFloat())
    )

    fun pointY(temp: Float): Float = paddingY + (maxTemp - temp) * pitchY

    fun pointX(count: Int) = paddingX + count.toFloat() * pitchX
}
