package cloud.horizonbot.webhookprocessor.persistence

import cloud.horizonbot.webhookprocessor.core.VoteEvent

interface VoteRepository {
    fun record(event: VoteEvent)
}
