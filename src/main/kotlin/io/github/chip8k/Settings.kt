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
        val content = VBox(10.0)
        content.alignment = Pos.CENTER
        val colorPickerStyle = """
            -fx-color-rect: derive(-fx-base, -20%); /* Adjust the color of the rectangle */
            -fx-background-color: rgb(
             ${color2.value.red * 255},
             ${color2.value.green * 255},
             ${color2.value.blue * 255});
            -fx-border-color: gray;
            -fx-border-width: 1px;
            -fx-padding: 5px;
            -fx-text-fill: white;
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
        val fpsSlider = Slider(1.0, 120.0, fps.toDouble())
        fpsSlider.valueProperty().addListener { _, _, newValue ->
            fps = newValue.toInt()
            fpsLabel.text = "FPS: $fps"
            delayInNs = delayInNs()
            println("fps: $fps")
            println("delayInNs: $delayInNs")
        }

        val ipfLabel = Label("IPF: $ipf")
        val ipfSlider = Slider(1.0, 20.0, ipf.toDouble())
        ipfSlider.valueProperty().addListener { _, _, newValue ->
            ipf = newValue.toInt()
            ipfLabel.text = "IPF: $ipf"
        }

        val logLinesLabel = Label("Log Lines: ${logHandler.maxLineCount}")
        val logLinesSlider = Slider(1.0, 200.0, logHandler.maxLineCount.toDouble())
        logLinesSlider.valueProperty().addListener { _, _, newValue ->
            logHandler.maxLineCount = newValue.toInt()
            logLinesLabel.text = "Log Lines: ${logHandler.maxLineCount}"
        }
        val closeButton = Button("Close")
        closeButton.onAction = EventHandler {
            hidePopup()
        }
        val greyBackground = Pane()
        greyBackground.background = Background(BackgroundFill(Color.GRAY.deriveColor(0.0, 0.0, 0.0, 1.0), null, null))

        content.children.addAll(
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

        popup.add(content, 0, 0)
        popup.add(greyBackground, 1, 0)
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
}