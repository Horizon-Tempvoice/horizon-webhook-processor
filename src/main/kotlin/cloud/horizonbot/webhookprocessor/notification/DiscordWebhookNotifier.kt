package cloud.horizonbot.webhookprocessor.notification

import cloud.horizonbot.webhookprocessor.core.VoteEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private val logger = KotlinLogging.logger {}

class DiscordWebhookNotifier(
    private val httpClient: HttpClient,
    private val webhookUrl: String,
) : VoteNotifier {
    override val name = "Discord"

    override suspend fun notify(event: VoteEvent) {
        val response =
            httpClient.post("$webhookUrl?with_components=true") {
                contentType(ContentType.Application.Json)
                setBody(buildMessage(event).toString())
            }
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            logger.warn { "Discord webhook returned ${response.status}: $body" }
        }
    }

    private fun buildMessage(event: VoteEvent): JsonObject =
        buildJsonObject {
            putJsonArray("embeds") {
                addJsonObject {
                    put("title", "New Vote")
                    put("description", "<@${event.userId}> ${event.userName} `${event.userId}`")
                    put("color", EMBED_COLOR)
                    putJsonArray("fields") {
                        addField("Platform", event.platformName)
                        event.voteId?.let { addField("Vote ID", "`$it`") }
                    }
                }
            }
            putJsonArray("components") {
                addJsonObject {
                    put("type", ACTION_ROW)
                    putJsonArray("components") {
                        addJsonObject {
                            put("type", BUTTON)
                            put("style", LINK_STYLE)
                            put("label", "Vote here")
                            put("url", event.voteUrl)
                        }
                    }
                }
            }
        }

    private fun JsonArrayBuilder.addField(
        name: String,
        value: String,
    ) {
        addJsonObject {
            put("name", name)
            put("value", value)
            put("inline", false)
        }
    }

    private companion object {
        const val EMBED_COLOR = 0x00A0FF
        const val ACTION_ROW = 1
        const val BUTTON = 2
        const val LINK_STYLE = 5
    }
}
