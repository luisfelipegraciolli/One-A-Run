package com.shiwa.onearun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shiwa.onearun.navigation.OneARunNavGraph
import com.shiwa.onearun.ui.theme.OneARunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            OneARunTheme {
                OneARunNavGraph()
            }
        }
    }
}
