package com.example.data.remote

import com.example.data.model.*
import java.util.Locale

object LocalStudyEngine {

    fun generateSmartResponse(prompt: String): String {
        val lower = prompt.lowercase(Locale.ROOT)
        return when {
            lower.contains("explain simply") || lower.contains("simple") -> {
                val topic = extractTopic(prompt)
                """
                💡 **Simple Breakdown of $topic**:
                
                Think of **$topic** like a real-world system:
                • **Core Idea**: It solves the problem of organizing and processing complex information efficiently without overloading resources.
                • **How it Works**: 
                  1. Input is received and validated.
                  2. A structured set of rules (algorithm/mechanism) processes the state step-by-step.
                  3. Output is produced with optimal time and memory.
                • **Real-World Analogy**: Like a well-organized airport terminal where passengers (data) are routed through security checkpoints (filters) to their designated boarding gates (destinations).
                
                **Key Takeaway**: Understanding the foundational rules makes predicting the edge cases easy!
                """.trimIndent()
            }
            lower.contains("detailed") || lower.contains("deep") -> {
                val topic = extractTopic(prompt)
                """
                📘 **Comprehensive Academic Analysis: $topic**
                
                ### 1. Theoretical Foundations
                **$topic** represents a fundamental construct in academic curricula. Its primary objective is providing mathematical or structural guarantees regarding consistency, efficiency, and scalability.
                
                ### 2. Core Mechanisms & Components
                1. **State Transition**: State changes are bounded by invariant properties that guarantee deterministic behavior.
                2. **Resource Management**: Balances temporal complexity (O(N) / O(log N)) with spatial overhead.
                3. **Synchronization & Consistency**: Ensures that concurrent or interdependent operations maintain transactional integrity.
                
                ### 3. Practical Applications
                * High-throughput enterprise architectures.
                * Distributed data pipelines and operating system scheduling.
                * Mathematical optimization and cryptographic verification.
                
                ### 4. Common Exam Traps
                * Confusing worst-case time complexity with average-case behavior.
                * Neglecting boundary conditions (empty states, zero limits, null references).
                """.trimIndent()
            }
            lower.contains("example") -> {
                val topic = extractTopic(prompt)
                """
                🌟 **Practical Examples & Case Studies: $topic**
                
                **Example 1: Everyday Concrete Application**
                Suppose a system processes 10,000 requests per minute. Using **$topic**, request routing is prioritized so critical tasks complete in under 5ms while background batches execute in idle windows.
                
                **Example 2: Code / Mathematical Model**
                ```
                Step 1: Initialize base state (base_case = 0)
                Step 2: For each item in collection:
                           Apply transformation rule: f(x) -> x'
                           Verify invariant consistency
                Step 3: Return aggregated result
                ```
                
                **Example 3: Counter-Example (What happens without it?)**
                Without proper application of $topic, resource starvation, memory leaks, and cascading race conditions can occur under heavy load.
                """.trimIndent()
            }
            lower.contains("mcq") || lower.contains("quiz") -> {
                """
                📝 **Quick Practice MCQs:**
                
                **Q1: What is the primary advantage of this concept?**
                A) Eliminates runtime complexity completely
                B) Guarantees deterministic resource management and modularity ✅
                C) Decreases network security
                D) Disables compiler optimizations
                
                *Explanation*: Modularity and deterministic resource handling ensure system predictability.
                
                **Q2: Which asymptotic notation represents optimal worst-case bound?**
                A) O(1)
                B) O(log n) ✅
                C) O(n²)
                D) O(2ⁿ)
                
                *Explanation*: Logarithmic complexity scales sub-linearly with input volume.
                """.trimIndent()
            }
            lower.contains("viva") || lower.contains("oral") -> {
                """
                🎓 **Top Viva-Voce Questions for Examiners:**
                
                **Q1: How would you defend this design choice in an oral defense?**
                *Answer*: Emphasize the trade-off between algorithmic latency and memory footprint, demonstrating that the chosen approach minimizes critical path bottlenecks.
                
                **Q2: What is the single biggest vulnerability or limitation?**
                *Answer*: Edge cases involving non-linear scaling or unhandled concurrency deadlocks under extreme contention.
                """.trimIndent()
            }
            lower.contains("revision") || lower.contains("notes") -> {
                val topic = extractTopic(prompt)
                """
                📌 **High-Yield Revision Sheet: $topic**
                
                • **Definition**: A standard paradigm designed to optimize performance, correctness, and modular design.
                • **Key Formula / Rule**: Invariant state must hold true before and after every state transition.
                • **3 Golden Rules for the Exam**:
                  1. Always state assumptions and time/space complexity first.
                  2. Draw neat block diagrams with clearly labeled inputs/outputs.
                  3. Mention trade-offs (e.g. Memory vs. Speed).
                """.trimIndent()
            }
            else -> {
                """
                🎓 **AI StudyMate Insights on ${prompt.take(40)}**:
                
                • **Core Concept**: This topic centers around optimizing efficiency, analytical problem-solving, and practical application.
                • **High-Yield Insight**: In exams, professors look for clear definitions, labeled step-by-step mechanisms, and practical trade-off analysis.
                • **Suggested Next Steps**:
                  1. Use the **Note Summarizer** to distill long textbook chapters.
                  2. Test your recall with **MCQ Generator** (5-10 questions).
                  3. Rehearse for oral examinations using **Viva Preparation**.
                """.trimIndent()
            }
        }
    }

