package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuttherope.ui.AppScreen
import com.example.cuttherope.ui.CandyShopScreen
import com.example.cuttherope.ui.GameCanvasScreen
import com.example.cuttherope.ui.GameViewModel
import com.example.cuttherope.ui.LevelEditorScreen
import com.example.cuttherope.ui.LevelSelectScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF261812)),
                    color = Color(0xFF261812)
                ) {
                    CutTheRopeApp()
                }
            }
        }
    }
}

@Composable
fun CutTheRopeApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button for sub-screens
    BackHandler(enabled = uiState.currentScreen != AppScreen.LEVEL_SELECT) {
        viewModel.navigateTo(AppScreen.LEVEL_SELECT)
    }

    when (uiState.currentScreen) {
        AppScreen.LEVEL_SELECT -> {
            LevelSelectScreen(
                viewModel = viewModel,
                onOpenEditor = { viewModel.navigateTo(AppScreen.LEVEL_EDITOR) },
                onOpenShop = { viewModel.navigateTo(AppScreen.CANDY_SHOP) }
            )
        }
        AppScreen.GAMEPLAY -> {
            GameCanvasScreen(
                viewModel = viewModel
            )
        }
        AppScreen.LEVEL_EDITOR -> {
            LevelEditorScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppScreen.LEVEL_SELECT) }
            )
        }
        AppScreen.CANDY_SHOP -> {
            CandyShopScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppScreen.LEVEL_SELECT) }
            )
        }
    }
}
