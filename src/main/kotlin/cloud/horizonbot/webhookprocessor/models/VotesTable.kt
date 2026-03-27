package cloud.horizonbot.webhookprocessor.models

import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.core.Table

object VotesTable : Table("votes") {
    val id = long("id").autoIncrement()
    val userId = long("userid")
    val platform = varchar("platform", 50)
    val votedAt = timestamp("voted_at")
    val expiredAt = timestamp("expired_at")

    override val primaryKey = PrimaryKey(id)
}
