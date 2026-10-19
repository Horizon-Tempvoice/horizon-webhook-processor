package cloud.horizonbot.webhookprocessor.adapters.topgg

import cloud.horizonbot.webhookprocessor.core.SignatureVerifier
import cloud.horizonbot.webhookprocessor.core.VerificationResult
import cloud.horizonbot.webhookprocessor.core.WebhookRequest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TopGGSignatureVerifier(
    private val secret: String,
) : SignatureVerifier {
    override fun verify(request: WebhookRequest): VerificationResult {
        val header = request.header(HEADER) ?: return VerificationResult.Missing("Missing $HEADER header")

        return if (matches(header, request.body)) {
            VerificationResult.Valid
        } else {
            VerificationResult.Invalid("Invalid top.gg webhook signature")
        }
    }

    private fun matches(
        header: String,
        body: String,
    ): Boolean {
        val parts =
            header.split(",").associate {
                val (k, v) = it.split("=", limit = 2)
                k to v
            }
        val timestamp = parts["t"] ?: return false
        val receivedSig = parts["v1"] ?: return false

        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), ALGORITHM))
        val computed = mac.doFinal("$timestamp.$body".toByteArray(Charsets.UTF_8))
        val computedHex = computed.joinToString("") { "%02x".format(it) }

        return computedHex == receivedSig
    }

    private companion object {
        const val HEADER = "x-topgg-signature"
        const val ALGORITHM = "HmacSHA256"
    }
}
