package com.example.game

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlin.random.Random

// Alien class
data class Alien(
    var x: MutableState<Float>,
    var y: MutableState<Float>,
    var launchx: MutableState<Float> = mutableStateOf(0f),
    var launchy: MutableState<Float> = mutableStateOf(0f),
    var row: Int,
    var type: AlienType = AlienType.Normal,
    var isDestroyed: Boolean = false, // Add a property to track if alien is destroyed
    var isDetached: Boolean = false,
    var alienPath: MutableList<Offset> = mutableListOf()
)

enum class AlienType {
    Normal,
    Fast,
    Tough
}

// Bot class
data class Bot(
    var x: MutableState<Float>,
    var y: MutableState<Float>,
    var isAlive: Boolean = true
)

// Projectile class
data class Projectile(
    var x: MutableState<Float>,
    var y: MutableState<Float>,
    var isAlienProjectile: Boolean,
    var speed: Float = 40f
) {
    fun move() {
        if (isAlienProjectile) {
            y.value += speed
        } else {
            y.value -= speed
        }
    }
}

@Composable
fun GameScreen(navController: NavHostController) {

    var aliens by remember { mutableStateOf(mutableListOf<Alien>()) }
    var bot by remember { mutableStateOf(Bot(mutableStateOf(300F), mutableStateOf(700F))) } // Set bot initial position
    var projectiles by remember { mutableStateOf(mutableListOf<Projectile>()) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var alienRowOffset by remember { mutableStateOf(0f) }
    var pause by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    var path by remember { mutableStateOf(Path()) }
    var drawing by remember { mutableStateOf(false) }
    var totalPathLength by remember { mutableStateOf(0f) }
    var pathPoints by remember { mutableStateOf(mutableListOf<Offset>()) }
    val maxPathLength = 20000f // Maximum total length of the path
// ... Inside your game screen composable ...

    val updateScore: (Int) -> Unit = { newValue ->
        score += newValue // Modify the score state directly
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .paint(painterResource(id = R.drawable.background), contentScale = ContentScale.Crop)
            ) {
        // ... Game elements ...

        // Draw the path
        if(drawing) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            // Add new touch position to the list of points
                            if (totalPathLength < maxPathLength) {
                                val newPosition = change.position
                                pathPoints.add(newPosition)
                            }

                            // Clear the current path and recreate a smoother path
                            path = Path().apply {
                                addSmoothPath(pathPoints) // Use the function from the previous explanation
                            }

                            // Update total path length
                            totalPathLength += dragAmount.getDistance()
                        }
                    }
            ) {
                // Draw the path using the drawPath method
                drawPath(
                    path = path,
                    color = Color.Blue,
                    style = Stroke(width = 4f) // Stroke style with width for the path
                )
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        for (row in 0..3) {
            for (col in 2..6) {
                aliens.add(Alien(
                    x = mutableStateOf(col * 30f),
                    y = mutableStateOf(row * 30f),
                    row = row
                ))
            }
        }
    }

    var turnCount = 0
    LaunchedEffect(key1 = pause) {
        Log.d("GameScreen", "LaunchedEffect called")
        while (!pause) {
            if(aliens.isEmpty()) {
                pause = true
                navController.navigate("gameOver/${score}")
            }
            else{
            updateGame(aliens, bot, projectiles, score, isGameOver, alienRowOffset, screenWidth, turnCount,pathPoints,updateScore)
            delay(50)
            turnCount++
        }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        aliens.forEach { alien ->
            if (!alien.isDestroyed||!alien.isDetached) { // Only draw if not destroyed
                Image(
                    painter = painterResource(id = R.drawable.alien),
                    contentDescription = "Alien",
                    modifier = Modifier
                        .size(30.dp)
                        .offset(x = (alien.x.value).dp, y = alien.y.value.dp)
                )
            }
            else {
                Image(
                    painter = painterResource(id = R.drawable.alien),
                    contentDescription = "Alien",
                    modifier = Modifier
                        .size(50.dp)
                        .offset(x = (alien.x.value).dp, y = alien.y.value.dp)
                        )
            }
        }

        if (bot.isAlive) { // Only draw if alive
            Image(
                painter = painterResource(id = R.drawable.bot),
                contentDescription = "Bot",
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = bot.x.value.dp, y = bot.y.value.dp)
            )
            Log.d("GameScreen", "Bot x: ${bot.x}")
        }

        projectiles.forEach { projectile ->
            Image(
                painter = painterResource(
                    id = if (projectile.isAlienProjectile) R.drawable.alien_projectile else R.drawable.bot_projectile
                ),
                contentDescription = "Projectile",
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = projectile.x.value.dp, y = projectile.y.value.dp)
            )
        }

        Text(
            text = "Score: ${score}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        if (isGameOver) {
            Text(
                text = "Game Over",
                color = Color.Red,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Button(onClick = { if(! ((aliens.filter { !it.isDetached }.minOf { it.x.value }) - 50f <= 0))for(alien in aliens){if(!alien.isDetached)alien.x.value -= 50f } } ){
                Text("Left")
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (!drawing) {
                Button(onClick = {
                    pathPoints.clear()
                    totalPathLength = 0f
                    path = Path()
                    pause = true
                    drawing = true
                }) {
                    Text("Draw Path")
                }
            }
            else{
                Button(onClick = {
                    pause = false
                    drawing = false
                }) {
                    Text("Continue")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { if(!((aliens.filter { !it.isDetached }.maxOf { it.x.value }) + 50f >=screenWidth)) for(alien in aliens){if(!alien.isDetached)alien.x.value += 50f } }) {
                Text("Right")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { pause = !pause }) {
                Text("Pause")
            }
        }
    }

}

// Update game logic


fun updateGame(
    aliens: MutableList<Alien>,
    bot: Bot,
    projectiles: MutableList<Projectile>,
    score: Int,
    isGameOver: Boolean,
    alienRowOffset: Float,
    screenWidth: Float,
    turnCount: Int,
    path: MutableList<Offset> = mutableListOf(),
    updateScore: (Int) -> Unit
) {
    Log.d("updateGame", "Updating game...")
    botMove(bot,aliens, screenWidth)
    alienMove(aliens, screenWidth, turnCount, path,score,updateScore)
    if(turnCount%20==0) { // To add a projectile and trigger recomposition:
        projectiles.add(Projectile(mutableStateOf(bot.x.value +25), mutableStateOf(bot.y.value-10), false))
    }
    projectilesMove(projectiles)

    checkCollision(aliens, projectiles, score)
}
fun checkCollision(aliens: MutableList<Alien>, projectiles: MutableList<Projectile>, score: Int) {
    val projectileIterator = projectiles.iterator()
    while (projectileIterator.hasNext()) {
        val projectile = projectileIterator.next()
        if (!projectile.isAlienProjectile) {
            for (alien in aliens) {

                if (projectile.y.value >= alien.y.value && projectile.y.value <= alien.y.value + 50 &&
                    projectile.x.value >= alien.x.value && projectile.x.value <= alien.x.value + 50
                ) {
                    alien.isDestroyed = true
                    aliens.remove(alien) // Remove alien from the list
                    projectileIterator.remove() // Safely remove using iterator
                    break // Exit inner loop after removing projectile
                }
            }
        }
    }
}

fun projectilesMove(projectiles: MutableList<Projectile>) {
    projectiles.forEach { projectile ->
        projectile.move()
    }
}

fun alienMove(aliens: MutableList<Alien>, screenWidth: Float,turnCount: Int,path: MutableList<Offset> = mutableListOf(),score: Int,updateScore: (Int) -> Unit) {
    val alienWidth = 50f // Adjust based on your alien's actual width
        aliens.forEach { alien ->
            if (turnCount % 250 == 0) {
                alien.y.value += 50f
            }
            if (Random.nextFloat() < 0.002&&turnCount%2==0){
                if(path.isEmpty()){
                    alien.alienPath =
                        MutableList(100) { index ->
                        Offset(0f, index * 20f)
                    }
                }else {
                    alien.alienPath = path
                    Log.d("alienMove", "Path: $path")
                }
                alien.isDetached=true
                alien.launchx.value = alien.x.value - alien.alienPath[0].x
                alien.launchy.value = alien.y.value - alien.alienPath[0].y
            }
            if(alien.isDetached){
                    Log.d("alienMove", "X: ${alien.x.value} Y: ${alien.y.value}")
                    if (alien.alienPath.isEmpty()) {
                        alien.alienPath =
                            MutableList(100) { index ->
                                Offset(0f, index * 20f)
                            }
                    }
                    else if(turnCount%3==0){
                        alien.x.value = alien.launchx.value + alien.alienPath[0].x
                        alien.y.value = alien.launchy.value + alien.alienPath[0].y
                        alien.alienPath.removeAt(0)
                        Log.d("alienMove", "Path: ${alien.alienPath}")
                    }
            }
            if(alien.y.value > 800) {
                alien.isDestroyed = true
                updateScore(score + 1)
            }
        }
    aliens.removeAll { it.isDestroyed }
}
fun botMove(bot: Bot,aliens: MutableList<Alien>, screenWidth: Float) {
    val botWidth = 50f // Adjust based on your bot's actual width
    var toRight = true
    val random = Random
    var prob = .5
    var turnCount = 0
    if(bot.x.value < screenWidth/2) prob = .25 else prob = .75
    // 1. Calculate a random movement offset
    var speed = 0f
   if(!aliens.isEmpty()) {
        val lowestAlien = aliens.maxBy { it.y.value }
        toRight = bot.x.value < lowestAlien.x.value
        if (toRight) speed = 5f else speed = -5f
        if (bot.x.value == lowestAlien.x.value) speed = 0f
    }
    var newX = bot.x.value + speed
    newX = newX.coerceIn(0f, screenWidth - botWidth)
    bot.x.value = newX
}
fun Path.addSmoothPath(points: List<Offset>) {
    if (points.size < 2) return

    // Move to the first point
    this.moveTo(points.first().x, points.first().y)

    // Use quadratic Bézier curves to smooth the path
    for (i in 1 until points.size - 1 step 2) {
        val p1 = points[i]
        val p2 = points[i + 1]
        this.quadraticBezierTo(
            p1.x, p1.y, // Control point
            p2.x, p2.y  // End point
        )
    }
}