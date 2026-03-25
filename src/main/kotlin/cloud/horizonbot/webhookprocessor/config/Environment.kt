package cloud.horizonbot.webhookprocessor.config

import io.github.cdimascio.dotenv.dotenv

private val dotenv = dotenv {
    directory = "config"
    ignoreIfMissing = true
}

private fun env(key: String): String = System.getenv(key) ?: dotenv[key] ?: error("$key not set")
private fun env(key: String, default: String): String = System.getenv(key) ?: dotenv.get(key, default)

object Environment {
    val dbHost: String = env("DB_HOST", "localhost")
    val dbPort: String = env("DB_PORT", "5432")
    val dbName: String = env("DB_NAME", "horizon")
    val dbUser: String = env("DB_USER")
    val dbPassword: String = env("DB_PASSWORD")
    val topGGSecret: String = env("TOPGG_WEBHOOK_SECRET")
    val discordVoteWebhookUrl: String? = (System.getenv("DISCORD_VOTE_WEBHOOK_URL") ?: dotenv.get("DISCORD_VOTE_WEBHOOK_URL", "")).takeIf { it.isNotBlank() }
}