    private fun extractTopic(prompt: String): String {
        val clean = prompt.replace("explain simply", "")
            .replace("detailed explanation", "")
            .replace("give examples", "")
            .replace("create mcqs", "")
            .replace("create viva questions", "")
            .replace("make revision notes", "")
            .replace("about", "")
            .replace("what is", "")
            .replace("explain", "")
            .trim()
        return if (clean.length > 2) clean.replaceFirstChar { it.uppercase() } else "The Study Topic"
    }

    fun summarizeNotes(rawText: String): NoteSummaryResult {
        val cleanText = rawText.trim()
        val title = when {
            cleanText.contains("Process", ignoreCase = true) || cleanText.contains("OS", ignoreCase = true) || cleanText.contains("Scheduling", ignoreCase = true) -> "Operating Systems: CPU Scheduling & Process Management"
            cleanText.contains("Tree", ignoreCase = true) || cleanText.contains("Binary", ignoreCase = true) || cleanText.contains("Data Structure", ignoreCase = true) -> "Data Structures: Binary Search Trees & Traversal"
            cleanText.contains("Demand", ignoreCase = true) || cleanText.contains("Supply", ignoreCase = true) || cleanText.contains("Economics", ignoreCase = true) -> "Macroeconomics: Market Equilibrium, Supply & Demand"
            cleanText.contains("Cell", ignoreCase = true) || cleanText.contains("Respiration", ignoreCase = true) || cleanText.contains("Biology", ignoreCase = true) -> "Cellular Biology: ATP Synthesis & Cellular Respiration"
            else -> "Study Notes Summary: ${cleanText.lines().firstOrNull()?.take(45) ?: "Core Academic Concepts"}"
        }

        val shortSummary = "These notes outline key foundational principles, algorithmic operations, and theoretical implications essential for examinations. The central framework emphasizes efficiency, trade-off optimization, and structured execution."

        val importantPoints = listOf(
            "Fundamental Principles: Primary definitions establish invariant behaviors under various operational conditions.",
            "Core Methodology: Sequential execution steps ensure reproducible results and boundary-case protection.",
            "Performance Trade-Offs: Optimizing for latency generally incurs space complexity overhead and vice-versa.",
            "Failure Modes: Edge conditions like uninitialized pointers, resource contention, and divide-by-zero must be handled.",
            "Examination Focus: Key scoring criteria include block diagrams, asymptotic derivation, and real-world examples."
        )

        val keyTerms = listOf(
            KeyTerm("Throughput", "The total number of completed operations or processes per unit time."),
            KeyTerm("Invariant", "A condition or mathematical property that remains true throughout execution."),
            KeyTerm("Latency", "The time delay between stimulus initiation and response generation."),
            KeyTerm("Scalability", "The capability of a system to handle increasing load without proportional degradation.")
        )

        val mcqs = listOf(
            McqQuestion(
                question = "What is the primary factor determining system efficiency in these notes?",
                options = listOf("Unbounded queue depth", "Algorithmic time and space complexity", "Manual memory deallocation", "Hardware clock speed exclusively"),
                correctIndex = 1,
                explanation = "Time and space complexity establish the mathematical bounds of algorithmic scalability."
            ),
            McqQuestion(
                question = "Which condition represents a critical edge case that must be validated?",
                options = listOf("Empty/null input collection", "Maximal CPU cache hits", "Optimal network bandwidth", "Single-threaded execution"),
                correctIndex = 0,
                explanation = "Boundary conditions like empty datasets or null inputs are the most common source of runtime errors."
            ),
            McqQuestion(
                question = "How is optimal resource allocation achieved?",
                options = listOf("By maximizing idle thread sleep", "By continuous polling", "By dynamic prioritization and load balancing", "By random task discarding"),
                correctIndex = 2,
                explanation = "Dynamic prioritization prevents starvation while maximizing utilization across cores."
            ),
            McqQuestion(
                question = "What is the primary trade-off when implementing caching mechanisms?",
                options = listOf("Faster retrieval vs increased memory overhead", "Lower security vs higher battery life", "Higher compile time vs lower code readability", "Slower CPU clock vs lower temperatures"),
                correctIndex = 0,
                explanation = "Caching trades memory (space complexity) for instantaneous lookup speeds (time complexity)."
            ),
            McqQuestion(
                question = "Why is modular abstraction essential in system design?",
                options = listOf("To increase compilation warnings", "To decouple components for testability and maintainability", "To enforce single-file coding", "To disable hardware interrupts"),
                correctIndex = 1,
                explanation = "Modularity isolates faults, promotes code reuse, and enables independent unit testing."
            )
        )

        val vivaQuestions = listOf(
            VivaQuestion(
                question = "Explain the fundamental difference between worst-case and amortized complexity.",
                answer = "Worst-case is the absolute upper bound for a single operation. Amortized complexity averages the time of expensive rare operations across a sequence of cheap ones (e.g. Dynamic array resizing).",
                keyConcept = "Complexity Analysis",
                difficulty = "Medium"
            ),
            VivaQuestion(
                question = "How would you prevent deadlock or starvation in this system?",
                answer = "By adhering to Coffman condition prevention: strict resource ordering, timeout mechanisms, and preemptive priority scheduling.",
                keyConcept = "Concurrency & Safety",
                difficulty = "Hard"
            ),
            VivaQuestion(
                question = "What happens if an unexpected exception occurs mid-execution?",
                answer = "The system should rollback uncommitted state changes (atomic guarantee) and emit a descriptive telemetry log without terminating the host process.",
                keyConcept = "Fault Tolerance",
                difficulty = "Easy"
            ),
            VivaQuestion(
                question = "Why is defensive validation critical at API boundaries?",
                answer = "It prevents malformed or malicious inputs from causing undefined behavior deep inside the internal domain model.",
                keyConcept = "Defensive Programming",
                difficulty = "Easy"
            ),
            VivaQuestion(
                question = "How does this concept translate to distributed multi-node environments?",
                answer = "It requires consensus protocols (like Raft or Paxos) to synchronize state across partitions while managing network latency.",
                keyConcept = "Distributed Systems",
                difficulty = "Hard"
            )
        )

        return NoteSummaryResult(
            title = title,
            shortSummary = shortSummary,
            importantPoints = importantPoints,
            keyTerms = keyTerms,
            mcqs = mcqs,
            vivaQuestions = vivaQuestions
        )
    }

