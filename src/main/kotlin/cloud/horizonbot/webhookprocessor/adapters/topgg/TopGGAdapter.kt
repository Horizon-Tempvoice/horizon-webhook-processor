package cloud.horizonbot.webhookprocessor.adapters.topgg

import cloud.horizonbot.webhookprocessor.core.AbstractWebhookAdapter
import cloud.horizonbot.webhookprocessor.core.ParseResult
import cloud.horizonbot.webhookprocessor.core.SignatureVerifier
import cloud.horizonbot.webhookprocessor.core.VoteEvent
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

class TopGGAdapter(
    verifier: SignatureVerifier,
) : AbstractWebhookAdapter<TopGGPayload>(TopGGPayload.serializer(), verifier) {
    override val path = "topgg"

    override fun toResult(payload: TopGGPayload): ParseResult {
        if (payload.type !in HANDLED_TYPES) return ParseResult.Ignored
        val data = payload.data ?: return ParseResult.Ignored

        val userId = data.user.platformId.toLong()
        val votedAt = data.createdAt?.let(::parseInstant) ?: Instant.now()
        val expiresAt = data.expiresAt?.let(::parseInstant) ?: votedAt.plus(VOTE_DURATION)

        return ParseResult.Accepted(
            VoteEvent(
                platform = PLATFORM,
                platformName = PLATFORM_NAME,
                userId = userId,
                userName = data.user.name,
                voteId = data.id,
                votedAt = votedAt,
                expiresAt = expiresAt,
                voteUrl = "https://top.gg/bot/${data.project.platformId}/vote",
                isTest = payload.type == TYPE_TEST,
            ),
        )
    }

    private fun parseInstant(value: String): Instant = OffsetDateTime.parse(value).toInstant()

    private companion object {
        const val PLATFORM = "topgg"
        const val PLATFORM_NAME = "top.gg"
        const val TYPE_VOTE = "vote.create"
        const val TYPE_TEST = "webhook.test"
        val HANDLED_TYPES = setOf(TYPE_VOTE, TYPE_TEST)
        val VOTE_DURATION: Duration = Duration.ofHours(12)
    }
}
