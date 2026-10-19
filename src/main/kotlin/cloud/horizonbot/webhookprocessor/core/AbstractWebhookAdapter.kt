package cloud.horizonbot.webhookprocessor.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

abstract class AbstractWebhookAdapter<P : Any>(
    private val serializer: KSerializer<P>,
    private val verifier: SignatureVerifier,
) : WebhookAdapter {
    override fun verify(request: WebhookRequest): VerificationResult = verifier.verify(request)

    override fun parse(request: WebhookRequest): ParseResult = toResult(json.decodeFromString(serializer, request.body))

    protected abstract fun toResult(payload: P): ParseResult

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
