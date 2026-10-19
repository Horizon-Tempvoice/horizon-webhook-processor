package cloud.horizonbot.webhookprocessor.core

import java.time.Instant

data class VoteEvent(
    val platform: String,
    val platformName: String,
    val userId: Long,
    val userName: String,
    val voteId: String?,
    val votedAt: Instant,
    val expiresAt: Instant,
    val voteUrl: String,
    val isTest: Boolean,
)
