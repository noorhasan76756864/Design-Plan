package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.components.LocalDensityProvider
import com.example.ui.navigation.BuildSpaceMainApp
import com.example.ui.theme.BuildSpaceTheme
import com.example.ui.theme.DarkSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalDensityProvider.density = LocalDensity.current.density
            val repository = remember { BuildSpaceRepository() }

            BuildSpaceTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkSurface
                ) {
                    BuildSpaceMainApp(repository = repository)
                }
            }
        }
    }
}
