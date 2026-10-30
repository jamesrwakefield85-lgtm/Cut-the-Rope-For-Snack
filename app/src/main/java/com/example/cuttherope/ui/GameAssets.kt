package com.example.cuttherope.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.R

class GameAssets(
    val omnomIdle: ImageBitmap,
    val omnomSad1: ImageBitmap,
    val omnomSad2: ImageBitmap,
    val omnomEat1: ImageBitmap,
    val omnomEat2: ImageBitmap,
    val candyClassic: ImageBitmap
)

@Composable
fun rememberGameAssets(): GameAssets {
    val context = LocalContext.current
    return remember {
        val res = context.resources
        val opts = BitmapFactory.Options().apply {
            inScaled = false
        }
        GameAssets(
            omnomIdle = BitmapFactory.decodeResource(res, R.drawable.omnom_idle, opts).asImageBitmap(),
            omnomSad1 = BitmapFactory.decodeResource(res, R.drawable.omnom_sad_1, opts).asImageBitmap(),
            omnomSad2 = BitmapFactory.decodeResource(res, R.drawable.omnom_sad_2, opts).asImageBitmap(),
            omnomEat1 = BitmapFactory.decodeResource(res, R.drawable.omnom_eat_1, opts).asImageBitmap(),
            omnomEat2 = BitmapFactory.decodeResource(res, R.drawable.omnom_eat_2, opts).asImageBitmap(),
            candyClassic = BitmapFactory.decodeResource(res, R.drawable.candy_classic, opts).asImageBitmap()
        )
    }
}
