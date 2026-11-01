package com.example.data.model

enum class MobileMoneyOrCardMethod(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val brandColorHex: Long,
    val prefixes: List<String>,
    val placeholderPhone: String
) {
    MVOLA(
        id = "MVOLA",
        displayName = "MVola",
        subtitle = "Telma Madagascar (038, 034)",
        brandColorHex = 0xFF00A859, // Iconic Telma / MVola Green
        prefixes = listOf("038", "034"),
        placeholderPhone = "038 28 304 94"
    ),
    ORANGE_MONEY(
        id = "ORANGE_MONEY",
        displayName = "Orange Money",
        subtitle = "Orange Madagascar (032, 037)",
        brandColorHex = 0xFFFF7900, // Iconic Orange
        prefixes = listOf("032", "037"),
        placeholderPhone = "032 XX XXX XX"
    ),
    AIRTEL_MONEY(
        id = "AIRTEL_MONEY",
        displayName = "Airtel Money",
        subtitle = "Airtel Madagascar (033)",
        brandColorHex = 0xFFED1C24, // Iconic Airtel Red
        prefixes = listOf("033"),
        placeholderPhone = "033 XX XXX XX"
    ),
    CARTE_BANCAIRE(
        id = "CARTE_BANCAIRE",
        displayName = "Carte Bancaire",
        subtitle = "Visa & Mastercard sécurisé",
        brandColorHex = 0xFF1A73E8, // Visa / Bank Blue
        prefixes = emptyList(),
        placeholderPhone = ""
    )
}

data class PaymentTransaction(
    val id: String,
    val methodId: String,
    val methodDisplayName: String,
    val targetAccountOrPhone: String,
    val amountAr: Int = 200,
    val creditsAdded: Int = 5,
    val timestamp: Long = System.currentTimeMillis()
)

data class TransferVerificationResult(
    val reference: String,
    val senderPhoneOrAccount: String,
    val recipientMerchant: String = "Marchand Agréé Video AI Étienne (+261 34 89 200 00)",
    val operatorName: String,
    val amountAr: Int = 200,
    val isVerifiedReceived: Boolean,
    val statusMessage: String,
    val operatorTxCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val wasAlreadyCredited: Boolean = false
)
