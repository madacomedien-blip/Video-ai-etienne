package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimationStyle
import com.example.data.model.GenerationConfig
import com.example.data.model.MusicTrack
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoMode
import com.example.data.model.VisualFilter
import com.example.engine.ai.ScriptAiEngine
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
fun ScriptModeScreen(
    initialScript: String? = null,
    initialTitle: String? = null,
    onBack: () -> Unit,
    onGenerate: (GenerationConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle ?: "") }
    var scriptText by remember { mutableStateOf(initialScript ?: "") }
    var selectedAspectRatio by remember { mutableStateOf(VideoAspectRatio.VERTICAL_9_16) }
    var selectedDuration by remember { mutableIntStateOf(10) }
    var selectedStyle by remember { mutableStateOf(AnimationStyle.KEN_BURNS_ZOOM) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mode Script (Histoire)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("script_back_button")) {
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
            // Inspiration Chips
            item {
                Text(
                    text = "Inspirations d'Histoires IA",
                    fontSize = 13.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ScriptAiEngine.STORY_TEMPLATES) { template ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    title = template.title
                                    scriptText = template.prompt
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "✨ ${template.title}",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Title input
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la vidéo") },
                    placeholder = { Text("Ex: L'Épopée du Guerrier Céleste") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("script_title_input")
                )
            }

            // Script text input
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Histoire ou Script de la vidéo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${scriptText.length} caractères",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scriptText,
                        onValueChange = { scriptText = it },
                        placeholder = {
                            Text(
                                "Écrivez ici votre récit en français. L'IA découpera votre histoire en scènes cinématiques, animera chaque passage et générera la voix française synchronisée..."
                            )
                        },
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("script_text_input")
                    )
                }
            }

            // Aspect Ratio Selector
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

            // Style & Universe
            item {
                Text(
                    text = "Univers Visuel & Ambiance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VisualFilter.values()) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ElectricPurple else DarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricPurple else DarkSurfaceBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = filter.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                                Text(
                                    text = filter.description,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Animation Motion
            item {
                Text(
                    text = "Mouvement de Caméra & Effets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AnimationStyle.values()) { anim ->
                        val isSelected = selectedStyle == anim
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ElectricCyan else DarkSurface)
                                .clickable { selectedStyle = anim }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = anim.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
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
                                    val sample = if (scriptText.isNotBlank()) scriptText.take(60) else "Bonjour, voici votre vidéo animée par l'intelligence artificielle."
                                    audioSynth.speakPreview(sample, voicePitch, voiceSpeed)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("preview_voice_button")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Écouter", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pitch Slider
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

                        // Speed Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vitesse", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = String.format("%.1fx", voiceSpeed),
                                fontSize = 12.sp,
                                color = ElectricPurple
                            )
                        }
                        Slider(
                            value = voiceSpeed,
                            onValueChange = { voiceSpeed = it },
                            valueRange = 0.8f..1.3f,
                            colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
                        )
                    }
                }
            }

            // Royalty-Free Music Selection
            item {
                Text(
                    text = "Musique Libre de Droits",
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

            // Subtitle toggle
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
                            Text("Sous-titres automatiques", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text("Incrustés directement dans la vidéo", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Switch(
                        checked = burnSubtitles,
                        onCheckedChange = { burnSubtitles = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricPurple
                        ),
                        modifier = Modifier.testTag("subtitles_switch")
                    )
                }
            }

            // Generate CTA Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val finalTitle = title.ifBlank { "Histoire IA" }
                        val finalScript = scriptText.ifBlank {
                            "Dans une contrée lointaine, l'intelligence artificielle donne vie aux récits les plus extraordinaires."
                        }
                        onGenerate(
                            GenerationConfig(
                                mode = VideoMode.SCRIPT,
                                title = finalTitle,
                                scriptText = finalScript,
                                aspectRatio = selectedAspectRatio,
                                durationSeconds = selectedDuration,
                                animationStyle = selectedStyle,
                                visualFilter = selectedFilter,
                                musicTrack = selectedMusic,
                                voicePitch = voicePitch,
                                voiceSpeed = voiceSpeed,
                                burnSubtitles = burnSubtitles
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_script_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Créer la vidéo (MP4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
