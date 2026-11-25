package com.henpocket.app.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.henpocket.app.files.formatBytes
import com.henpocket.app.ui.components.BackRow
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors

@Composable
fun CleanupScreen(host: String?, onBack: () -> Unit, vm: CleanupViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    var confirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackRow(onBack)
        ScreenHeader(title = "Package cleanup", subtitle = "Free space in /dev_hdd0/packages/")

        HenCard {
            Text(text = s.message, color = HenColors.TextSecondary, fontSize = 13.sp)
            if (host == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connect to your PS3 on the Home tab first.",
                    color = HenColors.Warning,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HenButton(
                    text = "Scan",
                    onClick = { if (host != null) vm.scan(host) },
                    enabled = host != null && !s.loading,
                    modifier = Modifier.weight(1f)
                )
                HenButton(
                    text = if (s.selected.size == s.files.size && s.files.isNotEmpty()) "Select none" else "Select all",
                    onClick = { vm.selectAll() },
                    enabled = s.files.isNotEmpty() && !s.loading,
                    filled = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (s.files.isNotEmpty()) {
            HenCard {
                for (file in s.files) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = file.name in s.selected,
                            onCheckedChange = { vm.toggle(file.name) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = HenColors.PsBlue,
                                uncheckedColor = HenColors.TextSecondary,
                                checkmarkColor = Color.White
                            )
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.name, color = HenColors.TextPrimary, fontSize = 14.sp)
                            Text(
                                text = formatBytes(file.size),
                                color = HenColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            HenButton(
                text = "Delete selected (${s.selected.size}, ${formatBytes(s.selectedBytes)})",
                onClick = { confirm = true },
                enabled = s.selected.isNotEmpty() && !s.loading && host != null,
                danger = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = HenColors.Surface,
            titleContentColor = HenColors.TextPrimary,
            textContentColor = HenColors.TextSecondary,
            title = { Text("Delete ${s.selected.size} file(s)?") },
            text = { Text("They will be removed from your PS3 for good. Installed games are not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    if (host != null) vm.deleteSelected(host)
                }) {
                    Text("Delete", color = HenColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) {
                    Text("Cancel", color = HenColors.PsBlueLight)
                }
            }
        )
    }
}
