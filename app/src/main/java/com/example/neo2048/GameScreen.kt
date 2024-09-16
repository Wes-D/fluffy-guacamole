package com.example.neo2048

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    gameLogic: GameLogic,
    score: Int,
    onRestart: () -> Unit,
    onReturnToMain: () -> Unit,
    onSwipe: (Direction) -> Unit
) {
    var isGameOver by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isProcessingSwipe by remember { mutableStateOf(false) }

    LaunchedEffect(gameLogic) {
        if (gameLogic.isGameOver()) {
            isGameOver = true
        }
    }

    val tiles by remember { derivedStateOf { gameLogic.tiles } }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (isProcessingSwipe || isGameOver) return@detectDragGestures
                    val (x, y) = dragAmount
                    change.consume()

                    scope.launch {
                        isProcessingSwipe = true

                        when {
                            x > 20 -> onSwipe(Direction.RIGHT)
                            x < -20 -> onSwipe(Direction.LEFT)
                            y > 20 -> onSwipe(Direction.DOWN)
                            y < -20 -> onSwipe(Direction.UP)
                        }

                        // Debounce: Ignore subsequent swipes for a short time
                        delay(300)
                        isProcessingSwipe = false
                    }
                }
            },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score Display
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Game Board
        GameBoard(tiles = tiles, gridSize = 4)

        // Directional Buttons
        DirectionalButtons(
            onMove = { direction -> onSwipe(direction) }
        )

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onRestart) {
                Text("Restart")
            }
            Button(onClick = onReturnToMain) {
                Text("Return to Main")
            }
        }
    }

    if (isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text("No more moves are possible.") },
            confirmButton = {
                Button(onClick = {
                    onRestart()
                    isGameOver = false
                }) {
                    Text("Restart")
                }
            },
            dismissButton = {
                Button(onClick = onReturnToMain) {
                    Text("Return to Main")
                }
            }
        )
    }
}

@Composable
fun GameBoard(
    tiles: List<TileMovement>,
    gridSize: Int = 4,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val boardBackgroundColor = Color(0xFFFFE0E0) // Color of the board
    val boardBorderColor = Color(0xFFFFC0C0)     // Color of the border

    Box(
        modifier = modifier
            .padding(0.dp)
            .border(4.dp, boardBorderColor)
            .background(boardBackgroundColor)
            .aspectRatio(1f)
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        // Calculate tile size and spacing based on available space
        val tileSpacing = 4.dp
        val tileSize = (380.dp - (tileSpacing * (gridSize - 1))) / gridSize

        Box(modifier = Modifier.fillMaxSize()) {
            tiles.forEach { tile ->
                AnimatedTile(
                    tile = tile,
                    tileSize = tileSize
                )
            }
        }
    }
}


@Composable
fun AnimatedTile(tile: TileMovement, tileSize: Dp) {
    // Convert start and target positions to pixel offsets
    val startOffsetX = with(LocalDensity.current) { (tile.oldY * tileSize.toPx()).roundToInt() }
    val startOffsetY = with(LocalDensity.current) { (tile.oldX * tileSize.toPx()).roundToInt() }
    val targetOffsetX = with(LocalDensity.current) { (tile.newY * tileSize.toPx()).roundToInt() }
    val targetOffsetY = with(LocalDensity.current) { (tile.newX * tileSize.toPx()).roundToInt() }

    // Animatable for the offset position
    val animatableX = remember { Animatable(startOffsetX.toFloat()) }
    val animatableY = remember { Animatable(startOffsetY.toFloat()) }

    // Scale animation for new or merged tiles
    val animatableScale = remember { Animatable(1f) }

    // Single LaunchedEffect to control animation sequence
    LaunchedEffect(targetOffsetX, targetOffsetY) {
        // Step 1: Animate sliding to new position
        animatableX.animateTo(targetOffsetX.toFloat(), tween(300, easing = FastOutSlowInEasing))
        animatableY.animateTo(targetOffsetY.toFloat(), tween(300, easing = FastOutSlowInEasing))

        // Step 2: After sliding, trigger merge animation if necessary
        if (tile.isMerged) {
            animatableScale.animateTo(1.2f, tween(100))
            animatableScale.animateTo(1f, tween(100))
        }

        // Step 3: Animate the appearance of new tiles (scale-up effect)
        if (tile.isNew) {
            animatableScale.animateTo(1.2f, tween(150))
            animatableScale.animateTo(1f, tween(150))
        }
    }

    // Render the tile with animations applied
    Box(
        modifier = Modifier
            .size(tileSize)
            .offset { IntOffset(animatableX.value.roundToInt(), animatableY.value.roundToInt()) }
            .scale(animatableScale.value)
            .background(Color(0xFFFFA07A), shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
            .border(2.dp, Color(0xFFFF6347), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
fun DirectionalButtons(onMove: (Direction) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { onMove(Direction.UP) }) {
                Text("Up")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onMove(Direction.LEFT) }) {
                Text("Left")
            }
            Button(onClick = { onMove(Direction.DOWN) }) {
                Text("Down")
            }
            Button(onClick = { onMove(Direction.RIGHT) }) {
                Text("Right")
            }
        }
    }
}
