package com.henpocket.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.henpocket.app.ui.theme.HenColors

@Composable
fun HenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HenColors.TextPrimary,
            unfocusedTextColor = HenColors.TextPrimary,
            focusedBorderColor = HenColors.PsBlue,
            unfocusedBorderColor = HenColors.SurfaceHigh,
            cursorColor = HenColors.PsBlueLight,
            focusedLabelColor = HenColors.PsBlueLight,
            unfocusedLabelColor = HenColors.TextSecondary
        )
    )
}
