package com.ziven0069.miniproject3.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.ziven0069.miniproject3.BuildConfig
import com.ziven0069.miniproject3.R
import com.ziven0069.miniproject3.model.Tanaman
import com.ziven0069.miniproject3.model.User
import com.ziven0069.miniproject3.network.ApiStatus
import com.ziven0069.miniproject3.network.TanamanApi
import com.ziven0069.miniproject3.network.UserDataStore
import com.ziven0069.miniproject3.ui.theme.MiniProject3Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User("", "", ""))
    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showTanamanDialog by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showTanamanDialog = true
    }
    val deleteStatus by viewModel.deleteStatus
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTanaman by remember { mutableStateOf<Tanaman?>(null) }
    LaunchedEffect(deleteStatus) {
        if (deleteStatus != null) {
            Toast.makeText(context, deleteStatus, Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteStatus()
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                signIn(context, dataStore)
                            }
                        } else {
//                            Log.d("SIGN-IN", "User: $user")
                            showDialog = true
                        }
                    })
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle_24),
                            contentDescription = stringResource(id = R.string.profile),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (user.email.isNotEmpty()){
                FloatingActionButton(onClick = {
                    val options = CropImageContractOptions(
                        null,
                        CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = true,
                            fixAspectRatio = true
                        )
                    )

                    launcher.launch(options)
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.tambah_tanaman)
                    )
                }
            }

        }
    ){
            innerPadding ->
        ScreenContent(
            viewModel,
            userId = user.email,
            modifier = Modifier.padding(innerPadding),
            onDeleteClick = { tanaman ->
                selectedTanaman = tanaman
                showDeleteDialog = true
            }
        )
        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    signOut(context, dataStore)
                }
                showDialog = false
            }
        }
        if (showTanamanDialog) {
            TanamanDialog(
                bitmap = bitmap,
                onDismissRequest = { showTanamanDialog = false }) { nama, namaLatin ->
                viewModel.saveData(user.email, nama, namaLatin, bitmap!!)
                showTanamanDialog = false
            }
        }
        if (showDeleteDialog && selectedTanaman != null) {
            DeleteConfirmDialog(
                tanaman = selectedTanaman!!,
                onDismiss = {
                    showDeleteDialog = false
                    selectedTanaman = null
                },
                onConfirm = {
                    viewModel.deleteData(user.email, selectedTanaman!!.id)
                    showDeleteDialog = false
                    selectedTanaman = null
                }
            )
        }
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, userId: String, onDeleteClick: (Tanaman) -> Unit, modifier: Modifier = Modifier) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId) {
        viewModel.retrieveData(userId)
    }

    when(status){
        ApiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize().padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) {
                    ListItem(
                        tanaman = it,
                        onDeleteClick = if (it.mine == 1) {
                            { onDeleteClick(it) }
                        } else null,
                        userId = userId
                    )
                }
            }
        }
        ApiStatus.FAILED ->{
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = {viewModel.retrieveData(userId)},
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }

            }
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(
                User(
                    name = nama,
                    email = email,
                    photoUrl = photoUrl
                )
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User("", "", ""))
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Composable
fun ListItem(
    tanaman: Tanaman,
    userId: String, // Tambahkan userId sebagai parameter
    onDeleteClick: (() -> Unit)? = null
) {
    // State untuk dialog edit, state dialog hapus dihilangkan
    var showEditPlantDialog by remember { mutableStateOf(false) }

    // Dapatkan instance ViewModel
    val viewModel: MainViewModel = viewModel()

    // Ambil bitmap dari URL untuk dialog edit
    val bitmap = rememberBitmapFromUrl(TanamanApi.getTanamanUrl(tanaman.gambar))
    val context = LocalContext.current
    var bitmapa by remember { mutableStateOf<Bitmap?>(null) }

    // Load gambar awal dari URL
    LaunchedEffect(tanaman.gambar) {
        bitmapa = loadBitmapFromUrl(TanamanApi.getTanamanUrl(tanaman.gambar))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            bitmapa = BitmapFactory.decodeStream(stream)
        }
    }
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(TanamanApi.getTanamanUrl(tanaman.gambar))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, tanaman.nama_tanaman),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_img),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = tanaman.nama_tanaman,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Perbaikan: Tampilkan nama latin, bukan nama tanaman lagi
                Text(
                    text = tanaman.nama_latin,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            if (tanaman.mine == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showEditPlantDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Tanaman",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        // Perbaikan: Panggil callback onDeleteClick langsung
                        onClick = { onDeleteClick?.invoke() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Tanaman",
                            tint = Color.White
                        )
                    }
                }
            }


            if (showEditPlantDialog) {
                UpdateDialog(
                    bitmap = bitmapa,
                    currentNama = tanaman.nama_tanaman,
                    currentNamaLatin = tanaman.nama_latin,
                    onDismissRequest = { showEditPlantDialog = false },
                    onConfirmation = { newNama, newNamaLatin, newBitmap ->
                        viewModel.updateData(userId, newNama, newNamaLatin, newBitmap, tanaman.id)
                        showEditPlantDialog = false
                    },
                    onImageEditClick = {
                        launcher.launch("image/*")
                    }
                )
            }
        }
    }
}

suspend fun loadBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val input = URL(url).openStream()
        BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        null
    }
}


@Composable
fun rememberBitmapFromUrl(url: String): Bitmap? {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

     return bitmap
}


@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    MiniProject3Theme {
        MainScreen()
    }
}