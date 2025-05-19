package io.github.chip8k

import javafx.scene.control.TextArea

class LogHandler {
    var maxLineCount = 50
    var lineCount = 0
    var log = ""
    val textArea = TextArea()

    fun handleLogs() {
        val scrollTop = textArea.scrollTop
        textArea.appendText(log + "\n")
        lineCount++

        if (lineCount > maxLineCount) {
            // Find the index of the first newline character
            val firstNewlineIndex = textArea.text.indexOf('\n')
            if (firstNewlineIndex != -1) {
                // Remove the first line (including the newline)
                textArea.deleteText(0, firstNewlineIndex + 1)
                lineCount--
            } else {
                // Fallback if no newline is found (shouldn't happen in normal operation)
                textArea.clear()
                textArea.appendText(log + "\n")
                lineCount = 1
            }
            repeat(lineCount - maxLineCount) {
                textArea.deleteText(0, textArea.text.indexOf('\n') + 1)
                lineCount--
            }
        }
        // Restore the previous vertical scroll position
        textArea.scrollTop = scrollTop
    }
}