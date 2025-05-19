package io.github.chip8k

import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.control.Button
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.util.Duration

class Settings {
    var scale = 10.0
    var color1 = ColorPicker(Color.WHITE)
    var color2 = ColorPicker(Color.BLACK)
    var fps = 60
    var delayInNs = (1_000_000_000 / fps)
    var ipf = 11

    //v[0xF] = 0 from 8XY1 to 8XY3 (&,|,^ opcodes)
    var vfReset = true
    //i++ in FX55 and FX65 opcodes
    var memory = true
    //don't get fully
    var displayWait = false
    //cuts off the sprite at the edge
    var clipping = true
    //use Y in 8XY6 and 8XYE opcodes
    var shifting = false

    var jumping = false

    // Create a list of Stop objects for the gradient
    var stops = mutableListOf(
        Stop(0.0, color1.value),
        Stop(0.5, color2.value)
    )

    fun flipStops() {
        stops = mutableListOf(
            Stop(0.0, color1.value),
            Stop(0.5, color2.value)
        )

        gradient = RadialGradient(
            0.0, 0.0, // focusAngle, focusDistance
            0.5, 0.5, // centerX, centerY
            1.25, // radius
            true, // proportional
            null, // cycleMethod
            stops
        )
    }

    // Create a RadialGradient
    var gradient = RadialGradient(
        0.0, 0.0, // focusAngle, focusDistance
        0.5, 0.5, // centerX, centerY
        1.25, // radius
        true, // proportional
        null, // cycleMethod
        stops
    )

    var popup = createPopupPane()

    fun reset() {
        scale = 10.0
        color1.value = Color.WHITE
        color2.value = Color.BLACK
        stops = mutableListOf(
            Stop(0.0, color1.value),
            Stop(0.5, color2.value)
        )
        gradient = RadialGradient(
            0.0, 0.0, // focusAngle, focusDistance
            0.5, 0.5, // centerX, centerY
            1.25, // radius
            true, // proportional
            null, // cycleMethod
            stops
        )
        fps = 60
        delayInNs = delayInNs()
        ipf = 11
        cpu.log(0, "settings reset")
        gpu.updateAllGraphics()
    }

    private fun createPopupPane(): GridPane {
        val popupWidth = 200.0
        //val popupHeight = scene.height
        val popup = GridPane()
        popup.prefWidth = popupWidth
        //popup.prefHeight = popupHeight
        popup.background = Background(BackgroundFill(color2.value, null, null))

        // Add some content to the popup
        val generalSettings = VBox(10.0)
        generalSettings.alignment = Pos.CENTER
        val gsLabel = Label("General Settings")
        gsLabel.textFill = Color.WHITE
        val colorPickerStyle = """
            -fx-color-rect: derive(-fx-base, -20%); /* Adjust the color of the rectangle */
            -fx-background-color: rgb(
             ${color2.value.red * 255},
             ${color2.value.green * 255},
             ${color2.value.blue * 255});
            -fx-border-color: white;
            -fx-border-width: 1px;
            -fx-padding: 5px;
        """
        color1.onAction = EventHandler {
            colorChange()
        }
        color2.onAction = EventHandler {
            colorChange()
        }
        color1.style = colorPickerStyle
        color2.style = colorPickerStyle

        val resetSettingsButton = Button ("Reset Settings")
        resetSettingsButton.onAction = EventHandler {
            hidePopup()
            reset()
        }

        val fpsLabel = Label("FPS: $fps")
        fpsLabel.textFill = Color.WHITE
        val fpsSlider = Slider(1.0, 120.0, fps.toDouble())
        fpsSlider.valueProperty().addListener { _, _, newValue ->
            fps = newValue.toInt()
            fpsLabel.text = "FPS: $fps"
            delayInNs = delayInNs()
            println("fps: $fps")
            println("delayInNs: $delayInNs")
        }

        val ipfLabel = Label("IPF: $ipf")
        ipfLabel.textFill = Color.WHITE
        val ipfSlider = Slider(1.0, 20.0, ipf.toDouble())
        ipfSlider.valueProperty().addListener { _, _, newValue ->
            ipf = newValue.toInt()
            ipfLabel.text = "IPF: $ipf"
        }

        val logLinesLabel = Label("Log Lines: ${logHandler.maxLineCount}")
        logLinesLabel.textFill = Color.WHITE
        val logLinesSlider = Slider(1.0, 200.0, logHandler.maxLineCount.toDouble())
        logLinesSlider.valueProperty().addListener { _, _, newValue ->
            logHandler.maxLineCount = newValue.toInt()
            logLinesLabel.text = "Log Lines: ${logHandler.maxLineCount}"
        }
        val closeButton = Button("Close")
        closeButton.onAction = EventHandler {
            hidePopup()
        }

        generalSettings.children.addAll(
            gsLabel,
            color1,
            color2,
            fpsLabel,
            fpsSlider,
            ipfLabel,
            ipfSlider,
            logLinesLabel,
            logLinesSlider,
            resetSettingsButton,
            closeButton
        )

        val presets = VBox(10.0)
        presets.alignment = Pos.TOP_CENTER
        val presetsLabel = Label("Presets")
        presetsLabel.textFill = Color.WHITE
        val chip8 = Button("CHIP-8")
        chip8.background = Background(BackgroundFill(gradient, null, null))
        chip8.onAction = EventHandler {
            handleEmulationPreset("chip-8")
        }
        val superChipM = Button("Super-CHIP M")
        superChipM.background = Background(BackgroundFill(gradient, null, null))
        superChipM.onAction = EventHandler {
            handleEmulationPreset("super-chip M")
        }
        val superChipL = Button("Super-CHIP L")
        superChipL.background = Background(BackgroundFill(gradient, null, null))
        superChipL.onAction = EventHandler {
            handleEmulationPreset("super-chip L")
        }
        val xoChip = Button("XO-CHIP")
        xoChip.background = Background(BackgroundFill(gradient, null, null))
        xoChip.onAction = EventHandler {
            handleEmulationPreset("xo-chip")
        }
        presets.children.addAll(presetsLabel, chip8, superChipM, superChipL, xoChip)

        popup.hgap = 10.0
        popup.add(generalSettings, 0, 0)
        popup.add(presets, 1, 0)
        //popup.alignment = Pos.CENTER_LEFT
        slideOut(popup)
        return popup
    }

