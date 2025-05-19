package io.github.chip8k

import javafx.scene.control.TextArea

class LogHandler {
    private val maxLogLines = 50
    private var currentLogLineCount = 0
    var log = ""
    val textArea = TextArea()

    fun handleLogs() {
        val scrollTop = textArea.scrollTop
        textArea.appendText(log + "\n")
        currentLogLineCount++

        if (currentLogLineCount > maxLogLines) {
            // Find the index of the first newline character
            val firstNewlineIndex = textArea.text.indexOf('\n')
            if (firstNewlineIndex != -1) {
                // Remove the first line (including the newline)
                textArea.deleteText(0, firstNewlineIndex + 1)
                currentLogLineCount--
            } else {
                // Fallback if no newline is found (shouldn't happen in normal operation)
                textArea.clear()
                textArea.appendText(log + "\n")
                currentLogLineCount = 1
            }
        }
        // Restore the previous vertical scroll position
        textArea.scrollTop = scrollTop
    }
}