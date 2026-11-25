package com.henpocket.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.model.ConnectionStatus
import com.henpocket.app.model.PowerAction
import com.henpocket.app.ui.components.ConnectionStatusCard
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.HenTextField
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(vm: ConnectionViewModel) {
    val s by vm.state.collectAsState()
    var manualIp by remember { mutableStateOf(vm.savedIp ?: "") }
    var pending by remember { mutableStateOf<PowerAction?>(null) }

    // Poll /cpursx.ps3 every 3 seconds while connected and this tab is visible.
    LaunchedEffect(s.status) {
        if (s.status == ConnectionStatus.CONNECTED) {
            while (true) {
                vm.pollTelemetry()
                delay(3000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "HENPocket", subtitle = "Your PS3 control center")

        ConnectionStatusCard(
            status = s.status,
            ip = s.ip,
            message = s.message,
            scanProgress = s.scanProgress,
            onFind = { vm.findPs3() },
            onDisconnect = { vm.disconnect() }
        )

        if (s.status == ConnectionStatus.CONNECTED) {
            TelemetryCard(telemetry = s.telemetry, note = s.telemetryNote)
            RemoteCard(
                busy = s.remoteBusy,
                message = s.remoteMessage,
                onRefresh = { vm.refreshXmb() },
                onReboot = { pending = PowerAction.REBOOT },
                onShutdown = { pending = PowerAction.SHUTDOWN }
            )
        } else {
            HenCard {
                Text(
                    text = "Enter the IP manually",
                    color = HenColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Backup option if auto-discovery cannot find your console.",
                    color = HenColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HenTextField(
                    value = manualIp,
                    onValueChange = { manualIp = it },
                    label = "PS3 IP address (for example 192.168.1.42)",
                    keyboardType = KeyboardType.Uri
                )
                Spacer(modifier = Modifier.height(12.dp))
                HenButton(
                    text = "Connect",
                    onClick = { vm.connectManual(manualIp) },
                    enabled = s.status != ConnectionStatus.SCANNING,
                    filled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    val action = pending
    if (action != null) {
        AlertDialog(
            onDismissRequest = { pending = null },
            containerColor = HenColors.Surface,
            titleContentColor = HenColors.TextPrimary,
            textContentColor = HenColors.TextSecondary,
            title = { Text(action.dialogTitle) },
            text = { Text(action.dialogBody) },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    if (action == PowerAction.REBOOT) vm.reboot() else vm.shutdown()
                }) {
                    Text(action.confirmLabel, color = HenColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) {
                    Text("Cancel", color = HenColors.PsBlueLight)
                }
            }
        )
    }
}
