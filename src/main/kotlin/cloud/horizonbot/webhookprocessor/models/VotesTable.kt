package cloud.horizonbot.webhookprocessor.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object TopggVotesTable : Table("topgg_votes") {
    val userId = long("userid")
    val platform = varchar("platform", 50)
    val votedAt = timestamp("voted_at")
    val expiredAt = timestamp("expired_at")

    override val primaryKey = PrimaryKey(userId)
}