    fun generateMcqs(subject: String, topic: String, count: Int, difficulty: String): List<McqQuestion> {
        val questions = mutableListOf<McqQuestion>()
        val topicClean = topic.ifBlank { "Core Concepts" }

        val pool = listOf(
            Triple(
                "In the study of $subject ($topicClean), what is the primary role of foundational models?",
                listOf(
                    "To establish predictive frameworks and formal verification",
                    "To replace empirical experimentation entirely",
                    "To artificially inflate computational overhead",
                    "To limit architectural scalability"
                ),
                Pair(0, "Foundational models provide the mathematical and conceptual baseline for rigorous verification.")
            ),
            Triple(
                "When evaluating $topicClean under $difficulty conditions, which metric is most critical?",
                listOf(
                    "Subjective user sentiment",
                    "Deterministic accuracy, throughput, and error boundaries",
                    "Source code line count",
                    "File storage naming conventions"
                ),
                Pair(1, "Quantitative reliability and boundary behavior determine system viability under load.")
            ),
            Triple(
                "Which mechanism best resolves synchronization bottlenecks in $topicClean?",
                listOf(
                    "Global thread blocking",
                    "Lock-free atomic primitives or partitioned queues",
                    "Disabling hardware interrupts",
                    "Random delay insertion"
                ),
                Pair(1, "Lock-free structures and sharding distribute contention across hardware lanes.")
            ),
            Triple(
                "What is the mathematical consequence of non-linear state divergence in $topicClean?",
                listOf(
                    "Linear speedup",
                    "Cascading failure modes and exponential error propagation",
                    "Reduced memory usage",
                    "Guaranteed asymptotic convergence"
                ),
                Pair(1, "Small perturbations in unbounded non-linear systems amplify rapidly across stages.")
            ),
            Triple(
                "How does $topicClean optimize resource utilization during peak throughput?",
                listOf(
                    "By throttling background garbage collection and queueing requests",
                    "By shutting down network ports",
                    "By erasing previous historical state",
                    "By dropping all incoming packets"
                ),
                Pair(0, "Intelligent queue management and deferred housekeeping safeguard low latency.")
            ),
            Triple(
                "Which principle ensures fault isolation in modern $subject systems?",
                listOf(
                    "Shared mutable global memory",
                    "Encapsulation and clear boundary interfaces",
                    "Hardcoded memory addresses",
                    "Monolithic coupling"
                ),
                Pair(1, "Encapsulation prevents localized failures from corrupting adjacent subsystems.")
            ),
            Triple(
                "In $topicClean, what defines a strictly conservative approximation?",
                listOf(
                    "Overestimating safety margins to guarantee no false negatives",
                    "Ignoring edge cases completely",
                    "Assuming infinite bandwidth and zero latency",
                    "Selecting random constants"
                ),
                Pair(0, "Conservative approximations guarantee safety properties even in catastrophic scenarios.")
            )
        )

        val selected = pool.shuffled().take(count.coerceIn(3, 10))
        for (item in selected) {
            questions.add(
                McqQuestion(
                    question = item.first,
                    options = item.second,
                    correctIndex = item.third.first,
                    explanation = item.third.second
                )
            )
        }

        return questions
    }

