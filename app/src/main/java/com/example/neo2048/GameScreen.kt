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
fun GameBoard(tiles: List<TileMovement>, gridSize: Int = 4, @SuppressLint("ModifierParameter") modifier: Modifier = Modifier) {

    val boardBackgroundColor = Color(0xFFFFE0E0) // Color of the board
    val boardBorderColor = Color(0xFFFFC0C0) // Color of the boarder

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
                    tileSize = tileSize,
                    startX = tile.oldY, // Start from oldX, oldY
                    startY = tile.oldX,
                    targetX = tile.newY, // Target to newX, newY
                    targetY = tile.newX
                )
            }
        }
    }
}

@Composable
fun AnimatedTile(tile: TileMovement, tileSize: Dp, startX: Int, startY: Int, targetX: Int, targetY: Int) {
    // Convert start and target positions to pixel offsets
    val startOffsetX = with(LocalDensity.current) { (startX * tileSize.toPx()).roundToInt() }
    val startOffsetY = with(LocalDensity.current) { (startY * tileSize.toPx()).roundToInt() }
    val targetOffsetX = with(LocalDensity.current) { (targetX * tileSize.toPx()).roundToInt() }
    val targetOffsetY = with(LocalDensity.current) { (targetY * tileSize.toPx()).roundToInt() }

    // Animatable for the offset position
    val animatableX = remember { Animatable(startOffsetX.toFloat()) }
    val animatableY = remember { Animatable(startOffsetY.toFloat()) }

    // Scale animation for new or merged tiles
    val animatableScale = remember { Animatable(1f) }

    // Launch the animations for movement
    LaunchedEffect(targetX, targetY) {
        animatableX.animateTo(
            targetOffsetX.toFloat(),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        animatableY.animateTo(
            targetOffsetY.toFloat(),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    // Launch the animation for scale (on merge or new tile)
    LaunchedEffect(tile.isMerged, tile.isNew) {
        if (tile.isMerged || tile.isNew) {
            animatableScale.animateTo(1.2f, animationSpec = tween(100))
            animatableScale.animateTo(1f, animationSpec = tween(100))
        }
    }

    // Box to represent the tile with animation
    Box(
        modifier = Modifier
            .size(tileSize)
            .offset { IntOffset(animatableX.value.roundToInt(), animatableY.value.roundToInt()) } // Apply animated offset
            .scale(animatableScale.value) // Apply animated scale
            .background(Color(0xFFFFA07A), shape = RoundedCornerShape(8.dp)) // Tile color and shape
            .padding(4.dp)
            .border(2.dp, Color(0xFFFF6347), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
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
