package cloud.horizonbot.webhookprocessor.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopGGPayload(
    val type: String,
    val data: TopGGEventData? = null,
)

@Serializable
data class TopGGEventData(
    val id: String? = null,
    val weight: Int = 1,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    val user: TopGGUser,
    val project: TopGGProject,
)

@Serializable
data class TopGGUser(
    val id: String,
    @SerialName("platform_id")
    val platformId: String,
    val name: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
)

@Serializable
data class TopGGProject(
    val id: String,
    val type: String,
    val platform: String,
    @SerialName("platform_id")
    val platformId: String,
)
