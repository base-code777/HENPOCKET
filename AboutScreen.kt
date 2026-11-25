package com.henpocket.app.ui.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henpocket.app.data.AppLinks
import com.henpocket.app.ui.components.BackRow
import com.henpocket.app.ui.components.HenButton
import com.henpocket.app.ui.components.HenCard
import com.henpocket.app.ui.components.ScreenHeader
import com.henpocket.app.ui.theme.HenColors

private fun openLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No app can open this link.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackRow(onBack)
        ScreenHeader(title = "About HENPocket", subtitle = "Version ${AppLinks.VERSION}")

        HenCard {
            Text(
                text = "A free, open-source companion for PS3 HEN and webMAN MOD. It finds your console on " +
                    "Wi-Fi, shows live temperatures, sends files to the right folder over FTP, and splits " +
                    "large files for FAT32 USB drives.",
                color = HenColors.TextSecondary,
                fontSize = 14.sp
            )
        }

        HenCard {
            Text(
                text = "Support the project",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            HenButton(
                text = "GitHub",
                onClick = { openLink(context, AppLinks.GITHUB_URL) },
                filled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            HenButton(
                text = "Ko-fi",
                onClick = { openLink(context, AppLinks.KOFI_URL) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            HenButton(
                text = "PayPal",
                onClick = { openLink(context, AppLinks.PAYPAL_URL) },
                filled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        HenCard {
            Text(
                text = "Legal",
                color = HenColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "HENPocket does not include, download or distribute any Sony firmware, keys or game files. " +
                    "It only moves files that you supply to a console you own. PlayStation is a trademark of " +
                    "Sony Interactive Entertainment; this app is not affiliated with or endorsed by Sony.",
                color = HenColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
