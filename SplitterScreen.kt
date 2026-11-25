package com.henpocket.app.ui.splitter

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
import com.henpocket.app.files.FileSplitter
import com.henpocket.app.files.formatBytes
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.HenProgressBar
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors

@Composable
fun SplitterScreen(vm: SplitterViewModel = viewModel()) {
    val s by vm.state.collectAsState()

    val sourcePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        vm.onSourcePicked(uri)
    }
    val destPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        vm.onDestinationPicked(uri)
    }

    val ready = s.sourceName != null &&
        s.destLabel != null &&
        s.sourceSize > FileSplitter.FAT32_MAX &&
        !s.running

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "USB Splitter", subtitle = "Split big files into FAT32-safe .66600 parts")

        HenCard {
            Text(text = "1. Source file", color = HenColors.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = s.sourceName ?: "No file selected",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (s.sourceName != null) {
                Text(
                    text = formatBytes(s.sourceSize),
                    color = HenColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HenButton(
                text = "Choose ISO / PKG",
                onClick = { sourcePicker.launch(arrayOf("*/*")) },
                enabled = !s.running,
                filled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "2. Destination folder", color = HenColors.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = s.destLabel ?: "No folder selected",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            HenButton(
                text = "Choose USB folder",
                onClick = { destPicker.launch(null) },
                enabled = !s.running,
                filled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        HenCard {
            HenProgressBar(fraction = s.percent / 100f)
            Spacer(modifier = Modifier.height(8.dp))
            val partText = if (s.totalParts > 0 && s.currentPart > 0) {
                "  •  part ${s.currentPart} of ${s.totalParts}"
            } else {
                ""
            }
            Text(
                text = "${s.percent}%  •  ${"%.1f".format(s.speedMbPerSec)} MB/s$partText",
                color = HenColors.TextPrimary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatBytes(s.bytesDone)} / ${formatBytes(s.sourceSize)}",
                color = HenColors.TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = s.message, color = HenColors.TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (s.running) {
                    HenButton(
                        text = "Cancel",
                        onClick = { vm.cancel() },
                        danger = true,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    HenButton(
                        text = "Start split",
                        onClick = { vm.start() },
                        enabled = ready,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HenCard {
            Text(
                text = "Good to know",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Parts are named game.iso.66600, game.iso.66601 and so on. Each part is just under 4 GB, " +
                    "which is the FAT32 limit. Delete any older parts with the same name from the drive first, " +
                    "otherwise Android may rename the new ones. Keep the phone awake while a large file is splitting.",
                color = HenColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
