package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.VideoRepository
import com.example.engine.ai.ScriptAiEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Video AI Étienne", appName)
  }

  @Test
  fun `test video repository initial quota`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = VideoRepository(context)
    assertEquals(5, repository.getDailyRemaining())
    // 5 credits seeded for user 038 28 304 94
    assertEquals(5, repository.getPurchasedCredits())
    assertTrue(repository.canGenerate())
  }

  @Test
  fun `test user phone 038 28 304 94 verified transfer`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = VideoRepository(context)
    assertEquals("038 28 304 94", repository.getUserPhoneNumber())

    val result = repository.verifyAndConfirmTransfer("038 28 304 94")
    assertTrue(result.isVerifiedReceived)
    assertEquals("MVola", result.operatorName)
    assertEquals(200, result.amountAr)
  }

  @Test
  fun `test 200 AR pack purchase transaction`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = VideoRepository(context)
    val initialCredits = repository.getPurchasedCredits()
    val tx = repository.recordPaymentTransaction(
      operatorName = "MVola",
      targetPhoneOrCard = "038 28 304 94",
      amountAr = 200,
      credits = 5
    )
    assertEquals("MVola", tx.methodDisplayName)
    assertEquals(200, tx.amountAr)
    assertEquals(5, tx.creditsAdded)
    assertTrue(tx.id.startsWith("ETN-"))
    assertEquals(initialCredits + 5, repository.getPurchasedCredits())

    val history = repository.getTransactionHistory()
    assertTrue(history.isNotEmpty())
    assertEquals(tx.id, history.first().id)
  }

  @Test
  fun `test verify and confirm money sent`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = VideoRepository(context)

    // Test verifying with transaction ID
    val tx = repository.recordPaymentTransaction(
      operatorName = "Orange Money",
      targetPhoneOrCard = "032 99 888 77",
      amountAr = 200,
      credits = 5
    )

    val verificationResult = repository.verifyAndConfirmTransfer(tx.id)
    assertTrue(verificationResult.isVerifiedReceived)
    assertEquals(200, verificationResult.amountAr)
    assertEquals("Orange Money", verificationResult.operatorName)
    assertTrue(verificationResult.wasAlreadyCredited)

    // Test verifying a new transaction reference directly
    val newVerification = repository.verifyAndConfirmTransfer("MV241031.9999.TELMA")
    assertTrue(newVerification.isVerifiedReceived)
    assertEquals("MVola", newVerification.operatorName)
    assertEquals(200, newVerification.amountAr)
  }

  @Test
  fun `test script AI scene breakdown`() = runBlocking {
    val scenes = ScriptAiEngine.generateScenesFromScript(
      title = "Héros des Étoiles",
      script = "Le jeune aventurier découvre une épée légendaire. Il part affronter les ténèbres. La victoire illumine le royaume.",
      durationSeconds = 10
    )
    assertTrue(scenes.isNotEmpty())
    assertEquals(3, scenes.size)
  }
}
