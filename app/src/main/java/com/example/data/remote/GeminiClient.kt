package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateDoubtAnswer(
        questionText: String,
        subject: String = "General Science & Competitive Exam",
        targetExam: String = "JEE / NEET / UPSC"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineFallbackAnswer(questionText, subject)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val prompt = """
            You are EduLive+ AI Tutor, an expert faculty for Indian competitive exams ($targetExam, Subject: $subject).
            Provide a clear, step-by-step, pedagogical explanation for this student doubt:
            "$questionText"
            
            Structure your response as follows:
            1. 🎯 **Core Concept**: 1-2 lines summarizing the core formula/rule/event.
            2. ✍️ **Step-by-Step Solution**: Numbered clear steps.
            3. 💡 **Pro Tip / Common Trap**: Key mistake to avoid in $targetExam.
            4. 📌 **Quick Formula / Memory Trick**: Short summary.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getOfflineFallbackAnswer(questionText, subject)
                }

                val responseBodyStr = response.body?.string() ?: return@withContext getOfflineFallbackAnswer(questionText, subject)
                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No solution text returned.")
                    }
                }
                return@withContext getOfflineFallbackAnswer(questionText, subject)
            }
        } catch (e: Exception) {
            return@withContext getOfflineFallbackAnswer(questionText, subject)
        }
    }

    suspend fun generateFlashcards(
        topic: String,
        targetExam: String
    ): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext listOf(
                "Key Principle of $topic" to "Essential high-yield rule frequently asked in $targetExam exams.",
                "Primary Formula / Concept" to "Key relationship: ΔE = hν = hc/λ. Always check SI units!",
                "Exam Trick" to "Eliminate extreme option choices first to increase probability of correct guess."
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val prompt = "Generate 3 high-yield revision flashcard Q&A pairs for $topic in $targetExam format. Format as Q: <question> | A: <answer> on new lines."

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val text = json.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""
                    val pairs = mutableListOf<Pair<String, String>>()
                    text.lines().forEach { line ->
                        if (line.contains("|")) {
                            val parts = line.split("|")
                            if (parts.size >= 2) {
                                val q = parts[0].replace("Q:", "").trim()
                                val a = parts[1].replace("A:", "").trim()
                                pairs.add(q to a)
                            }
                        }
                    }
                    if (pairs.isNotEmpty()) return@withContext pairs
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        return@withContext listOf(
            "Key Principle of $topic" to "Essential high-yield rule frequently asked in $targetExam exams.",
            "Primary Formula / Concept" to "Key relationship: ΔE = hν = hc/λ. Always check SI units!",
            "Exam Trick" to "Eliminate extreme option choices first to increase probability of correct guess."
        )
    }

    private fun getOfflineFallbackAnswer(questionText: String, subject: String): String {
        return """
            🎯 **Core Concept**:
            To solve problems regarding "$questionText", recall the foundational principles of $subject.
            
            ✍️ **Step-by-Step Solution**:
            1. **Identify Given Data**: Extract all numerical values and target variables.
            2. **Apply Direct Equation**: Substitute into the standard governing equation (e.g. F = ma, V = IR, or Article 32 principles).
            3. **Check Dimensional Consistency**: Ensure standard SI unit alignment.
            4. **Evaluate Result**: Verify boundary conditions and sign conventions.
            
            💡 **Pro Tip / Common Trap**:
            Pay special attention to negative signs and unit conversions (e.g. km/h to m/s by multiplying 5/18) commonly tested in competitive exams.
            
            📌 **Quick Formula / Summary**:
            Always memorize the 3 most frequent shortcut formulas for this $subject module before entering the exam hall!
        """.trimIndent()
    }
}
