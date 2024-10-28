package com.example.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.game.ui.theme.GameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTheme {
                Navigation()
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = androidx.navigation.compose.rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("game") { GameScreen(navController) }
        composable("gameOver/{score}", arguments = listOf(androidx.navigation.navArgument("score") { type =
            androidx.navigation.NavType.IntType })) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            GameOverScreen(navController, score)
        }
    }
}
@Composable
fun HomeScreen(navController: NavHostController) {
    Box(modifier = Modifier
        .fillMaxSize()
        .paint(painterResource(id = R.drawable.background), contentScale = ContentScale.Crop)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Alien Invasion",
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.navigate("game") }) {
                Text("Start Game")
            }
        }
    }
}

@Composable
fun GameOverScreen(navController: NavHostController, score: Int) {
    Box(modifier = Modifier
        .fillMaxSize()
        .paint(painterResource(id = R.drawable.background), contentScale = ContentScale.Crop)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Over!",
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text="Your Score: $score",color = Color.White)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.navigate("home") }) {
                Text("Play Again")
            }
        }
    }
}