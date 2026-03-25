package cloud.horizonbot.webhookprocessor.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopGGPayload(
    val bot: String,
    val user: String,
    val type: String,
    @SerialName("isWeekend")
    val isWeekend: Boolean = false,
    val query: String? = null,
)
