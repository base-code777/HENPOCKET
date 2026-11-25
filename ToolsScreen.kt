package com.henpocket.app.ui.tools

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors

private enum class ToolSection { CLEANUP, DNS, ABOUT }

@Composable
fun ToolsScreen(host: String?) {
    var section by remember { mutableStateOf<ToolSection?>(null) }

    BackHandler(enabled = section != null) { section = null }

    when (section) {
        ToolSection.CLEANUP -> CleanupScreen(host = host, onBack = { section = null })
        ToolSection.DNS -> DnsScreen(onBack = { section = null })
        ToolSection.ABOUT -> AboutScreen(onBack = { section = null })
        null -> Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenHeader(title = "Tools", subtitle = "Extras for your PS3")
            ToolTile(
                title = "Package cleanup",
                subtitle = "List and delete installers in /dev_hdd0/packages/",
                onClick = { section = ToolSection.CLEANUP }
            )
            ToolTile(
                title = "DNS cheat sheet",
                subtitle = "Steps and one-tap copy for custom DNS",
                onClick = { section = ToolSection.DNS }
            )
            ToolTile(
                title = "About & support",
                subtitle = "Version, GitHub, Ko-fi and PayPal",
                onClick = { section = ToolSection.ABOUT }
            )
        }
    }
}

@Composable
private fun ToolTile(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HenColors.Surface)
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Text(
            text = title,
            color = HenColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, color = HenColors.TextSecondary, fontSize = 13.sp)
    }
}
