package com.example.neo2048

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import com.example.neo2048.ui.theme.Neo2048Theme
import androidx.compose.animation.core.*
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
                            x > 100 -> onSwipe(Direction.RIGHT)
                            x < -100 -> onSwipe(Direction.LEFT)
                            y > 100 -> onSwipe(Direction.DOWN)
                            y < -100 -> onSwipe(Direction.UP)
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
        GameBoard(
            board = gameLogic.board,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

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
fun GameBoard(board: Array<Array<Int>>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in board) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (cell in row) {
                    val animatedValue by animateIntAsState(targetValue = cell, label = "")

                    val backgroundColor by animateColorAsState(
                        targetValue = when (animatedValue) {
                            2 -> Color(0xFFEEE4DA)
                            4 -> Color(0xFFEDE0C8)
                            8 -> Color(0xFFF2B179)
                            16 -> Color(0xFFF59563)
                            32 -> Color(0xFFF67C5F)
                            64 -> Color(0xFFF65E3B)
                            128 -> Color(0xFFEDCF72)
                            256 -> Color(0xFFEDCC61)
                            512 -> Color(0xFFEDC850)
                            1024 -> Color(0xFFEDC53F)
                            2048 -> Color(0xFFEDC22E)
                            else -> Color(0xFFCDC1B4)
                        },
                        animationSpec = tween(durationMillis = 300), label = ""
                    )
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(80.dp)
                            .background(backgroundColor)
                            .animateContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (animatedValue == 0) "" else animatedValue.toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}


enum class Direction { LEFT, RIGHT, UP, DOWN }

