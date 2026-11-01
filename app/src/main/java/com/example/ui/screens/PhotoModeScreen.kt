package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AnimationStyle
import com.example.data.model.GenerationConfig
import com.example.data.model.MusicTrack
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoMode
import com.example.data.model.VisualFilter
import com.example.engine.audio.AudioSynthesizer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoModeScreen(
    onBack: () -> Unit,
    onGenerate: (GenerationConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Sample default photo: Anime Hero
    val sampleAnimeUri = "android.resource://${context.packageName}/${R.drawable.sample_anime_hero_1793474790763}"
    val sampleCinemaUri = "android.resource://${context.packageName}/${R.drawable.sample_cinema_scene_1793474803852}"

    var selectedPhotoUri by remember { mutableStateOf<String?>(sampleAnimeUri) }
    var captionText by remember {
        mutableStateOf("Le chevalier des étoiles réveille son pouvoir céleste pour protéger la galaxie.")
    }
    var title by remember { mutableStateOf("L'Éveil du Héros") }
    var selectedAspectRatio by remember { mutableStateOf(VideoAspectRatio.VERTICAL_9_16) }
    var selectedDuration by remember { mutableIntStateOf(10) }
    var selectedAnimation by remember { mutableStateOf(AnimationStyle.PARALLAX_BREATHING) }
    var selectedFilter by remember { mutableStateOf(VisualFilter.ANIME_VIBRANT) }
    var selectedMusic by remember { mutableStateOf(MusicTrack.ANIME_SHONEN) }
    var voicePitch by remember { mutableFloatStateOf(1.0f) }
    var voiceSpeed by remember { mutableFloatStateOf(1.0f) }
    var burnSubtitles by remember { mutableStateOf(true) }

    val audioSynth = remember { AudioSynthesizer(context) }

    LaunchedEffect(Unit) {
        audioSynth.initTts()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioSynth.release()
        }
    }

    // Modern Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mode Photo (Animation)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("photo_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // Photo Preview & Picker Card
            item {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurface)
                            .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedPhotoUri != null) {
                            AsyncImage(
                                model = Uri.parse(selectedPhotoUri),
                                contentDescription = "Photo sélectionnée",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay change button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Changer la photo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Importer une photo de la galerie", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text("Visage, paysage, dessin ou personnage", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ready-to-use Sample Photos row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ou choisissez un exemple prêt :", fontSize = 12.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .clickable {
                                        selectedPhotoUri = sampleAnimeUri
                                        title = "L'Éveil du Héros"
                                        captionText = "Le chevalier des étoiles réveille son pouvoir pour sauver son monde."
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("🗡️ Héros Manga", fontSize = 11.sp, color = ElectricCyan)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .clickable {
                                        selectedPhotoUri = sampleCinemaUri
                                        title = "Cité Céleste"
                                        captionText = "La cité flottante resplendit au coucher du soleil entre ciel et terre."
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("🌆 Cité Céleste", fontSize = 11.sp, color = AmberGold)
                            }
                        }
                    }
                }
            }

            // Title input
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'animation") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Caption / Narration
            item {
                Column {
                    Text(
                        text = "Histoire & Narration de la photo (Voix off)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        placeholder = { Text("Racontez ce qui se passe dans cette image. L'IA l'animera et la lira en voix française...") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_caption_input")
                    )
                }
            }

            // Animation Motion Selector
            item {
                Text(
                    text = "Mouvement d'Animation de la Photo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AnimationStyle.values()) { anim ->
                        val isSelected = selectedAnimation == anim
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ElectricCyan else DarkSurface)
                                .clickable { selectedAnimation = anim }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = anim.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                                Text(
                                    text = anim.description,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Format Selector
            item {
                Text(
                    text = "Format d'affichage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoAspectRatio.values().forEach { ratio ->
                        val isSelected = selectedAspectRatio == ratio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ElectricPurple else DarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricPurple else DarkSurfaceBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedAspectRatio = ratio }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = ratio.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                                Text(
                                    text = ratio.sublabel.split("/").first().trim(),
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Duration Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Durée : $selectedDuration secondes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(5, 10, 15).forEach { dur ->
                        val isSelected = selectedDuration == dur
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AmberGold else DarkSurface)
                                .clickable { selectedDuration = dur }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${dur}s",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.Black else TextPrimary
                            )
                        }
                    }
                }
            }

            // Voice synthesis section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp)),
                    color = DarkSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ElectricCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Voix Française IA (TTS)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val sample = captionText.ifBlank { "Un souvenir magnifique qui prend vie." }
                                    audioSynth.speakPreview(sample, voicePitch, voiceSpeed)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tester", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tonalité (Pitch)", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = if (voicePitch < 0.95f) "Grave" else if (voicePitch > 1.1f) "Aigu / Anime" else "Naturel",
                                fontSize = 12.sp,
                                color = ElectricCyan
                            )
                        }
                        Slider(
                            value = voicePitch,
                            onValueChange = { voicePitch = it },
                            valueRange = 0.7f..1.4f,
                            colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                        )
                    }
                }
            }

            // Royalty-Free Music Selection
            item {
                Text(
                    text = "Musique d'ambiance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MusicTrack.values()) { track ->
                        val isSelected = selectedMusic == track
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AmberGold else DarkSurface)
                                .clickable { selectedMusic = track }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else AmberGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = track.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Subtitle Switch
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Subtitles, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Sous-titres vidéo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text("Incrustation dynamique", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Switch(
                        checked = burnSubtitles,
                        onCheckedChange = { burnSubtitles = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricCyan
                        )
                    )
                }
            }

            // CTA Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val finalTitle = title.ifBlank { "Animation Photo" }
                        val finalCaption = captionText.ifBlank { "Une photo qui prend vie avec Video AI Étienne." }
                        onGenerate(
                            GenerationConfig(
                                mode = VideoMode.PHOTO,
                                title = finalTitle,
                                scriptText = finalCaption,
                                photoUri = selectedPhotoUri,
                                aspectRatio = selectedAspectRatio,
                                durationSeconds = selectedDuration,
                                animationStyle = selectedAnimation,
                                visualFilter = selectedFilter,
                                musicTrack = selectedMusic,
                                voicePitch = voicePitch,
                                voiceSpeed = voiceSpeed,
                                burnSubtitles = burnSubtitles
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_photo_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Animer et Créer la vidéo (MP4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
