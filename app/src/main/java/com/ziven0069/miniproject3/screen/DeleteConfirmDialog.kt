package com.ziven0069.miniproject3.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ziven0069.miniproject3.R
import com.ziven0069.miniproject3.model.Tanaman

@Composable
fun DeleteConfirmDialog(
    tanaman: Tanaman,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.hapus_tanaman_title))
        },
        text = {
            Text(text = stringResource(R.string.hapus_tanaman_body, tanaman.nama_tanaman))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.hapus))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.batal))
            }
        }
    )
}