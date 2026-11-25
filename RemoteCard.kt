package com.henpocket.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.theme.HenColors

@Composable
fun RemoteCard(
    busy: Boolean,
    message: String?,
    onRefresh: () -> Unit,
    onReboot: () -> Unit,
    onShutdown: () -> Unit
) {
    HenCard {
        Text(
            text = "webMAN remote",
            color = HenColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        HenButton(
            text = "Refresh XMB",
            onClick = onRefresh,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HenButton(
                text = "Reboot",
                onClick = onReboot,
                enabled = !busy,
                danger = true,
                filled = false,
                modifier = Modifier.weight(1f)
            )
            HenButton(
                text = "Shutdown",
                onClick = onShutdown,
                enabled = !busy,
                danger = true,
                modifier = Modifier.weight(1f)
            )
        }
        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = HenColors.TextSecondary, fontSize = 13.sp)
        }
    }
}
