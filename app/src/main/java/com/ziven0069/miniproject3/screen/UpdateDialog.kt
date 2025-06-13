package com.ziven0069.miniproject3.screen

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ziven0069.miniproject3.ui.theme.MiniProject3Theme
import com.ziven0069.miniproject3.R

@Composable
fun UpdateDialog(
    bitmap: Bitmap?,
    currentNama: String,
    currentNamaLatin: String,
    onDismissRequest: () -> Unit,
    onConfirmation: (newNama: String, newNamaLatin: String, newBitmap: Bitmap) -> Unit,
    onImageEditClick: () -> Unit // now required
) {
    var nama by remember { mutableStateOf(currentNama) }
    var namaLatin by remember { mutableStateOf(currentNamaLatin) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize()
                        )
                        IconButton(
                            onClick = onImageEditClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.try_again),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text(stringResource(id = R.string.nama)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                )
                OutlinedTextField(
                    value = namaLatin,
                    onValueChange = { namaLatin = it },
                    label = { Text(stringResource(id = R.string.nama_latin)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.batal))
                    }

                    OutlinedButton(
                        onClick = {
                            if (bitmap != null) {
                                onConfirmation(nama, namaLatin, bitmap)
                            }
                        },
                        enabled = nama.isNotBlank() && namaLatin.isNotBlank() && bitmap != null,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.simpan))
                    }
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun UpdateDialogPreview() {
    MiniProject3Theme {
        UpdateDialog(
            bitmap = null,
            currentNama = "Nama",
            currentNamaLatin = "Nama Latin",
            onDismissRequest = {},
            onConfirmation = { _, _, _ -> },
            onImageEditClick = TODO()
        )
    }
}
