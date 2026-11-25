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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.data.Prefs
import com.henpocket.app.model.DnsEntry
import com.henpocket.app.network.IpValidator
import com.henpocket.app.ui.components.BackRow
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.HenTextField
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors

private val STEPS = listOf(
    "On the PS3 open Settings > Network Settings > Internet Connection Settings.",
    "Choose Custom, pick Wired or Wireless, and keep your usual settings until you reach DNS Setting.",
    "Set DNS Setting to Manual and type the Primary and Secondary addresses.",
    "Finish the wizard, then run Test Connection.",
    "Restore Automatic DNS the same way if something stops working."
)

@Composable
fun DnsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val entries = remember { mutableStateListOf<DnsEntry>().apply { addAll(prefs.dnsEntries) } }
    val clipboard = LocalClipboardManager.current

    var name by remember { mutableStateOf("") }
    var primary by remember { mutableStateOf("") }
    var secondary by remember { mutableStateOf("") }
    var formMessage by remember { mutableStateOf<String?>(null) }
    var copied by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackRow(onBack)
        ScreenHeader(title = "DNS cheat sheet", subtitle = "Custom DNS for your PS3")

        HenCard {
            Text(
                text = "How to set DNS on the PS3",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            STEPS.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    color = HenColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        HenCard {
            Text(
                text = "Revival servers",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "DNS addresses for community revival services change over time, so HENPocket does not " +
                    "hard-code any. Copy the current addresses from the revival project's own website and save " +
                    "them below for one-tap copying.",
                color = HenColors.TextSecondary,
                fontSize = 13.sp
            )
        }

        for (entry in entries.toList()) {
            HenCard {
                Text(
                    text = entry.name,
                    color = HenColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Primary: ${entry.primary}", color = HenColors.PsBlueLight, fontSize = 14.sp)
                Text(text = "Secondary: ${entry.secondary}", color = HenColors.PsBlueLight, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HenButton(
                        text = "Copy 1st",
                        onClick = {
                            clipboard.setText(AnnotatedString(entry.primary))
                            copied = "Copied ${entry.primary}"
                        },
                        filled = false,
                        modifier = Modifier.weight(1f)
                    )
                    HenButton(
                        text = "Copy 2nd",
                        onClick = {
                            clipboard.setText(AnnotatedString(entry.secondary))
                            copied = "Copied ${entry.secondary}"
                        },
                        filled = false,
                        modifier = Modifier.weight(1f)
                    )
                    HenButton(
                        text = "Remove",
                        onClick = {
                            entries.remove(entry)
                            prefs.dnsEntries = entries.toList()
                        },
                        danger = true,
                        filled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (copied != null) {
            Text(text = copied ?: "", color = HenColors.Success, fontSize = 13.sp)
        }

        HenCard {
            Text(
                text = "Add a DNS entry",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            HenTextField(value = name, onValueChange = { name = it }, label = "Name")
            Spacer(modifier = Modifier.height(8.dp))
            HenTextField(
                value = primary,
                onValueChange = { primary = it },
                label = "Primary DNS",
                keyboardType = KeyboardType.Uri
            )
            Spacer(modifier = Modifier.height(8.dp))
            HenTextField(
                value = secondary,
                onValueChange = { secondary = it },
                label = "Secondary DNS",
                keyboardType = KeyboardType.Uri
            )
            if (formMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formMessage ?: "", color = HenColors.Warning, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HenButton(
                text = "Save entry",
                onClick = {
                    when {
                        name.isBlank() -> formMessage = "Give the entry a name."
                        !IpValidator.isIpv4(primary) -> formMessage = "Primary DNS must be an IPv4 address."
                        !IpValidator.isIpv4(secondary) -> formMessage = "Secondary DNS must be an IPv4 address."
                        else -> {
                            entries.add(DnsEntry(name.trim(), primary.trim(), secondary.trim()))
                            prefs.dnsEntries = entries.toList()
                            name = ""
                            primary = ""
                            secondary = ""
                            formMessage = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
