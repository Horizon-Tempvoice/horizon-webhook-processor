package cloud.horizonbot.webhookprocessor.core

sealed interface ParseResult {
    data class Accepted(val event: VoteEvent) : ParseResult

    data object Ignored : ParseResult
}
