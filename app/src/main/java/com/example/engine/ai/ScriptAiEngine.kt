package com.example.engine.ai

import com.example.data.model.ScriptScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ScriptAiEngine {

    data class StoryPromptTemplate(
        val category: String,
        val title: String,
        val prompt: String,
        val suggestedMusic: String
    )

    val STORY_TEMPLATES = listOf(
        StoryPromptTemplate(
            category = "Anime Shonen",
            title = "L'Éveil du Guerrier Céleste",
            prompt = "Dans un village caché au sommet des falaises sacrées, un jeune apprenti découvre une lame gravée de runes azur. Lorsque les ombres envahissent le royaume, son courage réveille une puissance ancestrale qui illumine le ciel nocturne.",
            suggestedMusic = "Héros Shonen"
        ),
        StoryPromptTemplate(
            category = "Conte & Légende",
            title = "Le Secret de l'Île Rouge",
            prompt = "Au cœur de l'océan Indien, sous les baobabs géants de Madagascar, une légende raconte qu'une fleur d'or ne fleurit qu'une fois par siècle. Une jeune aventurière se lance dans la quête pour sauver son village.",
            suggestedMusic = "Rêverie Cinéma"
        ),
        StoryPromptTemplate(
            category = "Cyberpunk 2099",
            title = "Néon & Rébellion",
            prompt = "Sous les gratte-ciels scintillants de Néo-Mada, des vaisseaux volants sillonnent la brume violette. Une hackeuse d'élite découvre un signal crypté promettant la liberté à toute la mégalopole.",
            suggestedMusic = "Néon Synthwave"
        ),
        StoryPromptTemplate(
            category = "Documentaire Nature",
            title = "Les Mystères de la Forêt Enchantée",
            prompt = "Au lever de l'aurore, les brumes matinales dévoilent une faune extraordinaire. Les lémuriens sautent d'arbre en arbre tandis que les cascades cristallines résonnent d'une douce mélodie naturelle.",
            suggestedMusic = "Douceur Acoustique"
        ),
        StoryPromptTemplate(
            category = "Épopée Héroïque",
            title = "La Forteresse des Nuages",
            prompt = "Flottant au-dessus des montagnes, la cité d'airain veille sur l'horizon. Les chevaliers ailés déploient leurs bannières dorées au coucher du soleil pour défendre la paix éternelle.",
            suggestedMusic = "Épopée Héroïque"
        )
    )

    suspend fun generateScenesFromScript(
        title: String,
        script: String,
        durationSeconds: Int
    ): List<ScriptScene> = withContext(Dispatchers.Default) {
        val sceneCount = when {
            durationSeconds <= 6 -> 2
            durationSeconds <= 12 -> 3
            else -> 4
        }

        // Split sentences cleanly
        val sentences = script.split(Regex("[.!?\n]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (sentences.isEmpty()) {
            return@withContext listOf(
                ScriptScene(
                    order = 1,
                    sceneTitle = title.ifBlank { "Scène Initiale" },
                    narrationText = "Une histoire fantastique commence maintenant sous le regard émerveillé du monde.",
                    subtitleText = "Une histoire fantastique commence maintenant...",
                    visualPrompt = "Plan cinématique lumineux et vibrant",
                    primaryColorHex = 0xFF8E5AFF
                )
            )
        }

        val scenes = mutableListOf<ScriptScene>()
        val chunkSize = (sentences.size + sceneCount - 1) / sceneCount

        for (i in 0 until sceneCount) {
            val fromIndex = (i * chunkSize).coerceAtMost(sentences.size)
            val toIndex = ((i + 1) * chunkSize).coerceAtMost(sentences.size)
            val chunkSentences = if (fromIndex < toIndex) {
                sentences.subList(fromIndex, toIndex).joinToString(". ") + "."
            } else {
                sentences.lastOrNull() ?: "L'aventure continue vers l'infini."
            }

            val sceneTitle = when (i) {
                0 -> "Acte I : Le Prélude"
                1 -> "Acte II : L'Ascension"
                2 -> "Acte III : Le Défi"
                else -> "Acte Final : La Victoire"
            }

            val color = when (i % 4) {
                0 -> 0xFF8E5AFF // Purple
                1 -> 0xFF00E5FF // Cyan
                2 -> 0xFFFFB703 // Amber
                else -> 0xFFFF3377 // Pink
            }

            scenes.add(
                ScriptScene(
                    order = i + 1,
                    sceneTitle = sceneTitle,
                    narrationText = chunkSentences,
                    subtitleText = chunkSentences.take(80) + if (chunkSentences.length > 80) "..." else "",
                    visualPrompt = "Scène $i: $chunkSentences",
                    primaryColorHex = color
                )
            )
        }

        scenes
    }
}
