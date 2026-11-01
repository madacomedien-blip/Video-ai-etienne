package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricPurple
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
fun PaymentBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPaymentConfirmed: (operator: String, account: String, amountAr: Int, credits: Int) -> Unit,
    onFreeBonusClaimed: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedMethod by remember { mutableStateOf(MobileMoneyOrCardMethod.MVOLA) }

    // Mobile Money Fields
    var mobileMoneyPhone by remember { mutableStateOf("038 28 304 94") }
    var mobileMoneyPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }

    // Card Fields
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvc by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var completedTransaction by remember { mutableStateOf<PaymentTransaction?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            if (completedTransaction != null) {
                // Receipt Success State
                val tx = completedTransaction!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Paiement 200 AR Confirmé !",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = "+5 Animations Vidéo HD ajoutées",
                        fontSize = 14.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Réf Transaction :", fontSize = 11.sp, color = TextSecondary)
                            Text(tx.id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Opérateur :", fontSize = 11.sp, color = TextSecondary)
                            Text(tx.methodDisplayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Compte débité :", fontSize = 11.sp, color = TextSecondary)
                            Text(tx.targetAccountOrPhone, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Montant :", fontSize = 11.sp, color = TextSecondary)
                            Text("200 AR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("modal_receipt_done_button")
                    ) {
                        Text("Créer ma vidéo maintenant", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Header with Pack title and 200 AR price badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pack 5 Animations Vidéo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mobile Money & Carte Bancaire",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberGold.copy(alpha = 0.25f))
                            .border(1.dp, AmberGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "200 AR",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Operator Selection Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MobileMoneyOrCardMethod.values().forEach { method ->
                        val isSelected = selectedMethod == method
                        val brandColor = Color(method.brandColorHex)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) brandColor else DarkSurfaceVariant)
                                .clickable {
                                    selectedMethod = method
                                    if (method.prefixes.isNotEmpty() && !method.prefixes.any { mobileMoneyPhone.startsWith(it) }) {
                                        mobileMoneyPhone = method.prefixes.first()
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Adapted Input form
                if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) {
                    // Credit Card Inputs
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            cardNumber = clean.chunked(4).joinToString(" ").take(19)
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
                        modifier = Modifier.fillMaxWidth().testTag("modal_card_number_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                cardExpiry = if (clean.length > 2) clean.take(2) + "/" + clean.drop(2).take(2) else clean
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
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = cardCvc,
                            onValueChange = { if (it.length <= 3) cardCvc = it },
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
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Mobile Money Inputs (MVola, Orange Money, Airtel Money)
                    val brandColor = Color(selectedMethod.brandColorHex)

                    // Prefixes row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedMethod.prefixes.forEach { prefix ->
                            val isSelected = mobileMoneyPhone.startsWith(prefix)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) brandColor.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) brandColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { mobileMoneyPhone = prefix }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = prefix,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) brandColor else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mobileMoneyPhone,
                        onValueChange = { mobileMoneyPhone = it },
                        label = { Text("Numéro ${selectedMethod.displayName}") },
                        placeholder = { Text(selectedMethod.placeholderPhone) },
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
                        modifier = Modifier.fillMaxWidth().testTag("modal_phone_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mobileMoneyPin,
                        onValueChange = { if (it.length <= 4) mobileMoneyPin = it },
                        label = { Text("Code secret / PIN Mobile Money") },
                        placeholder = { Text("••••") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Afficher PIN",
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
                        modifier = Modifier.fillMaxWidth().testTag("modal_pin_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = processingMessage, fontSize = 12.sp, color = TextSecondary)
                    }
                } else {
                    val brandColor = Color(selectedMethod.brandColorHex)
                    Button(
                        onClick = {
                            isProcessing = true
                            processingMessage = "Connexion sécurisée à ${selectedMethod.displayName}..."
                            coroutineScope.launch {
                                delay(1000)
                                processingMessage = "Validation du débit de 200 AR..."
                                delay(1200)

                                val target = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) {
                                    "•••• " + cardNumber.takeLast(4).ifBlank { "4242" }
                                } else {
                                    mobileMoneyPhone
                                }

                                val txId = "ETN-" + (100000..999999).random() + "-" + selectedMethod.displayName.take(3).uppercase()
                                val tx = PaymentTransaction(
                                    id = txId,
                                    methodId = selectedMethod.id,
                                    methodDisplayName = selectedMethod.displayName,
                                    targetAccountOrPhone = target,
                                    amountAr = 200,
                                    creditsAdded = 5,
                                    timestamp = System.currentTimeMillis()
                                )

                                onPaymentConfirmed(selectedMethod.displayName, target, 200, 5)
                                isProcessing = false
                                completedTransaction = tx
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) AmberGold else brandColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("modal_confirm_pay_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payer 200 AR (${selectedMethod.displayName})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (selectedMethod == MobileMoneyOrCardMethod.CARTE_BANCAIRE) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onFreeBonusClaimed(5)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(ElectricCyan, ElectricPurple))),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("modal_free_gift_button")
                    ) {
                        Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🎁 Activer 5 vidéos offertes (Cadeau)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
