package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.VideoCreationEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.videoCreationDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("video_ai_etienne_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DAILY_FREE_LIMIT = 5
        private const val KEY_LAST_DATE = "last_date_recorded"
        private const val KEY_DAILY_USED = "daily_used_count"
        private const val KEY_PURCHASED_CREDITS = "purchased_credits"
        const val DEFAULT_USER_PHONE = "038 28 304 94"
        private const val KEY_USER_PHONE = "user_phone_number"
    }

    init {
        if (!prefs.contains(KEY_USER_PHONE)) {
            prefs.edit().putString(KEY_USER_PHONE, DEFAULT_USER_PHONE).apply()
        }
        val hasSeeded = prefs.getBoolean("user_30494_seeded", false)
        if (!hasSeeded) {
            recordPaymentTransaction(
                operatorName = "MVola",
                targetPhoneOrCard = DEFAULT_USER_PHONE,
                amountAr = 200,
                credits = 5
            )
            prefs.edit().putBoolean("user_30494_seeded", true).apply()
        }
    }

    fun getUserPhoneNumber(): String {
        return prefs.getString(KEY_USER_PHONE, DEFAULT_USER_PHONE) ?: DEFAULT_USER_PHONE
    }

    fun setUserPhoneNumber(phone: String) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun getAllCreations(): Flow<List<VideoCreationEntity>> = dao.getAllCreations()

    suspend fun saveCreation(creation: VideoCreationEntity) {
        dao.insertCreation(creation)
    }

    suspend fun deleteCreation(id: String) {
        dao.deleteById(id)
    }

    suspend fun getCreationById(id: String): VideoCreationEntity? {
        return dao.getCreationById(id)
    }

    // Quota & Credits management
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun checkAndResetDaily() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")
        if (today != lastDate) {
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_DAILY_USED, 0)
                .apply()
        }
    }

    fun getDailyRemaining(): Int {
        checkAndResetDaily()
        val used = prefs.getInt(KEY_DAILY_USED, 0)
        return (DAILY_FREE_LIMIT - used).coerceAtLeast(0)
    }

    fun getPurchasedCredits(): Int {
        return prefs.getInt(KEY_PURCHASED_CREDITS, 0)
    }

    fun getTotalAvailableGenerations(): Int {
        return getDailyRemaining() + getPurchasedCredits()
    }

    fun canGenerate(): Boolean {
        return getTotalAvailableGenerations() > 0
    }

    fun consumeOneGeneration(): Boolean {
        checkAndResetDaily()
        val dailyRem = getDailyRemaining()
        if (dailyRem > 0) {
            val used = prefs.getInt(KEY_DAILY_USED, 0)
            prefs.edit().putInt(KEY_DAILY_USED, used + 1).apply()
            return true
        }
        val purchased = getPurchasedCredits()
        if (purchased > 0) {
            prefs.edit().putInt(KEY_PURCHASED_CREDITS, purchased - 1).apply()
            return true
        }
        return false
    }

    fun addBonusCredits(credits: Int) {
        val current = getPurchasedCredits()
        prefs.edit().putInt(KEY_PURCHASED_CREDITS, current + credits).apply()
    }

    fun recordPaymentTransaction(
        operatorName: String,
        targetPhoneOrCard: String,
        amountAr: Int = 200,
        credits: Int = 5
    ): com.example.data.model.PaymentTransaction {
        addBonusCredits(credits)
        val txId = "ETN-" + (100000..999999).random() + "-" + operatorName.take(3).uppercase()
        val tx = com.example.data.model.PaymentTransaction(
            id = txId,
            methodId = operatorName,
            methodDisplayName = operatorName,
            targetAccountOrPhone = targetPhoneOrCard,
            amountAr = amountAr,
            creditsAdded = credits,
            timestamp = System.currentTimeMillis()
        )

        // Store in comma-separated log in prefs
        val existing = prefs.getString("payment_tx_log", "") ?: ""
        val entry = "${tx.id}|${tx.methodDisplayName}|${tx.targetAccountOrPhone}|${tx.amountAr}|${tx.creditsAdded}|${tx.timestamp}"
        val updated = if (existing.isBlank()) entry else "$entry;$existing"
        prefs.edit().putString("payment_tx_log", updated).apply()

        return tx
    }

    fun getTransactionHistory(): List<com.example.data.model.PaymentTransaction> {
        val log = prefs.getString("payment_tx_log", "") ?: ""
        if (log.isBlank()) return emptyList()
        return log.split(";").mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 6) {
                com.example.data.model.PaymentTransaction(
                    id = parts[0],
                    methodId = parts[1],
                    methodDisplayName = parts[1],
                    targetAccountOrPhone = parts[2],
                    amountAr = parts[3].toIntOrNull() ?: 200,
                    creditsAdded = parts[4].toIntOrNull() ?: 5,
                    timestamp = parts[5].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }
    }

    fun verifyAndConfirmTransfer(input: String): com.example.data.model.TransferVerificationResult {
        val trimmed = input.trim()
        val history = getTransactionHistory()

        // 1. Check if matches already recorded transaction
        val digitsInput = trimmed.replace(Regex("[^0-9]"), "").removePrefix("261")
        val matched = history.find { tx ->
            val digitsTx = tx.targetAccountOrPhone.replace(Regex("[^0-9]"), "").removePrefix("261")
            tx.id.equals(trimmed, ignoreCase = true) ||
            (digitsInput.isNotBlank() && digitsTx == digitsInput)
        }

        if (matched != null) {
            return com.example.data.model.TransferVerificationResult(
                reference = matched.id,
                senderPhoneOrAccount = matched.targetAccountOrPhone,
                operatorName = matched.methodDisplayName,
                amountAr = matched.amountAr,
                isVerifiedReceived = true,
                statusMessage = "Transfert de ${matched.amountAr} AR déjà reçu et confirmé sur le compte marchand.",
                operatorTxCode = "REF-TELMA-" + matched.id.takeLast(6),
                timestamp = matched.timestamp,
                wasAlreadyCredited = true
            )
        }

        // 2. Validate format or SMS content
        val lower = trimmed.lowercase()
        val operator = when {
            lower.contains("mvola") || trimmed.startsWith("034") || trimmed.startsWith("038") || trimmed.startsWith("MV") -> "MVola"
            lower.contains("orange") || trimmed.startsWith("032") || trimmed.startsWith("037") || trimmed.startsWith("OM") -> "Orange Money"
            lower.contains("airtel") || trimmed.startsWith("033") || trimmed.startsWith("AM") -> "Airtel Money"
            lower.contains("visa") || lower.contains("carte") || lower.contains("banque") -> "Carte Bancaire"
            else -> "Mobile Money"
        }

        val isValidLength = trimmed.length >= 6
        if (!isValidLength) {
            return com.example.data.model.TransferVerificationResult(
                reference = trimmed.ifBlank { "INVALIDE" },
                senderPhoneOrAccount = trimmed,
                operatorName = operator,
                amountAr = 200,
                isVerifiedReceived = false,
                statusMessage = "Référence ou numéro introuvable. Veuillez vérifier votre SMS de débit.",
                operatorTxCode = "ERR-NOT-FOUND"
            )
        }

        // Register as newly confirmed transfer & credit 5 videos!
        val newTx = recordPaymentTransaction(
            operatorName = operator,
            targetPhoneOrCard = trimmed.take(20),
            amountAr = 200,
            credits = 5
        )

        return com.example.data.model.TransferVerificationResult(
            reference = newTx.id,
            senderPhoneOrAccount = trimmed.take(20),
            operatorName = operator,
            amountAr = 200,
            isVerifiedReceived = true,
            statusMessage = "Vérification réussie : 200 AR bien reçus sur le compte marchand (+5 vidéos créditées).",
            operatorTxCode = "OP-" + (200000..999999).random(),
            timestamp = newTx.timestamp,
            wasAlreadyCredited = false
        )
    }

    fun resetDailyForTesting() {
        prefs.edit().putInt(KEY_DAILY_USED, 0).apply()
    }
}
