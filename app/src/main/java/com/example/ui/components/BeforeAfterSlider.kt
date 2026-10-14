package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BeforeAfterSlider(
    beforeImageRes: Int,
    afterImageRes: Int,
    modifier: Modifier = Modifier
) {
    var sliderRatio by remember { mutableFloatStateOf(0.5f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900)
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (containerSize.width > 0) {
                        val newRatio = (sliderRatio + dragAmount.x / containerSize.width)
                        sliderRatio = newRatio.coerceIn(0.05f, 0.95f)
                    }
                }
            }
            .testTag("before_after_slider")
    ) {
        // Base: After Image (Full width background)
        Image(
            painter = painterResource(id = afterImageRes),
            contentDescription = "After Redesign",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay: Before Image clipped to slider ratio
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = sliderRatio)
                .clip(RectangleShape)
        ) {
            Image(
                painter = painterResource(id = beforeImageRes),
                contentDescription = "Before Renovation",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (containerSize.width > 0) (containerSize.width / LocalDensityProvider.density).dp else 400.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Divider Line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .align(Alignment.CenterStart)
                .offset(x = if (containerSize.width > 0) (containerSize.width * sliderRatio / LocalDensityProvider.density).dp - 1.5.dp else 190.dp)
                .background(Color.White)
        )

        // Grab Handle
        Surface(
            shape = CircleShape,
            color = Slate900,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.CenterStart)
                .offset(x = if (containerSize.width > 0) (containerSize.width * sliderRatio / LocalDensityProvider.density).dp - 22.dp else 170.dp)
                .testTag("slider_handle")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Drag comparison slider",
                    tint = BlueprintCyanLight,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Before Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Slate900.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "BEFORE",
                color = Slate200,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // After Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = BlueprintCyan.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Text(
                text = "AI REDESIGN",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Interaction hint banner at bottom
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "◀ Drag handle to compare ▶",
                color = Slate200,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }
    }
}

// Density helper
object LocalDensityProvider {
    var density: Float = 2.625f
}
