package com.henpocket.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.henpocket.app.ui.theme.HenColors

@Composable
fun BackRow(onBack: () -> Unit) {
    TextButton(onClick = onBack) {
        Text(
            text = "‹ Back to tools",
            color = HenColors.PsBlueLight,
            fontWeight = FontWeight.SemiBold
        )
    }
}
