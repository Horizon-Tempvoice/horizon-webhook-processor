package cloud.horizonbot.webhookprocessor.core

interface WebhookAdapter {
    val path: String

    fun verify(request: WebhookRequest): VerificationResult

    fun parse(request: WebhookRequest): ParseResult
}
