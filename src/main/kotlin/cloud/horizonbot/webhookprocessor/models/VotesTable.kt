package cloud.horizonbot.webhookprocessor.models

import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.core.Table

object TopggVotesTable : Table("topgg_votes") {
    val userId = long("userid")
    val platform = varchar("platform", 50)
    val votedAt = timestamp("voted_at")
    val expiredAt = timestamp("expired_at")
    val acknowledged = bool("acknowledged").default(false)

    override val primaryKey = PrimaryKey(userId)
}
