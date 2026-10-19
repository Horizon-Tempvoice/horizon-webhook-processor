package cloud.horizonbot.webhookprocessor.notification

import cloud.horizonbot.webhookprocessor.core.VoteEvent

interface VoteNotifier {
    val name: String

    suspend fun notify(event: VoteEvent)
}
