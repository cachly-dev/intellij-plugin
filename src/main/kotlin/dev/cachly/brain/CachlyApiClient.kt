package dev.cachly.brain

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.HttpURLConnection
import java.net.URI

data class TopLesson(
    val topic: String = "",
    val outcome: String = "",
    @SerializedName("recall_count") val recallCount: Int = 0,
    val severity: String? = null,
    @SerializedName("what_worked") val whatWorked: String = "",
    val ts: String? = null,
)

data class MemoryResponse(
    @SerializedName("lesson_count") val lessonCount: Int = 0,
    @SerializedName("context_count") val contextCount: Int = 0,
    val topics: List<String> = emptyList(),
    @SerializedName("top_lessons") val topLessons: List<TopLesson> = emptyList(),
    @SerializedName("last_session") val lastSession: Map<String, Any>? = null,
    @SerializedName("memory_used_bytes") val memoryUsedBytes: Long = 0,
    @SerializedName("memory_limit_bytes") val memoryLimitBytes: Long = 0,
    @SerializedName("memory_used_pct") val memoryUsedPct: Double = 0.0,
)

data class InstanceResponse(
    val tier: String? = null,
    val status: String? = null,
)

data class BrainHealth(
    val lessons: Int = 0,
    val contexts: Int = 0,
    val totalRecalls: Int = 0,
    val estimatedTokensSaved: Int = 0,
    val tier: String = "unknown",
    val status: String = "unreachable",
    val topLessons: List<TopLesson> = emptyList(),
    val topics: List<String> = emptyList(),
    val lastSession: String? = null,
    val memoryUsedBytes: Long = 0,
    val memoryLimitBytes: Long = 0,
    val memoryUsedPct: Double = 0.0,
) {
    companion object {
        /** Average tokens saved per recall — reuses known solution instead of re-researching. */
        const val TOKENS_PER_RECALL = 1200
    }
}

object CachlyApiClient {

    private val gson = Gson()

    fun fetchHealth(): BrainHealth {
        val settings = CachlySettings.getInstance().state
        if (settings.apiKey.isBlank() || settings.instanceId.isBlank()) {
            return BrainHealth(status = "not_configured")
        }

        val baseUrl = settings.apiUrl.trimEnd('/')
        val id = settings.instanceId

        // 1. Fetch instance info
        val instJson = httpGet("$baseUrl/api/v1/instances/$id", settings.apiKey)
            ?: return BrainHealth(status = "unreachable")
        val inst = gson.fromJson(instJson, InstanceResponse::class.java)

        // 2. Fetch memory stats
        val memJson = httpGet("$baseUrl/api/v1/instances/$id/memory", settings.apiKey)
        val mem = if (memJson != null) gson.fromJson(memJson, MemoryResponse::class.java) else MemoryResponse()

        val totalRecalls = mem.topLessons.sumOf { it.recallCount }
        val lastSessionStr = mem.lastSession?.get("summary")?.toString()
            ?: mem.lastSession?.get("focus")?.toString()

        return BrainHealth(
            lessons = mem.lessonCount,
            contexts = mem.contextCount,
            totalRecalls = totalRecalls,
            estimatedTokensSaved = totalRecalls * BrainHealth.TOKENS_PER_RECALL,
            tier = inst.tier ?: "unknown",
            status = if (inst.tier != null) "healthy" else "degraded",
            topLessons = mem.topLessons,
            topics = mem.topics,
            lastSession = lastSessionStr,
            memoryUsedBytes = mem.memoryUsedBytes,
            memoryLimitBytes = mem.memoryLimitBytes,
            memoryUsedPct = mem.memoryUsedPct,
        )
    }

    fun triggerRecall() {
        val settings = CachlySettings.getInstance().state
        if (settings.apiKey.isBlank() || settings.instanceId.isBlank()) return
        val baseUrl = settings.apiUrl.trimEnd('/')
        httpPost("$baseUrl/api/v1/instances/${settings.instanceId}/recall", settings.apiKey, """{"source":"intellij"}""")
    }

    private fun httpGet(url: String, apiKey: String): String? {
        return try {
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().readText()
            } else null
        } catch (_: Exception) { null }
    }

    private fun httpPost(url: String, apiKey: String, body: String): String? {
        return try {
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.outputStream.bufferedWriter().use { it.write(body) }
            if (conn.responseCode == 200) conn.inputStream.bufferedReader().readText() else null
        } catch (_: Exception) { null }
    }
}
