package io.github.chip8k

import javafx.event.EventHandler
import javafx.scene.input.KeyCode

class KeyHandler {
    //keyboard
    var keys = BooleanArray(16) // 16 keys on the keyboard, seems to follow hexadecimal system (0-9, A-F)

    var keyMap = mapOf<KeyCode, Int>(
        KeyCode.DIGIT1 to 0x1, KeyCode.DIGIT2 to 0x2, KeyCode.DIGIT3 to 0x3, KeyCode.DIGIT4 to 0xC,
        KeyCode.Q to 0x4, KeyCode.W to 0x5, KeyCode.E to 0x6, KeyCode.R to 0xD,
        KeyCode.A to 0x7, KeyCode.S to 0x8, KeyCode.D to 0x9, KeyCode.F to 0xE,
        KeyCode.Z to 0xA, KeyCode.X to 0x0, KeyCode.C to 0xB, KeyCode.V to 0xF
    )


    fun handleInputs() {
        gpu.screen.onKeyPressed = EventHandler { event ->
            keyMap[event.code]?.let { index ->
                if (index in keys.indices) {
                    keys[index] = true
                    cpu.log(0, "key " + index.toString(16).uppercase() + " pressed")
                }
            }
        }
        gpu.screen.onKeyReleased = EventHandler { event ->
            keyMap[event.code]?.let { index ->
                if (index in keys.indices) {
                    keys[index] = false
                    cpu.log(0, "key " + index.toString(16).uppercase() + " released")
                }
            }
        }
    }

    fun keyDown(index: Int): Boolean {
        return keys[index]
    }
}