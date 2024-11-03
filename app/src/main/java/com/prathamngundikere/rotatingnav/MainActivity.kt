package com.prathamngundikere.rotatingnav

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prathamngundikere.rotatingnav.ui.theme.RotatingNavTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RotatingNavTheme {

                val context = applicationContext

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(30.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val buttonIcons = listOf(
                        Icons.Default.Create,
                        Icons.Default.Add,
                        Icons.Default.Done,
                        Icons.Default.Build,
                        Icons.Default.DateRange,
                        Icons.Default.Delete
                    )
                    val centerButton = Icons.Default.Settings
                    RotatingButtonRing(
                        centerIcon = centerButton,
                        buttonIcons = buttonIcons,
                        onCenterClick = {
                            Toast.makeText(context, "Center Button Clicked", Toast.LENGTH_SHORT).show()
                        },
                        onButtonClick = { index ->
                            Toast.makeText(context, "Button $index clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RotatingButtonRing(
    centerIcon: ImageVector,
    buttonIcons: List<ImageVector>,
    onCenterClick: () -> Unit,
    onButtonClick: (Int) -> Unit, // For identifying button index
    modifier: Modifier = Modifier,
    buttonSize: Dp = 40.dp,
    centerButtonSize: Dp = 60.dp,
    distanceFromCenter: Float = 100f
) {
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }
    var initialAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var touchX by remember { mutableFloatStateOf(0f) }
    var touchY by remember { mutableFloatStateOf(0f) }
    var centerX by remember { mutableFloatStateOf(0f) }
    var centerY by remember { mutableFloatStateOf(0f) }

    // Speed up auto-rotation and make it continuous
    val autoRotateAngle by animateFloatAsState(
        targetValue = if (isDragging) accumulatedRotation else accumulatedRotation + 2f,
        animationSpec = tween(
            durationMillis = 50,
            easing = LinearEasing
        ),
        label = ""
    )

    // Apply auto-rotation when not dragging
    LaunchedEffect(autoRotateAngle) {
        if (!isDragging) accumulatedRotation = autoRotateAngle
    }

    val diameter = (distanceFromCenter * 2).dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(diameter) // Set the size of the Box to the diameter of the circular layout
            .offset(y = diameter / 2) // Offset to push half the Box below the screen
            .onGloballyPositioned {
                val bounds = it.boundsInWindow()
                centerX = bounds.size.width / 2f
                centerY = bounds.size.height / 2f
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        initialAngle = -atan2(centerX - touchX, centerY - touchY) * (180 / Math.PI).toFloat()
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                ) { change, _ ->
                    touchX = change.position.x
                    touchY = change.position.y
                    val currentAngle = -atan2(centerX - touchX, centerY - touchY) * (180 / PI).toFloat()
                    val angleDifference = currentAngle - initialAngle
                    accumulatedRotation += angleDifference
                    initialAngle = currentAngle
                }
            }
    ) {
        // Center Button
        Icon(
            imageVector = centerIcon,
            contentDescription = "Center Button",
            modifier = Modifier
                .size(centerButtonSize)
                .clickable { onCenterClick() },
            tint = Color.White
        )

        // Circular Buttons
        buttonIcons.forEachIndexed { index, icon ->
            val angle = (index * (360f / buttonIcons.size) + accumulatedRotation) % 360f
            val xOffset = cos(Math.toRadians(angle.toDouble())) * distanceFromCenter
            val yOffset = sin(Math.toRadians(angle.toDouble())) * distanceFromCenter

            Icon(
                imageVector = icon,
                contentDescription = "Button $index",
                modifier = Modifier
                    .size(buttonSize)
                    .offset(x = xOffset.dp, y = yOffset.dp)
                    .clickable { onButtonClick(index) },
                tint = Color.White
            )
        }
    }
}
