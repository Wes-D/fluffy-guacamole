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

    val tiles by remember { derivedStateOf { gameLogic.getTiles() } }

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
        GameBoard(tiles = tiles, gridSize = 4, modifier = Modifier.fillMaxWidth().weight(1f))

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

@Composable
fun GameBoard(tiles: List<Tile>, gridSize: Int = 4, @SuppressLint("ModifierParameter") modifier: Modifier = Modifier) {
    // Define the colors for the board background and border
    val boardBackgroundColor = Color(0xFFFFE0E0) // Light pink
    val boardBorderColor = Color(0xFFFFC0C0) // Deeper shade of pink for the border

    Box(
        modifier = modifier
            .padding(0.dp) // Outer padding to give space around the board
            .border(4.dp, boardBorderColor) // Border with a deeper pink color
            .background(boardBackgroundColor) // Light pink background
            .aspectRatio(1f) // Maintain a square aspect ratio
            .padding(0.dp), // No extra inner padding
        contentAlignment = Alignment.Center
    ) {
        // Calculate tile size based on the available space in the box
        val tileSpacing = 4.dp
        val tileSize = (380.dp - (tileSpacing * (gridSize - 1))) / gridSize

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            for (tile in tiles) {
                // Reverse the y-coordinate to correctly map the tiles to the screen
                val actualX = tile.x
                val actualY = gridSize - 1 - tile.y
                AnimatedTile(tile = tile, tileSize = tileSize, targetX = actualX, targetY = actualY)
            }

        }
    }
}
/*
@Composable
fun AnimatedTile(tile: Tile, tileSize: Dp) {
    val xPos = remember { Animatable(tile.y.toFloat()) }
    val yPos = remember { Animatable(tile.x.toFloat()) }

    LaunchedEffect(tile.x, tile.y) {
        println("Animating tile to position: x=${tile.y}, y=${tile.x}")

        // Animate the tile's x and y positions to their new target values
        xPos.animateTo(tile.y.toFloat(), animationSpec = tween(durationMillis = 200))
        yPos.animateTo(tile.x.toFloat(), animationSpec = tween(durationMillis = 200))

        println("xPos: ${xPos.value}, yPos: ${yPos.value}")
    }

    val tileSpacing = 4.dp
    val offsetX = (xPos.value * (tileSize + tileSpacing))
    val offsetY = (yPos.value * (tileSize + tileSpacing))

    println("OffsetX: $offsetX, OffsetY: $offsetY")

    Box(
        modifier = Modifier
            .offset(offsetX, offsetY)
            .size(tileSize)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
*/

@Composable
fun AnimatedTile(tile: Tile, tileSize: Dp, targetX: Int, targetY: Int) {
    // Convert target positions from grid coordinates to pixel offsets
    val targetOffsetX = with(LocalDensity.current) { (targetX * tileSize.toPx()).roundToInt() }
    val targetOffsetY = with(LocalDensity.current) { (targetY * tileSize.toPx()).roundToInt() }

    // Animatable for the offset position
    val animatableX = remember { Animatable(targetOffsetX.toFloat()) }
    val animatableY = remember { Animatable(targetOffsetY.toFloat()) }

    // Launch the animations
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

    Box(
        modifier = Modifier
            .size(tileSize) // Tile size
            .offset { IntOffset(animatableX.value.roundToInt(), animatableY.value.roundToInt()) } // Apply the animated offset
            .background(Color(0xFFFFA07A), shape = RoundedCornerShape(8.dp)) // Tile color and shape
            .padding(4.dp) // Inner padding inside the tile
            .border(2.dp, Color(0xFFFF6347), shape = RoundedCornerShape(8.dp)) // Border color (Tomato)
    ) {
        // Content inside the tile (e.g., a number)
        Text(
            text = tile.value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}





