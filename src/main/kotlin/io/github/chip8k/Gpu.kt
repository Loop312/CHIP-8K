package io.github.chip8k

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import java.io.File

@OptIn(ExperimentalUnsignedTypes::class)
class Gpu {
    //display
    //64x32 pixels (uses an on off system for the pixels)
    //true = on, false = off
    var display = Array(64) {BooleanArray(32)}

    //actual display
    val screen = Canvas(64 * settings.scale, 32 * settings.scale)
    //log of opcodes that are going through
    val opcodeTextArea = TextArea()
    //buttons (4x6 grid)
    val keypadScreen = GridPane()
    //power and load another rom buttons
    val menuScreen = VBox(10.0)

    var buttons = emptyArray<Button>()

    val mainScreen = GridPane()
    val root = StackPane(mainScreen, settings.popup)
    val scene = Scene(root)

    init {
        //buttons (4x4 grid) (need to change the order of the buttons)
        for (y in 0 until 4) {
            for (x in 0 until 4) {
                val button = Button ((y * 4 + x).toString(16).uppercase())
                buttons += button
                button.onMousePressed = EventHandler {
                    keyHandler.keys[y * 4 + x] = true
                    cpu.log(0, "key " + (y * 4 + x).toString(16).uppercase() + " pressed")
                }
                button.onMouseReleased = EventHandler {
                    keyHandler.keys[y * 4 + x] = false
                    cpu.log(0, "key " + (y * 4 + x).toString(16).uppercase() + " released")
                    screen.requestFocus()
                }
                keypadScreen.add(button, x, y)
            }
        }
        //buttons
        menuScreen.alignment = Pos.CENTER
        val powerButton = Button("power")
        buttons += powerButton
        powerButton.onAction = EventHandler {
            if (running) {
                cpu.log(0, "powering off")
                handleLogs()
                cpu.reset()
                running = false
            }
            else {
                cpu.log(0, "powering on")
                cpu.loadProgram(loadedRom.toUByteArray())
                running = true
            }
            screen.requestFocus()
        }

        val pauseButton = Button("toggle pause")
        buttons += pauseButton
        pauseButton.onAction = EventHandler {
            togglePause()
            screen.requestFocus()
        }
        val loadRomButton = Button("load rom")
        buttons += loadRomButton
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
                cpu.loadProgram(loadedRom.toUByteArray())
            } else {
                cpu.log(0, "File selection cancelled")
            }
            screen.requestFocus()
        }

        val settingsButton = Button("settings")
        buttons += settingsButton
        settingsButton.onAction = EventHandler {
            settings.showPopup()
            screen.requestFocus()
        }

        val toggleThemeButton = Button("toggle theme")
        buttons += toggleThemeButton
        toggleThemeButton.onAction = EventHandler {
            if (settings.colours.first == Color.WHITE) {
                settings.colours = Pair(Color.BLACK, Color.WHITE)
            } else {
                settings.colours = Pair(Color.WHITE, Color.BLACK)
            }
            settings.flipStops()
            updateAllGraphics()
            screen.requestFocus()
        }
        menuScreen.children.addAll(powerButton, loadRomButton, pauseButton, settingsButton, toggleThemeButton)

        updateAllGraphics()
        mainScreen.add(screen, 0, 0)
        mainScreen.add(opcodeTextArea, 0, 1)
        mainScreen.add(keypadScreen, 1, 0)
        mainScreen.add(menuScreen, 1, 1)
        screen.requestFocus()
    }
    fun draw(x: Int, y: Int, height: Int) {
        cpu.v[0xF] = 0.toUByte() //collision register defaults to off

        for (row in 0 until height){
            //get sprite data from memory
            val spriteByte = cpu.memory[cpu.i + row].toInt() and 0xFF

            for (bit in 0 until 8) {
                if ((spriteByte and (0x80 shr bit)) != 0) {
                    val pixelX = (x + bit) % 64
                    val pixelY = (y + row) % 32
                    if (display[pixelX][pixelY]) {
                        cpu.v[0xF] = 1.toUByte() // Collision
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
                    gc.fill = settings.colours.first
                } else {
                    gc.fill = settings.colours.second
                }
                gc.fillRect(x * settings.scale, y * settings.scale, settings.scale, settings.scale)
            }
        }
    }

    fun handleLogs() {
        val scrollTop = opcodeTextArea.scrollTop // Get the current vertical scroll position
        opcodeTextArea.appendText(cpu.log + "\n")
        opcodeTextArea.scrollTop = scrollTop // Restore the previous vertical scroll position
    }

    fun togglePause() {
        if (!paused) {
            cpu.log(0, "emulation paused")
            handleLogs()
            paused = true
        }
        else {
            cpu.log(0, "emulation resumed")
            paused = false
        }
    }

    fun updateAllGraphics() {
        mainScreen.background = Background(BackgroundFill(settings.colours.second, null, null))
        opcodeTextArea.style = """
        -fx-control-inner-background: rgb(
         ${settings.colours.second.red * 255},
         ${settings.colours.second.green * 255},
         ${settings.colours.second.blue * 255});

        -fx-text-fill: rgb(
        ${settings.colours.first.red * 255},
        ${settings.colours.first.green * 255},
        ${settings.colours.first.blue * 255});
    """.trimIndent()
        for (button in buttons) {
            button.background = Background(BackgroundFill(settings.gradient, null, null))
            button.textFill = settings.colours.second
        }
        updateDisplay()
    }
}