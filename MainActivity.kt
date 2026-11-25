package com.henpocket.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.henpocket.app.ui.AppRoot
import com.henpocket.app.ui.theme.HenPocketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HenPocketTheme {
                AppRoot()
            }
        }
    }
}
