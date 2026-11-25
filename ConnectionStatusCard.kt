package com.henpocket.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.model.ConnectionStatus
import com.henpocket.app.ui.theme.HenColors

@Composable
fun ConnectionStatusCard(
    status: ConnectionStatus,
    ip: String?,
    message: String,
    scanProgress: Int,
    onFind: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillColor = when (status) {
        ConnectionStatus.CONNECTED -> HenColors.Success
        ConnectionStatus.SCANNING -> HenColors.Warning
        ConnectionStatus.DISCONNECTED -> HenColors.Error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(HenColors.SurfaceHigh, HenColors.Surface)))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "PS3 Connection", color = HenColors.TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ip ?: "No console linked",
                    color = HenColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusPill(text = status.label, color = pillColor)
        }

        if (status == ConnectionStatus.SCANNING) {
            Spacer(modifier = Modifier.height(14.dp))
            HenProgressBar(fraction = scanProgress / 100f)
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = HenColors.TextSecondary, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (status) {
            ConnectionStatus.CONNECTED -> HenButton(
                text = "Disconnect",
                onClick = onDisconnect,
                modifier = Modifier.fillMaxWidth(),
                filled = false
            )
            ConnectionStatus.SCANNING -> HenButton(
                text = "Working…",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            ConnectionStatus.DISCONNECTED -> HenButton(
                text = "Find My PS3",
                onClick = onFind,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
