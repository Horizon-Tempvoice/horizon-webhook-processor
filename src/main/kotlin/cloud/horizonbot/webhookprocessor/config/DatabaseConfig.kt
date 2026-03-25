package cloud.horizonbot.webhookprocessor.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun init() {
        val dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://${Environment.dbHost}:${Environment.dbPort}/${Environment.dbName}"
                username = Environment.dbUser
                password = Environment.dbPassword
                maximumPoolSize = 5
                minimumIdle = 1
            },
        )

        Database.connect(dataSource)
    }
}
