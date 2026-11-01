package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.VideoCreationEntity
import com.example.data.model.GenerationConfig
import com.example.data.repository.VideoRepository
import com.example.engine.video.VideoEncoder
import com.example.ui.components.PaymentBottomSheet
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.GenerationProgressScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.PhotoModeScreen
import com.example.ui.screens.PreviewPlayerScreen
import com.example.ui.screens.ScriptModeScreen
import com.example.ui.theme.VideoAIEtienneTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    SCRIPT_MODE,
    PHOTO_MODE,
    GENERATING,
    PREVIEW,
    GALLERY,
    PAYMENT
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: VideoRepository
    private lateinit var videoEncoder: VideoEncoder

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = VideoRepository(applicationContext)
        videoEncoder = VideoEncoder(applicationContext)

        setContent {
            VideoAIEtienneTheme {
                val coroutineScope = rememberCoroutineScope()
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                val creations by repository.getAllCreations().collectAsState(initial = emptyList())

                var dailyRemaining by remember { mutableIntStateOf(repository.getDailyRemaining()) }
                var purchasedCredits by remember { mutableIntStateOf(repository.getPurchasedCredits()) }

                // Script navigation arguments
                var prefillScript by remember { mutableStateOf<String?>(null) }
                var prefillTitle by remember { mutableStateOf<String?>(null) }

                // Generation & Preview state
                var currentGeneratingTitle by remember { mutableStateOf("Création de votre vidéo...") }
                var generationProgress by remember {
                    mutableStateOf(
                        VideoEncoder.GenerationProgress(
                            step = 1,
                            message = "Initialisation...",
                            percentage = 0.05f
                        )
                    )
                }
                var activePreviewCreation by remember { mutableStateOf<VideoCreationEntity?>(null) }

                // Payment modal
                var showPaymentModal by remember { mutableStateOf(false) }
                val paymentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                fun refreshQuota() {
                    dailyRemaining = repository.getDailyRemaining()
                    purchasedCredits = repository.getPurchasedCredits()
                }

                fun startVideoGeneration(config: GenerationConfig) {
                    if (!repository.canGenerate()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Limite quotidienne atteinte. Prenez le pack 200 Ar ou activez les 5 vidéos offertes !",
                            Toast.LENGTH_LONG
                        ).show()
                        showPaymentModal = true
                        return
                    }

                    // Consume one credit
                    repository.consumeOneGeneration()
                    refreshQuota()

                    currentGeneratingTitle = config.title
                    generationProgress = VideoEncoder.GenerationProgress(
                        step = 1,
                        message = "1/4 Analyse du script et préparation des scènes...",
                        percentage = 0.10f
                    )
                    currentScreen = AppScreen.GENERATING

                    coroutineScope.launch {
                        try {
                            val result = videoEncoder.generateVideo(config) { p ->
                                generationProgress = p
                            }

                            // Save to Room DB
                            val entity = VideoCreationEntity(
                                id = result.id,
                                title = result.title,
                                mode = config.mode.name,
                                aspectRatio = config.aspectRatio.label,
                                durationSeconds = result.durationSeconds,
                                videoFilePath = result.mp4File.absolutePath,
                                thumbnailFilePath = result.thumbnailFile.absolutePath,
                                scriptOrCaption = result.scriptOrCaption,
                                styleName = if (config.mode.name == "SCRIPT") config.visualFilter.title else config.animationStyle.title,
                                musicTrackName = config.musicTrack.title
                            )
                            repository.saveCreation(entity)

                            activePreviewCreation = entity
                            currentScreen = AppScreen.PREVIEW
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Erreur de génération : ${e.localizedMessage ?: "Codec indisponible"}",
                                Toast.LENGTH_LONG
                            ).show()
                            currentScreen = AppScreen.HOME
                        }
                    }
                }

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            dailyRemaining = dailyRemaining,
                            dailyLimit = VideoRepository.DAILY_FREE_LIMIT,
                            purchasedCredits = purchasedCredits,
                            recentCreations = creations,
                            onNavigateToScriptMode = { script, title ->
                                prefillScript = script
                                prefillTitle = title
                                currentScreen = AppScreen.SCRIPT_MODE
                            },
                            onNavigateToPhotoMode = {
                                currentScreen = AppScreen.PHOTO_MODE
                            },
                            onNavigateToGallery = {
                                currentScreen = AppScreen.GALLERY
                            },
                            onOpenPaymentModal = {
                                currentScreen = AppScreen.PAYMENT
                            },
                            onPlayVideo = { creation ->
                                activePreviewCreation = creation
                                currentScreen = AppScreen.PREVIEW
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppScreen.SCRIPT_MODE -> {
                        BackHandler {
                            currentScreen = AppScreen.HOME
                        }
                        ScriptModeScreen(
                            initialScript = prefillScript,
                            initialTitle = prefillTitle,
                            onBack = { currentScreen = AppScreen.HOME },
                            onGenerate = { config ->
                                startVideoGeneration(config)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppScreen.PHOTO_MODE -> {
                        BackHandler {
                            currentScreen = AppScreen.HOME
                        }
                        PhotoModeScreen(
                            onBack = { currentScreen = AppScreen.HOME },
                            onGenerate = { config ->
                                startVideoGeneration(config)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppScreen.GENERATING -> {
                        // Prevent back while generating
                        BackHandler(enabled = true) {
                            // Video is currently generating
                        }
                        GenerationProgressScreen(
                            progress = generationProgress,
                            videoTitle = currentGeneratingTitle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppScreen.PREVIEW -> {
                        BackHandler {
                            currentScreen = AppScreen.HOME
                        }
                        activePreviewCreation?.let { creation ->
                            PreviewPlayerScreen(
                                creation = creation,
                                onBack = { currentScreen = AppScreen.HOME },
                                onNewVideo = { currentScreen = AppScreen.HOME },
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            currentScreen = AppScreen.HOME
                        }
                    }

                    AppScreen.GALLERY -> {
                        BackHandler {
                            currentScreen = AppScreen.HOME
                        }
                        GalleryScreen(
                            creations = creations,
                            onBack = { currentScreen = AppScreen.HOME },
                            onPlayVideo = { creation ->
                                activePreviewCreation = creation
                                currentScreen = AppScreen.PREVIEW
                            },
                            onDeleteVideo = { creation ->
                                coroutineScope.launch {
                                    repository.deleteCreation(creation.id)
                                }
                            },
                            onNavigateToCreate = {
                                currentScreen = AppScreen.HOME
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppScreen.PAYMENT -> {
                        BackHandler {
                            currentScreen = AppScreen.HOME
                        }
                        PaymentScreen(
                            currentPurchasedCredits = purchasedCredits,
                            dailyRemaining = dailyRemaining,
                            transactionHistory = repository.getTransactionHistory(),
                            onBack = { currentScreen = AppScreen.HOME },
                            onPaymentSuccess = { operator, account, amount, credits ->
                                repository.recordPaymentTransaction(operator, account, amount, credits)
                                refreshQuota()
                            },
                            onVerifyTransfer = { input ->
                                val res = repository.verifyAndConfirmTransfer(input)
                                refreshQuota()
                                res
                            },
                            onFreeBonusClaimed = { count ->
                                repository.addBonusCredits(count)
                                refreshQuota()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Payment / Top-Up Modal (also accessible from quota gates)
                if (showPaymentModal) {
                    PaymentBottomSheet(
                        sheetState = paymentSheetState,
                        onDismiss = { showPaymentModal = false },
                        onPaymentConfirmed = { operator, account, amount, credits ->
                            repository.recordPaymentTransaction(operator, account, amount, credits)
                            refreshQuota()
                            Toast.makeText(
                                this@MainActivity,
                                "+$credits Vidéos créditées avec succès !",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFreeBonusClaimed = { count ->
                            repository.addBonusCredits(count)
                            refreshQuota()
                            Toast.makeText(
                                this@MainActivity,
                                "🎁 +$count Vidéos offertes activées !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}
