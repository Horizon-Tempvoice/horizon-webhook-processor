package cloud.horizonbot.webhookprocessor.core

fun interface SignatureVerifier {
    fun verify(request: WebhookRequest): VerificationResult
}
