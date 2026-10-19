package cloud.horizonbot.webhookprocessor.core

sealed interface VerificationResult {
    data object Valid : VerificationResult

    sealed interface Rejected : VerificationResult {
        val reason: String
    }

    data class Missing(override val reason: String) : Rejected

    data class Invalid(override val reason: String) : Rejected
}
