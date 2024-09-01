package com.example.neo2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import com.example.neo2048.ui.theme.Neo2048Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Neo2048Theme {
                val navController = rememberNavController()
                val gameLogic = remember { GameLogic() }
                var score by remember { mutableIntStateOf(gameLogic.score) }

                GameScreen(
                    gameLogic = gameLogic,
                    score = score,
                    onRestart = {
                        gameLogic.resetGame()
                        score = gameLogic.score
                    },
                    onReturnToMain = { navController.navigate("landing") },
                    onSwipe = { direction ->
                        when (direction) {
                            Direction.LEFT -> gameLogic.moveLeft()
                            Direction.RIGHT -> gameLogic.moveRight()
                            Direction.UP -> gameLogic.moveUp()
                            Direction.DOWN -> gameLogic.moveDown()
                        }
                        score = gameLogic.score
                    }
                )
            }
        }
    }
}
