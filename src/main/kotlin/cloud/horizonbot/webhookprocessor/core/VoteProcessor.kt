package cloud.horizonbot.webhookprocessor.core

import cloud.horizonbot.webhookprocessor.notification.VoteNotifier
import cloud.horizonbot.webhookprocessor.persistence.VoteRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class VoteProcessor(
    private val repository: VoteRepository,
    private val notifiers: List<VoteNotifier>,
) {
    suspend fun process(event: VoteEvent) {
        if (event.isTest) {
            logger.info { "Received ${event.platformName} test webhook for user ${event.userId} (skipping DB insert)" }
        } else {
            repository.record(event)
            logger.info { "Recorded ${event.platformName} vote from user ${event.userId} (expires=${event.expiresAt})" }
        }

        notifiers.forEach { notifier ->
            try {
                notifier.notify(event)
            } catch (e: Exception) {
                logger.warn(e) { "Failed to post vote notification to ${notifier.name}" }
            }
        }
    }
}
