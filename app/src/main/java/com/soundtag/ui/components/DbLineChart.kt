package com.soundtag.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.soundtag.ui.theme.SoundTagBackground
import com.soundtag.ui.theme.SoundTagBorder
import com.soundtag.ui.theme.SoundTagGreen
import com.soundtag.ui.theme.SoundTagSurface

@Composable
fun DbLineChart(
    dbHistory: List<Float>,
    modifier: Modifier = Modifier,
    maxDb: Float = 40f
) {
    if (dbHistory.size < 2) {
        // Empty state
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SoundTagSurface)
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "Recording\u2026",
                fontSize = 13.sp,
                color = com.soundtag.ui.theme.SoundTagTextTertiary
            )
        }
        return
    }

    val effectiveMax = dbHistory.max().coerceAtLeast(maxDb)
    val steps = 4

    val pointsData = dbHistory.mapIndexed { index, db ->
        Point(index.toFloat(), db)
    }

    val xAxisData = AxisData.Builder()
        .axisStepSize(if (dbHistory.size > 1) (280 / dbHistory.size.coerceAtLeast(1)).coerceAtLeast(5).dp else 30.dp)
        .steps(dbHistory.size - 1)
        .backgroundColor(SoundTagSurface)
        .axisLineColor(SoundTagBorder)
        .axisLabelColor(SoundTagBorder)
        .labelData { "" }
        .build()

    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(SoundTagSurface)
        .axisLineColor(SoundTagBorder)
        .axisLabelColor(com.soundtag.ui.theme.SoundTagTextTertiary)
        .labelAndAxisLinePadding(12.dp)
        .labelData { i ->
            val yScale = effectiveMax / steps
            "${(i * yScale).toInt()}"
        }
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    lineStyle = LineStyle(
                        color = SoundTagGreen,
                        width = 3f
                    ),
                    intersectionPoint = IntersectionPoint(
                        color = SoundTagGreen,
                        radius = 3.dp
                    ),
                    shadowUnderLine = ShadowUnderLine(
                        alpha = 0.2f,
                        brush = Brush.verticalGradient(
                            colors = listOf(SoundTagGreen.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(
            color = SoundTagBorder.copy(alpha = 0.3f)
        ),
        backgroundColor = SoundTagSurface,
        containerPaddingEnd = 0.dp,
        paddingRight = 0.dp
    )

    LineChart(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp)),
        lineChartData = lineChartData
    )
}
