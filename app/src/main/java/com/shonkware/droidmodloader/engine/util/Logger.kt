package com.shonkware.droidmodloader.engine.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object Logger
{
    // ---- Log Level Enum ----
    enum class LogLevel
    {
        DEBUG,INFO,ERROR
    }

    // ---- Configurable Log Level ----
    var currentLevel: LogLevel = LogLevel.DEBUG

    // ---- Output Hook (can redirect to UI later) ----
    var output: (String) -> Unit = {message -> println(message)}

    // ---- Timestamp Formatter ----
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    // ---- Core Log Function ----
    fun log(level: LogLevel, message: String)
    {
        if (level.ordinal < currentLevel.ordinal) return

        val time = LocalTime.now().format(timeFormatter)
        val thread = Thread.currentThread().name

        val formatted = "[$time][$thread][${level.name}] $message"

        output(formatted)
    }

    //--- Convenience Methods ---
    fun debug(message: String) = log(LogLevel.DEBUG, message)

    fun info(message: String) = log(LogLevel.INFO, message)

    fun error(message: String) = log(LogLevel.ERROR, message)

    // ---- Timing Utility ----
    inline fun <T> time(label: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return block()
        } finally {
            val end = System.currentTimeMillis()
            val duration = end - start
            info("$label completed in ${duration}ms")
        }
    }
}
