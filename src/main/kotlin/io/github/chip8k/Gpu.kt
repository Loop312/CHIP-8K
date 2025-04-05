package io.github.chip8k

import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color

class Gpu {
    //display
    //64x32 pixels (uses an on off system for the pixels)
    //true = on, false = off
    var display = Array(64) {BooleanArray(32)}


    val scale = 10.0

    val canvas = Canvas(64 * scale, 32 * scale)
    val root = StackPane(canvas)
    val scene = Scene(root)

    lateinit var cpu: Cpu

    fun draw(x: Int, y: Int, height: Int) {
        cpu.v[0xF] = 0.toByte() //collision register defaults to off

        for (row in 0 until height){
            //get sprite data from memory
            val spriteByte = cpu.memory[cpu.i + row].toInt() and 0xFF

            for (bit in 0 until 8) {
                if ((spriteByte and (0x80 shr bit)) != 0) {
                    val pixelX = (x + bit) % 64
                    val pixelY = (y + row) % 32
                    if (display[pixelX][pixelY]) {
                        cpu.v[0xF] = 1.toByte() // Collision
                    }
                    display[pixelX][pixelY] = display[pixelX][pixelY] xor true
                }

            }
        }
    }

    fun updateDisplay() {
        val gc = canvas.graphicsContext2D

        for (y in 0 until 32) {
            for (x in 0 until 64) {
                if (display[x][y]) {
                    gc.fill = Color.WHITE
                } else {
                    gc.fill = Color.BLACK
                }
                gc.fillRect(x * scale, y * scale, scale, scale)
            }
        }
    }
}