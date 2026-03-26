package cloud.horizonbot.webhookprocessor.routes

import cloud.horizonbot.webhookprocessor.config.Environment
import cloud.horizonbot.webhookprocessor.dto.TopGGPayload
import cloud.horizonbot.webhookprocessor.models.VotesTable
import cloud.horizonbot.webhookprocessor.models.VoteRemindersTable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import java.time.Instant
import java.time.OffsetDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}
private val json = Json { ignoreUnknownKeys = true }

fun Routing.topGGRoutes(httpClient: HttpClient) {
    post("/webhook/topgg") {
        val signatureHeader = call.request.header("x-topgg-signature")
        if (signatureHeader == null) {
            logger.warn { "Missing x-topgg-signature header" }
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val rawBody = call.receiveText()

        if (!verifySignature(Environment.topGGSecret, signatureHeader, rawBody)) {
            logger.warn { "Invalid top.gg webhook signature" }
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val payload = json.decodeFromString<TopGGPayload>(rawBody)

        if (payload.type == "webhook.test") {
            logger.info { "Received top.gg test webhook" }
            call.respond(HttpStatusCode.OK)
            return@post
        }

        if (payload.type != "vote.create" || payload.data == null) {
            call.respond(HttpStatusCode.OK)
            return@post
        }

        val data = payload.data
        val discordUserId = data.user.platformId.toLong()
        val votedAt = data.createdAt?.let { OffsetDateTime.parse(it).toInstant() } ?: Instant.now()
        val expiredAt = data.expiresAt?.let { OffsetDateTime.parse(it).toInstant() }
            ?: votedAt.plusSeconds(12 * 3600)

        transaction {
            VotesTable.upsert {
                it[userId] = discordUserId
                it[platform] = "topgg"
                it[VotesTable.votedAt] = votedAt
                it[VotesTable.expiredAt] = expiredAt
                it[VotesTable.acknowledged] = false
            }
            VoteRemindersTable.upsert {
                it[userId] = discordUserId
                it[platform] = "topgg"
                it[remindAt] = expiredAt
                it[notified] = false
            }
            VotesTable.update({ VotesTable.userId eq discordUserId }) {
                it[VotesTable.acknowledged] = true
            }
        }

        logger.info { "Recorded top.gg vote from user $discordUserId (expires=$expiredAt)" }

        Environment.discordVoteWebhookUrl?.let { webhookUrl ->
            runCatching {
                httpClient.post(webhookUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildJsonObject {
                            putJsonArray("embeds") {
                                add(
                                    buildJsonObject {
                                        put("title", "New Vote")
                                        put("description", "<@$discordUserId> ${data.user.name} `$discordUserId`")
                                        put("color", 0x00A0FF)
                                        putJsonArray("fields") {
                                            add(
                                                buildJsonObject {
                                                    put("name", "Platform")
                                                    put("value", "top.gg")
                                                    put("inline", false)
                                                },
                                            )
                                            if (data.id != null) {
                                                add(
                                                    buildJsonObject {
                                                        put("name", "Vote ID")
                                                        put("value", "`${data.id}`")
                                                        put("inline", false)
                                                    },
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                        }.toString(),
                    )
                }
            }.onFailure { logger.warn(it) { "Failed to post vote notification to Discord" } }
        }

        call.respond(HttpStatusCode.OK)
    }
}

private fun verifySignature(secret: String, header: String, rawBody: String): Boolean {
    val parts = header.split(",").associate {
        val (k, v) = it.split("=", limit = 2)
        k to v
    }
    val timestamp = parts["t"] ?: return false
    val receivedSig = parts["v1"] ?: return false

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
    val computed = mac.doFinal("$timestamp.$rawBody".toByteArray(Charsets.UTF_8))
    val computedHex = computed.joinToString("") { "%02x".format(it) }

    return computedHex == receivedSig
}
