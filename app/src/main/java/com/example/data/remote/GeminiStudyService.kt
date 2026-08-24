package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiStudyService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    fun hasValidApiKey(): Boolean {
        return apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"
    }

    suspend fun generateAiResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext LocalStudyEngine.generateSmartResponse(prompt)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    })
                }
                put("contents", contentsArray)

                if (!systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemInstruction))
                        })
                    })
                }

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiStudyService", "Gemini API error: ${response.code} $responseBody")
                return@withContext LocalStudyEngine.generateSmartResponse(prompt)
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text
            } else {
                LocalStudyEngine.generateSmartResponse(prompt)
            }
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "API call failed, fallback to local engine", e)
            LocalStudyEngine.generateSmartResponse(prompt)
        }
    }

    suspend fun summarizeNotes(rawText: String): NoteSummaryResult = withContext(Dispatchers.IO) {
        if (hasValidApiKey()) {
            try {
                val prompt = """
                    You are an expert college tutor. Analyze the following college study notes and return a structured JSON response:
                    
                    NOTES:
                    $rawText
                    
                    Respond ONLY with valid JSON in this exact structure (no markdown fences, just pure JSON):
                    {
                      "title": "Topic Title",
                      "shortSummary": "A crisp 3-4 sentence comprehensive summary of the core ideas.",
                      "importantPoints": ["Key point 1", "Key point 2", "Key point 3", "Key point 4", "Key point 5"],
                      "keyTerms": [
                        {"term": "Term 1", "definition": "Clear concise definition"},
                        {"term": "Term 2", "definition": "Clear concise definition"},
                        {"term": "Term 3", "definition": "Clear concise definition"}
                      ],
                      "mcqs": [
                        {
                          "question": "Question text here?",
                          "options": ["Option A", "Option B", "Option C", "Option D"],
                          "correctIndex": 0,
                          "explanation": "Clear explanation why this is correct."
                        }
                      ],
                      "vivaQuestions": [
                        {
                          "question": "Viva examiner question?",
                          "answer": "Concise high-scoring oral response.",
                          "keyConcept": "Core concept tested",
                          "difficulty": "Medium"
                        }
                      ]
                    }
                """.trimIndent()

                val resultText = generateAiResponse(prompt, "You are a senior university professor and study aid creator.")
                val cleanedJson = resultText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = JSONObject(cleanedJson)
                val title = json.optString("title", "Study Notes Summary")
                val shortSummary = json.optString("shortSummary", "Comprehensive summary generated.")

                val pointsArray = json.optJSONArray("importantPoints")
                val points = mutableListOf<String>()
                if (pointsArray != null) {
                    for (i in 0 until pointsArray.length()) {
                        points.add(pointsArray.getString(i))
                    }
                }

                val termsArray = json.optJSONArray("keyTerms")
                val terms = mutableListOf<KeyTerm>()
                if (termsArray != null) {
                    for (i in 0 until termsArray.length()) {
                        val obj = termsArray.getJSONObject(i)
                        terms.add(KeyTerm(obj.optString("term"), obj.optString("definition")))
                    }
                }

                val mcqsArray = json.optJSONArray("mcqs")
                val mcqs = mutableListOf<McqQuestion>()
                if (mcqsArray != null) {
                    for (i in 0 until mcqsArray.length()) {
                        val obj = mcqsArray.getJSONObject(i)
                        val opts = mutableListOf<String>()
                        val optsArr = obj.optJSONArray("options")
                        if (optsArr != null) {
                            for (j in 0 until optsArr.length()) {
                                opts.add(optsArr.getString(j))
                            }
                        }
                        mcqs.add(
                            McqQuestion(
                                question = obj.optString("question"),
                                options = opts,
                                correctIndex = obj.optInt("correctIndex", 0),
                                explanation = obj.optString("explanation")
                            )
                        )
                    }
                }

                val vivaArray = json.optJSONArray("vivaQuestions")
                val viva = mutableListOf<VivaQuestion>()
                if (vivaArray != null) {
                    for (i in 0 until vivaArray.length()) {
                        val obj = vivaArray.getJSONObject(i)
                        viva.add(
                            VivaQuestion(
                                question = obj.optString("question"),
                                answer = obj.optString("answer"),
                                keyConcept = obj.optString("keyConcept"),
                                difficulty = obj.optString("difficulty", "Medium")
                            )
                        )
                    }
                }

                if (points.isNotEmpty() || mcqs.isNotEmpty()) {
                    return@withContext NoteSummaryResult(
                        title = title,
                        shortSummary = shortSummary,
                        importantPoints = points,
                        keyTerms = terms,
                        mcqs = mcqs,
                        vivaQuestions = viva
                    )
                }
            } catch (e: Exception) {
                Log.w("GeminiStudyService", "JSON parse error from Gemini, using local engine", e)
            }
        }

        return@withContext LocalStudyEngine.summarizeNotes(rawText)
    }

    suspend fun generateMcqs(subject: String, topic: String, count: Int, difficulty: String): List<McqQuestion> = withContext(Dispatchers.IO) {
        if (hasValidApiKey()) {
            try {
                val prompt = """
                    Generate exactly $count multiple-choice questions for college students on:
                    Subject: $subject
                    Topic: $topic
                    Difficulty: $difficulty
                    
                    Respond ONLY with valid JSON array in this format:
                    [
                      {
                        "question": "Question text?",
                        "options": ["Choice A", "Choice B", "Choice C", "Choice D"],
                        "correctIndex": 0,
                        "explanation": "Clear explanation of the correct concept."
                      }
                    ]
                """.trimIndent()

                val resultText = generateAiResponse(prompt, "You are an exam creator for higher education.")
                val cleanedJson = resultText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val array = JSONArray(cleanedJson)
                val list = mutableListOf<McqQuestion>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val optsArr = obj.getJSONArray("options")
                    val opts = mutableListOf<String>()
                    for (j in 0 until optsArr.length()) {
                        opts.add(optsArr.getString(j))
                    }
                    list.add(
                        McqQuestion(
                            question = obj.getString("question"),
                            options = opts,
                            correctIndex = obj.getInt("correctIndex"),
                            explanation = obj.getString("explanation")
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext list
            } catch (e: Exception) {
                Log.w("GeminiStudyService", "MCQ parsing failed, using local engine", e)
            }
        }

        return@withContext LocalStudyEngine.generateMcqs(subject, topic, count, difficulty)
    }

    suspend fun generateVivaQuestions(subject: String, topic: String, difficulty: String): List<VivaQuestion> = withContext(Dispatchers.IO) {
        if (hasValidApiKey()) {
            try {
                val prompt = """
                    Generate 5 essential college viva-voce oral examination questions and model answers for:
                    Subject: $subject
                    Topic: $topic
                    Target Level: $difficulty
                    
                    Respond ONLY with valid JSON array:
                    [
                      {
                        "question": "Viva question?",
                        "answer": "Concise, precise model answer suitable for speaking.",
                        "keyConcept": "Core concept tested",
                        "difficulty": "$difficulty"
                      }
                    ]
                """.trimIndent()

                val resultText = generateAiResponse(prompt, "You are a senior viva examiner.")
                val cleanedJson = resultText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val array = JSONArray(cleanedJson)
                val list = mutableListOf<VivaQuestion>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        VivaQuestion(
                            question = obj.getString("question"),
                            answer = obj.getString("answer"),
                            keyConcept = obj.optString("keyConcept", topic),
                            difficulty = obj.optString("difficulty", difficulty)
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext list
            } catch (e: Exception) {
                Log.w("GeminiStudyService", "Viva parse failed, fallback to local", e)
            }
        }

        return@withContext LocalStudyEngine.generateVivaQuestions(subject, topic, difficulty)
    }
}
