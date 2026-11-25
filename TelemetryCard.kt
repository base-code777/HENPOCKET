package com.henpocket.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.model.Telemetry
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.StatTile
import com.henpocket.app.ui.theme.HenColors

private fun tempColor(celsius: Int?): Color = when {
    celsius == null -> HenColors.TextSecondary
    celsius >= 80 -> HenColors.Error
    celsius >= 70 -> HenColors.Warning
    else -> HenColors.Success
}

@Composable
fun TelemetryCard(telemetry: Telemetry?, note: String?) {
    HenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live telemetry",
                color = HenColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (note != null) {
                Text(text = note, color = HenColors.Warning, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (telemetry == null) {
            Text(text = "Reading webMAN…", color = HenColors.TextSecondary, fontSize = 14.sp)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    label = "CPU",
                    value = telemetry.cpuC?.let { "$it°C" } ?: "--",
                    valueColor = tempColor(telemetry.cpuC),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "RSX",
                    value = telemetry.rsxC?.let { "$it°C" } ?: "--",
                    valueColor = tempColor(telemetry.rsxC),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "FAN",
                    value = telemetry.fanPercent?.let { "$it%" } ?: "--",
                    valueColor = HenColors.PsBlueLight,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Free on /dev_hdd0: ${telemetry.hddFree ?: "unknown"}",
                color = HenColors.TextPrimary,
                fontSize = 14.sp
            )
            if (!telemetry.hasAnyValue) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "webMAN answered, but this version's page format was not recognised.",
                    color = HenColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
