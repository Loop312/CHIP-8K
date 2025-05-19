package io.github.chip8k

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
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

    //
    val keypadAndStatsBox = VBox()
    //buttons (4x6 grid)
    val keypadScreen = GridPane()
    //power and load another rom buttons
    val menuScreen = VBox(10.0)

    var buttons = emptyArray<Button>()

    val liveStats = LiveStats()
    val mainScreen = GridPane()
    val root = StackPane(mainScreen, settings.popup)
    val scene = Scene(root)


    init {

        //buttons (4x4 grid) (need to change the order of the buttons)
        //keypadScreen.alignment = Pos.BOTTOM_CENTER
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
                    keyHandler.lastKeyPress = if (keyHandler.lastKeyPressRecorder) y * 4 + x else -1
                    cpu.log(0, "key " + (y * 4 + x).toString(16).uppercase() + " released")
                    screen.requestFocus()
                }
                keypadScreen.add(button, x, y)
            }
        }
        keypadAndStatsBox.children.addAll(liveStats.box, keypadScreen)
        //buttons
        menuScreen.alignment = Pos.BOTTOM_CENTER
        val saveButton = Button("save state")
        saveButton.minWidth = 100.0
        buttons += saveButton
        saveButton.onAction = EventHandler {
            cpu.saveState()
        }

        val loadButton = Button("load state")
        buttons += loadButton
        loadButton.minWidth = 100.0
        loadButton.onAction = EventHandler {
            cpu.loadState()
        }

        val powerButton = Button("power")
        powerButton.minWidth = 100.0
        buttons += powerButton
        powerButton.onAction = EventHandler {
            if (running) {
                cpu.log(0, "powering off")
                logHandler.handleLogs()
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

        val restartButton = Button("restart")
        restartButton.minWidth = 100.0
        buttons += restartButton
        restartButton.onAction = EventHandler {
            cpu.log(0, "restarting")
            logHandler.handleLogs()
            running = false
            cpu.reset()
            cpu.loadProgram(loadedRom.toUByteArray())
            running = true
            screen.requestFocus()
        }

        val pauseButton = Button("toggle pause")
        pauseButton.minWidth = 100.0
        buttons += pauseButton
        pauseButton.onAction = EventHandler {
            togglePause()
            screen.requestFocus()
        }
        val loadRomButton = Button("load rom")
        loadRomButton.minWidth = 100.0
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
        settingsButton.minWidth = 100.0
        buttons += settingsButton
        settingsButton.onAction = EventHandler {
            settings.showPopup()
            screen.requestFocus()
        }

        val toggleThemeButton = Button("toggle theme")
        toggleThemeButton.minWidth = 100.0
        buttons += toggleThemeButton
        toggleThemeButton.onAction = EventHandler {
            val temp = Pair(settings.color1, settings.color2)
            settings.color1 = temp.second
            settings.color2 = temp.first
            settings.flipStops()
            updateAllGraphics()
            screen.requestFocus()
        }
        menuScreen.children.addAll(
            saveButton,
            loadButton,
            powerButton,
            restartButton,
            loadRomButton,
            pauseButton,
            settingsButton,
            toggleThemeButton
        )

        updateAllGraphics()
        mainScreen.add(screen, 0, 0)
        mainScreen.add(logHandler.textArea, 0, 1)
        mainScreen.add(keypadAndStatsBox, 1, 1)
        mainScreen.add(menuScreen, 1, 0)
        screen.requestFocus()
    }
    fun draw(x: Int, y: Int, height: Int) {
        cpu.v[0xF] = 0.toUByte() //collision register defaults to off

        for (row in 0 until height){
            //get sprite data from memory
            val spriteByte = cpu.memory[cpu.i + row].toInt() and 0xFF

            for (bit in 0 until 8) {
                if ((spriteByte and (0x80 shr bit)) != 0) {
                    var pixelX = 0
                    var pixelY = 0
                    if (!settings.clipping) {
                        pixelX = (x + bit) % 64
                        pixelY = (y + row) % 32
                    }
                    else if (x + bit < 64 && y + row < 32) {
                        pixelX = (x + bit)
                        pixelY = (y + row)
                    }

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
                    gc.fill = settings.color1.value
                } else {
                    gc.fill = settings.color2.value
                }
                gc.fillRect(x * settings.scale, y * settings.scale, settings.scale, settings.scale)
            }
        }
    }


    fun togglePause() {
        if (!paused) {
            cpu.log(0, "emulation paused")
            logHandler.handleLogs()
            paused = true
        }
        else {
            cpu.log(0, "emulation resumed")
            paused = false
        }
    }

    fun updateAllGraphics() {
        mainScreen.background = Background(BackgroundFill(settings.color2.value, null, null))
        logHandler.textArea.style = """
        -fx-control-inner-background: rgb(
         ${settings.color2.value.red * 255},
         ${settings.color2.value.green * 255},
         ${settings.color2.value.blue * 255});

        -fx-text-fill: rgb(
        ${settings.color1.value.red * 255},
        ${settings.color1.value.green * 255},
        ${settings.color1.value.blue * 255});
    """.trimIndent()

        for (button in buttons) {
            button.background = Background(BackgroundFill(settings.gradient, null, null))
            button.textFill = settings.color2.value
        }

        liveStats.updateColors()

        updateDisplay()
    }
}