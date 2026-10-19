package cloud.horizonbot.webhookprocessor.persistence

import cloud.horizonbot.webhookprocessor.core.VoteEvent
import cloud.horizonbot.webhookprocessor.models.VoteRemindersTable
import cloud.horizonbot.webhookprocessor.models.VotesTable
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

class ExposedVoteRepository : VoteRepository {
    override fun record(event: VoteEvent) {
        transaction {
            VotesTable.insert {
                it[userId] = event.userId
                it[platform] = event.platform
                it[votedAt] = event.votedAt
                it[expiredAt] = event.expiresAt
            }
            VoteRemindersTable.upsert(
                VoteRemindersTable.userId,
                VoteRemindersTable.platform,
                onUpdate = { it[VoteRemindersTable.remindAt] = event.expiresAt },
            ) {
                it[userId] = event.userId
                it[platform] = event.platform
                it[remindAt] = event.expiresAt
            }
        }
    }
}