    fun showPopup() {
        cpu.log(0, "settings opened, emulation paused")
        logHandler.handleLogs()
        paused = true
        slideIn(popup)
    }

    fun hidePopup() {
        cpu.log(0, "settings closed, emulation resumed")
        logHandler.handleLogs()
        paused = false
        slideOut(popup)
    }

    private fun slideIn(pane: Pane, targetX: Double = 0.0) {
        val transition = TranslateTransition(Duration.millis(500.0), pane)
        transition.toX = targetX
        transition.play()
    }

    private fun slideOut(pane: Pane, targetX: Double = 1000.0) {
        val transition = TranslateTransition(Duration.millis(500.0), pane)
        transition.toX = targetX
        transition.play()
    }

    private fun colorChange() {
        color1.value = color1.value
        color2.value = color2.value
        stops = mutableListOf(
            Stop(0.0, color1.value),
            Stop(0.5, color2.value)
        )
        gradient = RadialGradient(
            0.0, 0.0, // focusAngle, focusDistance
            0.5, 0.5, // centerX, centerY
            1.25, // radius
            true, // proportional
            null, // cycleMethod
            stops
        )
        gpu.updateAllGraphics()
    }

    fun delayInNs(): Int {
        return (1_000_000_000 / fps)
    }

    fun handleEmulationPreset(preset: String) {
        when (preset) {
            "chip-8" -> {
                //v[0xF] = 0 from 8XY1 to 8XY3 (&,|,^ opcodes)
                vfReset = true
                //i++ in FX55 and FX65 opcodes
                memory = true
                //
                displayWait = true
                //cuts off the sprite at the edge
                clipping = true
                //use Y in 8XY6 and 8XYE opcodes
                shifting = false
                jumping = false
            }
            "super-chip M" -> {
                //v[0xF] = 0 from 8XY1 to 8XY3 (&,|,^ opcodes)
                vfReset = false
                //i++ in FX55 and FX65 opcodes
                memory = false
                //
                displayWait = true
                //cuts off the sprite at the edge
                clipping = true
                //use Y in 8XY6 and 8XYE opcodes
                shifting = true
                jumping = true
            }
            "super-chip L" -> {
                //v[0xF] = 0 from 8XY1 to 8XY3 (&,|,^ opcodes)
                vfReset = false
                //i++ in FX55 and FX65 opcodes
                memory = false
                //
                displayWait = false
                //cuts off the sprite at the edge
                clipping = true
                //use Y in 8XY6 and 8XYE opcodes
                shifting = true
                //
                jumping = true
            }
            "xo-chip" -> {
                //v[0xF] = 0 from 8XY1 to 8XY3 (&,|,^ opcodes)
                vfReset = false
                //i++ in FX55 and FX65 opcodes
                memory = true
                //
                displayWait = false
                //cuts off the sprite at the edge
                clipping = false
                //use Y in 8XY6 and 8XYE opcodes
                shifting = false
                //
                jumping = false
            }
        }

    }
}