    fun generateVivaQuestions(subject: String, topic: String, difficulty: String): List<VivaQuestion> {
        val topicClean = topic.ifBlank { "The Subject" }
        return listOf(
            VivaQuestion(
                question = "How would you define $topicClean in one clear, examiner-ready sentence?",
                answer = "$topicClean in $subject is a systematic framework designed to manage state, maximize operational throughput, and enforce theoretical guarantees.",
                keyConcept = "Fundamental Definition",
                difficulty = "Easy"
            ),
            VivaQuestion(
                question = "What are the three most critical assumptions required for $topicClean to remain valid?",
                answer = "1) Deterministic inputs, 2) Bounded resource availability (memory/CPU/bandwidth), and 3) Consistent interface contracts without uncontrolled side effects.",
                keyConcept = "Operational Invariants",
                difficulty = "Medium"
            ),
            VivaQuestion(
                question = "If an examiner asks how $topicClean scales to large enterprise datasets, what is your answer?",
                answer = "Explain how partitioning, asynchronous pipelining, and indexed structures maintain logarithmic or sub-linear operational latency.",
                keyConcept = "Scalability & Architecture",
                difficulty = "Hard"
            ),
            VivaQuestion(
                question = "What is the single most common design pitfall in $topicClean and how do you remediate it?",
                answer = "Over-engineering premature optimizations before profiling bottlenecks; remediate by establishing clear benchmarks and telemetry first.",
                keyConcept = "Engineering Best Practices",
                difficulty = "Medium"
            ),
            VivaQuestion(
                question = "How do modern industry frameworks implement this concept compared to classical textbook theory?",
                answer = "Industry relies on resilient distributed primitives, eventual consistency models, and microservices rather than monolithic, synchronous blocking locks.",
                keyConcept = "Industry Alignment",
                difficulty = "Hard"
            )
        )
    }
}
