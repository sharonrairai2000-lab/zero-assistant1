package com.zero.assistant

object OfflineResponder {
    fun getResponse(input: String): String {
        val txt = input.lowercase()
        return when {
            txt.contains("hello") || txt.contains("hi") -> "Hi! I am Zero. How can I help you today?"
            txt.contains("battery") -> "Your battery looks healthy. If it drops fast, try closing background apps."
            txt.contains("storage") || txt.contains("memory") -> "You have limited space. I recommend cleaning unused files."
            txt.contains("remind") -> "Okay — I set a local reminder. I will notify you even if offline."
            txt.contains("who are you") || txt.contains("what are you") -> "I am Zero, your friendly assistant. I can help with reminders, phone checks, and suggestions."
            else -> "I think I need the internet to answer that better. Would you like me to try when online?"
        }
    }
}
