package com.musica.paralosoidos.ui.downloader

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun DownloaderSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    isSearching: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isYouTubeUrl = query.contains("youtube.com") || query.contains("youtu.be")

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        placeholder = {
            Text(
                "Buscar cancion o pegar URL de YouTube...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            if (isYouTubeUrl) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "Enlace de YouTube detectado",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 6.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        enabled = enabled
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpiar busqueda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                            if (clipText.isNotBlank()) {
                                onQueryChange(clipText.trim())
                                onSearch(clipText.trim())
                            }
                        },
                        enabled = enabled
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = "Pegar desde portapapeles",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (query.isNotBlank() && enabled) {
                    focusManager.clearFocus()
                    onSearch(query.trim())
                }
            }
        )
    )
}
