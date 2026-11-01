package com.example.data.model

enum class VideoMode {
    SCRIPT,
    PHOTO
}

enum class VideoAspectRatio(
    val label: String,
    val sublabel: String,
    val width: Int,
    val height: Int,
    val ratioFloat: Float
) {
    VERTICAL_9_16("9:16", "TikTok / Reels / Shorts", 540, 960, 9f / 16f),
    HORIZONTAL_16_9("16:9", "YouTube / TV", 960, 540, 16f / 9f),
    SQUARE_1_1("1:1", "Instagram / Feed", 720, 720, 1f)
}

enum class AnimationStyle(
    val title: String,
    val description: String
) {
    KEN_BURNS_ZOOM("Zoom Cinématique", "Zoom avant fluide et panoramique naturel"),
    PARALLAX_BREATHING("Respiration & Vie", "Pulsation 2.5D organique et vivante"),
    ANIME_SPEED_LINES("Lignes Vitesse Manga", "Effet d'action anime avec rayonnements"),
    CINEMATIC_GLOW("Lueurs Magiques", "Particules flottantes et halos lumineux"),
    CYBERPUNK_PULSE("Néon Cyberpunk", "Ondes d'énergie et reflets néons")
}

enum class VisualFilter(
    val title: String,
    val description: String
) {
    ANIME_VIBRANT("Anime Shonen", "Couleurs vives, contraste élevé et netteté manga"),
    WARM_CINEMA("Cinéma Réaliste", "Tonalité chaude, éclairage doré et profondeur"),
    CYBERPUNK("Cyberpunk 2099", "Teintes violettes, cyans et reflets futuristes"),
    DREAMY_FANTASY("Conte Magique", "Ambiance féérique, brume douce et scintillements"),
    VINTAGE_CARTOON("Dessin Vintage", "Texture rétro et couleurs pastel sépia")
}

enum class MusicTrack(
    val title: String,
    val genre: String
) {
    ANIME_SHONEN("Héros Shonen", "Rythme énergique et cuivres inspirants"),
    CINEMATIC_DREAM("Rêverie Cinéma", "Nappe ambiante douce et piano élégant"),
    CYBERPUNK_BEAT("Néon Synthwave", "Basse électronique rétro et synthétiseur"),
    ACOUSTIC_CHILL("Douceur Acoustique", "Mélodie calme, guitare et sérénité"),
    EPIC_ADVENTURE("Épopée Héroïque", "Orchestration épique et tambours"),
    NONE("Sans musique", "Voix seule et sous-titres")
}

data class ScriptScene(
    val order: Int,
    val sceneTitle: String,
    val narrationText: String,
    val subtitleText: String,
    val visualPrompt: String,
    val primaryColorHex: Long = 0xFF8E5AFF
)

data class GenerationConfig(
    val mode: VideoMode,
    val title: String,
    val scriptText: String = "",
    val photoUri: String? = null,
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.VERTICAL_9_16,
    val durationSeconds: Int = 10,
    val animationStyle: AnimationStyle = AnimationStyle.KEN_BURNS_ZOOM,
    val visualFilter: VisualFilter = VisualFilter.ANIME_VIBRANT,
    val musicTrack: MusicTrack = MusicTrack.ANIME_SHONEN,
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f,
    val burnSubtitles: Boolean = true
)
