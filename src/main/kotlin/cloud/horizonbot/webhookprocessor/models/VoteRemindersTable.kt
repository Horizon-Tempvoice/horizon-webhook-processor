package cloud.horizonbot.webhookprocessor.models

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object VoteRemindersTable : Table("vote_reminders") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val platform = varchar("platform", 50)
    val remindAt = timestamp("remind_at")
    val notified = bool("notified").default(false)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(userId, platform)
    }
}
