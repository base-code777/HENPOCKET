package com.henpocket.app.ui.transfer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.henpocket.app.files.FileTypeRouter
import com.henpocket.app.files.formatBytes
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.HenProgressBar
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors

@Composable
fun TransferScreen(host: String?, vm: TransferViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        vm.onFilePicked(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Send to PS3", subtitle = "Smart FTP dropper")

        HenCard {
            Text(
                text = s.fileName ?: "No file selected",
                color = HenColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            if (s.fileName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatBytes(s.fileSize) + (s.routeLabel?.let { "  •  $it" } ?: ""),
                    color = HenColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
            if (s.destination != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Destination: ${s.destination}",
                    color = HenColors.PsBlueLight,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HenProgressBar(fraction = s.percent / 100f)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${s.percent}%  •  ${"%.1f".format(s.speedMbPerSec)} MB/s  •  " +
                    "${formatBytes(s.bytesDone)} / ${formatBytes(s.fileSize)}",
                color = HenColors.TextPrimary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = s.message, color = HenColors.TextSecondary, fontSize = 13.sp)

            if (host == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connect to your PS3 on the Home tab first.",
                    color = HenColors.Warning,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HenButton(
                    text = "Choose file",
                    onClick = { picker.launch(arrayOf("*/*")) },
                    enabled = !s.running,
                    filled = false,
                    modifier = Modifier.weight(1f)
                )
                if (s.running) {
                    HenButton(
                        text = "Cancel",
                        onClick = { vm.cancel() },
                        danger = true,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    HenButton(
                        text = "Send",
                        onClick = { if (host != null) vm.start(host) },
                        enabled = s.canSend && host != null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (s.mountPath != null && host != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HenButton(
                    text = "Mount on XMB",
                    onClick = { vm.mount(host) },
                    filled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HenCard {
            Text(
                text = "Auto-routing",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            for ((ext, folder) in FileTypeRouter.table) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text(
                        text = ext,
                        color = HenColors.PsBlueLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = folder,
                        color = HenColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    }
}
