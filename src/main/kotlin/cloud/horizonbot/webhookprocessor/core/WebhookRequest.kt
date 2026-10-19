package cloud.horizonbot.webhookprocessor.core

data class WebhookRequest(
    val headers: (String) -> String?,
    val body: String,
) {
    fun header(name: String): String? = headers(name)
}
