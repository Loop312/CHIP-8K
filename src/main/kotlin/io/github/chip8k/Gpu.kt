package io.github.chip8k

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import java.io.File

class Gpu {
    //display
    //64x32 pixels (uses an on off system for the pixels)
    //true = on, false = off
    var display = Array(64) {BooleanArray(32)}


    val scale = 10.0

    //actual display
    val screen = Canvas(64 * scale, 32 * scale)
    //log of opcodes that are going through
    val opcodeTextArea = TextArea()
    //buttons (4x6 grid)
    val keypadScreen = GridPane()
    //power and load another rom buttons
    val loadRomScreen = VBox(10.0)

    val root = GridPane()
    val scene = Scene(root)

    lateinit var cpu: Cpu

    init {
        //log of opcodes that are going through
        opcodeTextArea.isEditable = false

        //buttons (4x4 grid) (need to change the order of the buttons)
        for (y in 0 until 4) {
            for (x in 0 until 4) {
                val button = Button ((y * 4 + x).toString(16))
                button.onAction = EventHandler {
                    cpu.log(0, "pressed button " + (y * 4 + x).toString(16))
                }
                keypadScreen.add(button, x, y)
            }
        }
        //power and load another rom buttons
        val powerButton = Button("power")
        powerButton.onAction = EventHandler {
            if (running) {
                cpu.log(0, "powering off")
                handleLogs()
                cpu.reset()
                running = false
            }
            else {
                cpu.log(0, "powering on")
                cpu.loadProgram(loadedRom)
                running = true
            }
        }

        val pauseButton = Button("toggle pause")
        pauseButton.onAction = EventHandler {
            if (running) {
                cpu.log(0, "emulation paused")
                handleLogs()
                running = false
            }
            else {
                cpu.log(0, "emulation resumed")
                running = true
            }
        }
        val loadRomButton = Button("load rom")
        loadRomButton.onAction = EventHandler {
            cpu.log(0, "loading another rom")

            //open file dialog
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("CHIP-8 ROM", "*.ch8"))
            val selectedFile: File? = fileChooser.showOpenDialog(null)
            if (selectedFile != null) {
                cpu.log(0, "Selected file: ${selectedFile.absolutePath}")
                loadedRom = selectedFile.readBytes()
                cpu.reset()
                cpu.loadProgram(loadedRom)
            } else {
                cpu.log(0, "File selection cancelled")
            }
        }
        loadRomScreen.children.addAll(powerButton, loadRomButton, pauseButton)

        root.add(screen, 0, 0)
        root.add(opcodeTextArea, 0, 1)
        root.add(keypadScreen, 1, 0)
        root.add(loadRomScreen, 1, 1)
    }
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
        val gc = screen.graphicsContext2D

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

    fun handleLogs() {
        val scrollTop = opcodeTextArea.scrollTop // Get the current vertical scroll position
        opcodeTextArea.appendText(cpu.log + "\n")
        opcodeTextArea.scrollTop = scrollTop // Restore the previous vertical scroll position
    }
}