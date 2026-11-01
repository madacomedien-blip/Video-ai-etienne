package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobileMoneyOrCardMethod
import com.example.data.model.PaymentTransaction
import com.example.data.model.TransferVerificationResult
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    currentPurchasedCredits: Int,
    dailyRemaining: Int,
    transactionHistory: List<PaymentTransaction>,
    onBack: () -> Unit,
    onPaymentSuccess: (operatorName: String, accountTarget: String, amountAr: Int, creditsAdded: Int) -> Unit,
    onVerifyTransfer: (String) -> TransferVerificationResult,
    onFreeBonusClaimed: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Acheter, 1: Vérifier transfert
    var selectedMethod by remember { mutableStateOf(MobileMoneyOrCardMethod.MVOLA) }

    // Mobile Money Fields
    var mobileMoneyPhone by remember { mutableStateOf("038 28 304 94") }
    var mobileMoneyPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }

    // Bank Card Fields
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvc by remember { mutableStateOf("") }

    // Processing State
    var isProcessing by remember { mutableStateOf(false) }
    var processingStepMessage by remember { mutableStateOf("") }
    var completedReceipt by remember { mutableStateOf<PaymentTransaction?>(null) }

    // Verification tab state
    var verificationInput by remember { mutableStateOf("038 28 304 94") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationStepMsg by remember { mutableStateOf("") }
    var verificationResult by remember { mutableStateOf<TransferVerificationResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paiement & Vérification 200 AR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("payment_screen_back_button")) {
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
            // Main Mode Tabs (Acheter Pack vs Vérifier Envoi d'argent)
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = DarkSurface,
                    contentColor = ElectricCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = ElectricCyan,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Acheter (200 AR)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vérifier transfert", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }

            // TAB 1: VERIFIER UN ENVOI D'ARGENT
            if (activeTab == 1) {
                item {
                    VerificationSection(
                        input = verificationInput,
                        onInputChange = { verificationInput = it },
                        isVerifying = isVerifying,
                        verifyingMessage = verificationStepMsg,
                        result = verificationResult,
                        onStartVerify = {
                            if (verificationInput.isBlank()) {
                                Toast.makeText(context, "Entrez une référence ou un numéro pour vérifier", Toast.LENGTH_SHORT).show()
                                return@VerificationSection
                            }

                            isVerifying = true
                            verificationStepMsg = "1/3 Interrogation du serveur opérateur (MVola/Orange/Airtel)..."
                            coroutineScope.launch {
                                delay(900)
                                verificationStepMsg = "2/3 Contrôle de la réception de 200 AR sur le compte marchand..."
                                delay(1000)
                                verificationStepMsg = "3/3 Génération du certificat officiel de versement..."
                                delay(800)

                                val res = onVerifyTransfer(verificationInput)
                                isVerifying = false
                                verificationResult = res

                                if (res.isVerifiedReceived) {
                                    Toast.makeText(
                                        context,
                                        "✓ Transfert certifié : 200 AR bien reçus ! +5 vidéos actives",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(context, res.statusMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onQuickSample = { sample ->
                            verificationInput = sample
                        }
                    )
                }
            } else {
                // TAB 0: ACHETER LE PACK (200 AR)
                // Receipt / Success View
                if (completedReceipt != null) {
                    item {
                        ReceiptCard(
                            transaction = completedReceipt!!,
                            onDone = {
                                completedReceipt = null
                                onBack()
                            },
                            onVerifyDirect = {
                                verificationInput = completedReceipt!!.id
                                activeTab = 1
                            }
                        )
                    }
                } else {
                    // Pack Offer Hero Card
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.5.dp,
                                    Brush.horizontalGradient(listOf(AmberGold, ElectricPurple)),
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(AmberGold.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stars,
                                                contentDescription = null,
                                                tint = AmberGold,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Pack Étienne Anime",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "5 Animations Vidéo HD",
                                                fontSize = 12.sp,
                                                color = ElectricCyan,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "200 AR",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp,
                                            color = AmberGold
                                        )
                                        Text(
                                            text = "40 AR / vidéo",
                                            fontSize = 11.sp,
                                            color = SuccessGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Créez 5 vidéos complètes avec découpage de scènes IA, mouvements de caméra 2.5D, voix française, musique d'ambiance et téléchargement MP4 sans attente.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Crédit immédiat sur le compte",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Text(
                                        text = "Solde: +$currentPurchasedCredits crédits",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                }
                            }
                        }
                    }

                    // Operator / Payment Method Selector
                    item {
                        Text(
                            text = "Sélectionnez le moyen de paiement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MobileMoneyOrCardMethod.values().forEach { method ->
                                val isSelected = selectedMethod == method
                                val brandColor = Color(method.brandColorHex)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) brandColor else DarkSurfaceBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            selectedMethod = method
                                            if (method.prefixes.isNotEmpty() && !method.prefixes.any { mobileMoneyPhone.startsWith(it) }) {
                                                mobileMoneyPhone = method.prefixes.first()
                                            }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(brandColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (method == MobileMoneyOrCardMethod.CARTE_BANCAIRE) Icons.Default.CreditCard else Icons.Default.PhoneAndroid,
                                                contentDescription = null,
                                                tint = brandColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = method.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Specific Input Form based on selected method
                    item {
                        if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) {
                            // BANK CARD INTERFACE
                            BankCardInputSection(
                                cardNumber = cardNumber,
                                onCardNumberChange = { if (it.length <= 19) cardNumber = it },
                                cardHolderName = cardHolderName,
                                onCardHolderChange = { cardHolderName = it },
                                cardExpiry = cardExpiry,
                                onCardExpiryChange = { if (it.length <= 5) cardExpiry = it },
                                cardCvc = cardCvc,
                                onCardCvcChange = { if (it.length <= 3) cardCvc = it }
                            )
                        } else {
                            // MOBILE MONEY INTERFACE (MVola, Orange Money, Airtel Money)
                            MobileMoneyInputSection(
                                method = selectedMethod,
                                phoneNumber = mobileMoneyPhone,
                                onPhoneChange = { mobileMoneyPhone = it },
                                pin = mobileMoneyPin,
                                onPinChange = { if (it.length <= 4) mobileMoneyPin = it },
                                showPin = showPin,
                                onToggleShowPin = { showPin = !showPin }
                            )
                        }
                    }

                    // Payment Action Button or Processing State
                    item {
                        if (isProcessing) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                color = DarkSurface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = ElectricCyan,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = processingStepMessage,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Veuillez patienter pendant la transaction sécurisée...",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else {
                            val brandColor = Color(selectedMethod.brandColorHex)
                            Button(
                                onClick = {
                                    // Validation
                                    if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) {
                                        val digitsOnly = cardNumber.replace(" ", "")
                                        if (digitsOnly.length < 12) {
                                            Toast.makeText(context, "Veuillez saisir un numéro de carte valide", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                    } else {
                                        val digitsOnly = mobileMoneyPhone.replace(" ", "")
                                        if (digitsOnly.length < 9) {
                                            Toast.makeText(context, "Veuillez saisir un numéro Mobile Money complet", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                    }

                                    isProcessing = true
                                    processingStepMessage = "1/3 Connexion au réseau ${selectedMethod.displayName}..."

                                    coroutineScope.launch {
                                        delay(1000)
                                        processingStepMessage = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) {
                                            "2/3 Vérification 3D Secure et autorisation bancaire..."
                                        } else {
                                            "2/3 Envoi de la notification Push USSD sur votre mobile..."
                                        }
                                        delay(1200)
                                        processingStepMessage = "3/3 Validation du débit de 200 AR et génération des crédits..."
                                        delay(1000)

                                        val targetIdentifier = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) {
                                            "•••• " + cardNumber.takeLast(4).ifBlank { "4242" }
                                        } else {
                                            mobileMoneyPhone
                                        }

                                        val txId = "ETN-" + (100000..999999).random() + "-" + selectedMethod.displayName.take(3).uppercase()
                                        val newTx = PaymentTransaction(
                                            id = txId,
                                            methodId = selectedMethod.id,
                                            methodDisplayName = selectedMethod.displayName,
                                            targetAccountOrPhone = targetIdentifier,
                                            amountAr = 200,
                                            creditsAdded = 5,
                                            timestamp = System.currentTimeMillis()
                                        )

                                        onPaymentSuccess(selectedMethod.displayName, targetIdentifier, 200, 5)
                                        isProcessing = false
                                        completedReceipt = newTx
                                        Toast.makeText(context, "Paiement de 200 AR validé ! +5 vidéos ajoutées", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) AmberGold else brandColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("validate_payment_200_ar_button")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Payer 200 AR avec ${selectedMethod.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) Color.Black else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Free alternative claim
                            OutlinedButton(
                                onClick = {
                                    onFreeBonusClaimed(5)
                                    Toast.makeText(context, "🎁 5 Vidéos gratuites activées !", Toast.LENGTH_SHORT).show()
                                    onBack()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(ElectricCyan, ElectricPurple))
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("free_bonus_pack_button")
                            ) {
                                Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🎁 Activer 5 vidéos offertes (Cadeau Étienne)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Recent Transaction History Section if any
                    if (transactionHistory.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Historique de vos recharges",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                transactionHistory.take(4).forEach { tx ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                                        color = DarkSurface
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${tx.methodDisplayName} (${tx.targetAccountOrPhone})",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary
                                                )
                                                val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
                                                Text(
                                                    text = "Réf: ${tx.id} • $dateFormatted",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SuccessGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "+${tx.creditsAdded} vidéos",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Verification section verifying that money was truly received on merchant account
 */
@Composable
private fun VerificationSection(
    input: String,
    onInputChange: (String) -> Unit,
    isVerifying: Boolean,
    verifyingMessage: String,
    result: TransferVerificationResult?,
    onStartVerify: () -> Unit,
    onQuickSample: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp)),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Vérifier la réception de l'argent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Contrôle en direct sur les passerelles Mobile Money",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Entrez la référence de votre transaction SMS (ex: ETN-..., MV..., OM..., AM...) ou le numéro de téléphone utilisé pour envoyer les 200 AR vers notre compte marchand.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Samples chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "034 50 123 45",
                    "MV241031.1420",
                    "OM-200AR-OK"
                ).forEach { sample ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { onQuickSample(sample) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Ex: $sample",
                            fontSize = 10.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text("Référence SMS ou Numéro émetteur") },
                placeholder = { Text("Ex: ETN-123456 ou 034 xx xxx xx") },
                leadingIcon = {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = ElectricCyan)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_input_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isVerifying) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(verifyingMessage, fontSize = 12.sp, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = onStartVerify,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_verification_button")
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vérifier le virement de 200 AR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Display Certificate Result
            if (result != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (result.isVerifiedReceived) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f))
                        .border(1.5.dp, if (result.isVerifiedReceived) SuccessGreen else ErrorRed, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (result.isVerifiedReceived) Icons.Default.CheckCircle else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (result.isVerifiedReceived) SuccessGreen else ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (result.isVerifiedReceived) "ARGENT BIEN REÇU (200 AR)" else "NON TROUVÉ",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (result.isVerifiedReceived) SuccessGreen else ErrorRed
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (result.isVerifiedReceived) SuccessGreen else ErrorRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (result.isVerifiedReceived) "CONFIRMÉ" else "ÉCHEC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = result.statusMessage,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        if (result.isVerifiedReceived) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground.copy(alpha = 0.6f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ReceiptRow("Opérateur vérifié :", result.operatorName)
                                ReceiptRow("Compte émetteur :", result.senderPhoneOrAccount)
                                ReceiptRow("Compte marchand :", result.recipientMerchant)
                                ReceiptRow("Montant encaissé :", "${result.amountAr} AR")
                                ReceiptRow("Code passerelle :", result.operatorTxCode)
                                val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(result.timestamp))
                                ReceiptRow("Horodatage :", dateFmt)
                                ReceiptRow("Crédits débloqués :", "+5 Animations Vidéo HD ✓")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mobile Money specialized input section (MVola, Orange Money, Airtel Money)
 */
@Composable
private fun MobileMoneyInputSection(
    method: MobileMoneyOrCardMethod,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    showPin: Boolean,
    onToggleShowPin: () -> Unit
) {
    val brandColor = Color(method.brandColorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp)),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(brandColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Numéro de compte ${method.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = method.subtitle.split("(").last().replace(")", ""),
                    fontSize = 11.sp,
                    color = brandColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Operator Prefix Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                method.prefixes.forEach { prefix ->
                    val isSelected = phoneNumber.startsWith(prefix)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) brandColor.copy(alpha = 0.25f) else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) brandColor else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                onPhoneChange(prefix)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = prefix,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) brandColor else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                label = { Text("Numéro Mobile Money (Madagascar)") },
                placeholder = { Text(method.placeholderPhone) },
                leadingIcon = {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = brandColor)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandColor,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_money_number_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PIN Mobile Money Confirmation Field
            OutlinedTextField(
                value = pin,
                onValueChange = onPinChange,
                label = { Text("Code secret / PIN Mobile Money (4 chiffres)") },
                placeholder = { Text("••••") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold)
                },
                trailingIcon = {
                    IconButton(onClick = onToggleShowPin) {
                        Icon(
                            imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPin) "Masquer PIN" else "Afficher PIN",
                            tint = TextSecondary
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_money_pin_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // USSD Instruction Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = brandColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notification USSD sécurisée envoyée vers votre mobile. Montant exact débité : 200 AR.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Bank Card specialized input section with live card preview mockup
 */
@Composable
private fun BankCardInputSection(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardHolderName: String,
    onCardHolderChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    cardCvc: String,
    onCardCvcChange: (String) -> Unit
) {
    Column {
        // Visual Credit Card Mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A))
                    )
                )
                .border(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip mockup
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(AmberGold)
                    )

                    // Card Brand (Visa or Mastercard)
                    Text(
                        text = if (cardNumber.startsWith("4")) "VISA" else if (cardNumber.startsWith("5")) "Mastercard" else "CB",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                // Card Number display
                val formattedNumber = if (cardNumber.isBlank()) "•••• •••• •••• ••••" else {
                    cardNumber.replace(" ", "").chunked(4).joinToString(" ")
                }
                Text(
                    text = formattedNumber,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TITULAIRE",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = cardHolderName.ifBlank { "NOM DU CLIENT" }.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXPIRE",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = cardExpiry.ifBlank { "MM/AA" },
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp)),
            color = DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        val formatted = clean.chunked(4).joinToString(" ")
                        onCardNumberChange(formatted)
                    },
                    label = { Text("Numéro de carte (16 chiffres)") },
                    placeholder = { Text("4242 •••• •••• 4242") },
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = ElectricCyan)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("card_number_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Name
                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = onCardHolderChange,
                    label = { Text("Nom sur la carte") },
                    placeholder = { Text("Ex: RAZAKA ETIENNE") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("card_holder_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            val formatted = if (clean.length > 2) clean.take(2) + "/" + clean.drop(2).take(2) else clean
                            onCardExpiryChange(formatted)
                        },
                        label = { Text("MM/AA") },
                        placeholder = { Text("12/28") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("card_expiry_field")
                    )

                    OutlinedTextField(
                        value = cardCvc,
                        onValueChange = onCardCvcChange,
                        label = { Text("CVC") },
                        placeholder = { Text("123") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("card_cvc_field")
                    )
                }
            }
        }
    }
}

/**
 * Receipt & invoice display after payment success
 */
@Composable
private fun ReceiptCard(
    transaction: PaymentTransaction,
    onDone: () -> Unit,
    onVerifyDirect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.5.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Paiement Validé avec Succès !",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = TextPrimary
            )

            Text(
                text = "+5 Animations Vidéo HD Créditées",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Details Invoice Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReceiptRow(label = "Référence :", value = transaction.id)
                ReceiptRow(label = "Moyen de paiement :", value = transaction.methodDisplayName)
                ReceiptRow(label = "Compte débité :", value = transaction.targetAccountOrPhone)
                ReceiptRow(label = "Montant débité :", value = "${transaction.amountAr} AR")
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(transaction.timestamp))
                ReceiptRow(label = "Date & Heure :", value = dateStr)
                ReceiptRow(label = "Statut :", value = "Confirmé ✓")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onVerifyDirect,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(ElectricCyan, SuccessGreen))
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vérifier le statut du virement sur le réseau", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("receipt_done_button")
            ) {
                Text(
                    text = "Créer ma vidéo maintenant",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
