package com.henpocket.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.henpocket.app.model.ConnectionStatus
import com.henpocket.app.ui.dashboard.ConnectionViewModel
import com.henpocket.app.ui.dashboard.DashboardScreen
import com.henpocket.app.ui.splitter.SplitterScreen
import com.henpocket.app.ui.theme.HenColors
import com.henpocket.app.ui.tools.ToolsScreen
import com.henpocket.app.ui.transfer.TransferScreen

private enum class Tab(val label: String, val glyph: String) {
    HOME("Home", "⌂"),
    SEND("Send", "⇪"),
    SPLIT("Split", "✂"),
    TOOLS("Tools", "⚙")
}

@Composable
fun AppRoot() {
    val conn: ConnectionViewModel = viewModel()
    val state by conn.state.collectAsState()
    var tab by remember { mutableStateOf(Tab.HOME) }

    val host: String? = if (state.status == ConnectionStatus.CONNECTED) state.ip else null

    Scaffold(
        containerColor = HenColors.Obsidian,
        bottomBar = {
            NavigationBar(containerColor = HenColors.Surface) {
                for (t in Tab.values()) {
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Text(text = t.glyph, fontSize = 20.sp) },
                        label = { Text(text = t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HenColors.PsBlueLight,
                            selectedTextColor = HenColors.PsBlueLight,
                            indicatorColor = HenColors.SurfaceHigh,
                            unselectedIconColor = HenColors.TextSecondary,
                            unselectedTextColor = HenColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                Tab.HOME -> DashboardScreen(vm = conn)
                Tab.SEND -> TransferScreen(host = host)
                Tab.SPLIT -> SplitterScreen()
                Tab.TOOLS -> ToolsScreen(host = host)
            }
        }
    }
}
