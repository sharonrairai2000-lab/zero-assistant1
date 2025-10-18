package com.zero.assistant

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object OpenAIClient {
    // IMPORTANT: Do NOT hardcode your API key in production.
    // For testing, set the key here or better: load from secure storage.
    private const val OPENAI_API_KEY = "YOUR_OPENAI_API_KEY_HERE"
    private const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"
    private val JSON = MediaType.get("application/json; charset=utf-8")
    private val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getAssistantReply(input: String, conversation: List<String> = emptyList(), callback: (String) -> Unit) {
        val systemPrompt = "You are Zero — a casual friendly assistant for a user from Siliguri who runs a shoe brand called 'Fancy Kick' and does real estate/interior work under 'Planner Construction'. When online, answer thoughtfully with suggestions, and when offline respond with graceful fallback. Keep replies concise and helpful."
        val messagesJson = StringBuilder()
        messagesJson.append("[")
        messagesJson.append("{"role":"system","content":"")
        messagesJson.append(escapeJson(systemPrompt))
        messagesJson.append(""},")
        val maxContext = 6
        val recent = conversation.takeLast(maxContext)
        for (msg in recent) {
            messagesJson.append("{"role":"user","content":"")
            messagesJson.append(escapeJson(msg))
            messagesJson.append(""},")
        }
        messagesJson.append("{"role":"user","content":"")
        messagesJson.append(escapeJson(input))
        messagesJson.append(""}")
        messagesJson.append("]")
        val requestBodyJson = "{"model":"gpt-5","messages":${messagesJson.toString()},"max_tokens":512,"temperature":0.7}"
        val body = RequestBody.create(JSON, requestBodyJson)
        val request = Request.Builder()
            .url(OPENAI_URL)
            .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Sorry, I couldn't reach the server. " + OfflineResponder.getResponse(input))
            }
            override fun onResponse(call: Call, response: Response) {
                response.use { r ->
                    if (!r.isSuccessful) {
                        callback("Error from AI service: ${r.code}. " + OfflineResponder.getResponse(input))
                        return
                    }
                    val text = r.body()?.string() ?: ""
                    val assistantReply = parseAssistantReply(text)
                    if (assistantReply.isBlank()) {
                        callback(OfflineResponder.getResponse(input))
                    } else {
                        callback(assistantReply)
                    }
                }
            }
        })
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\").replace("\"", "\\"").replace("\n", "\\n")
    }

    private fun parseAssistantReply(jsonText: String): String {
        try {
            val idx = jsonText.indexOf(""content":")
            if (idx >= 0) {
                val start = jsonText.indexOf('"', idx + 10)
                val end = jsonText.indexOf('"', start + 1)
                if (start >= 0 && end > start) {
                    return jsonText.substring(start + 1, end).replace("\n", "
").replace("\"", """)
                }
            }
        } catch (e: Exception) {
        }
        return ""
    }
}
